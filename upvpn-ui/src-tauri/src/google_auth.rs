use base64::{engine::general_purpose::URL_SAFE_NO_PAD, Engine};
use rand::RngCore;
use sha2::{Digest, Sha256};
use std::time::Duration;
use tauri::AppHandle;
use tauri_plugin_opener::OpenerExt;
use tokio::io::AsyncWriteExt;
use tokio::net::TcpListener;
use tokio_util::sync::CancellationToken;
use upvpn_server::rest::ServerRestApiNoAuth;
use upvpn_types::rest::ExchangeTokenRequest;
use url::Url;

#[derive(Debug)]
pub enum GoogleAuthError {
    NoClientId,
    Cancelled,
    Timeout,
    ListenerError(String),
    StateMismatch,
    NoAuthCode(String),
    TokenExchangeError(String),
}

impl std::fmt::Display for GoogleAuthError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            GoogleAuthError::NoClientId => write!(f, "Google client ID not configured"),
            GoogleAuthError::Cancelled => write!(f, "Google sign-in was cancelled"),
            GoogleAuthError::Timeout => write!(f, "Google sign-in timed out"),
            GoogleAuthError::ListenerError(e) => write!(f, "Listener error: {e}"),
            GoogleAuthError::StateMismatch => write!(f, "OAuth state mismatch"),
            GoogleAuthError::NoAuthCode(e) => write!(f, "No auth code in callback: {e}"),
            GoogleAuthError::TokenExchangeError(e) => write!(f, "Token exchange failed: {e}"),
        }
    }
}

fn generate_pkce() -> (String, String) {
    let mut verifier_bytes = [0u8; 32];
    rand::thread_rng().fill_bytes(&mut verifier_bytes);
    let code_verifier = URL_SAFE_NO_PAD.encode(verifier_bytes);

    let mut hasher = Sha256::new();
    hasher.update(code_verifier.as_bytes());
    let code_challenge = URL_SAFE_NO_PAD.encode(hasher.finalize());

    (code_verifier, code_challenge)
}

fn generate_state() -> String {
    let mut state_bytes = [0u8; 16];
    rand::thread_rng().fill_bytes(&mut state_bytes);
    URL_SAFE_NO_PAD.encode(state_bytes)
}

const SUCCESS_HTML: &str = r#"<!DOCTYPE html>
<html>
<head><title>UpVPN</title></head>
<body style="font-family: system-ui, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #f8fafc;">
<div style="text-align: center;">
<h1 style="color: #1e40af;">Sign-in successful!</h1>
<p style="color: #64748b;">You can close this tab and return to the UpVPN app.</p>
</div>
</body>
</html>"#;

const ACCESS_DENIED_HTML: &str = r#"<!DOCTYPE html>
<html>
<head><title>UpVPN</title></head>
<body style="font-family: system-ui, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #f8fafc;">
<div style="text-align: center;">
<h1 style="color: #64748b;">Sign-in cancelled</h1>
<p style="color: #64748b;">You can close this tab and return to the UpVPN app.</p>
</div>
</body>
</html>"#;

const ERROR_HTML: &str = r#"<!DOCTYPE html>
<html>
<head><title>UpVPN</title></head>
<body style="font-family: system-ui, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #f8fafc;">
<div style="text-align: center;">
<h1 style="color: #dc2626;">Sign-in failed</h1>
<p style="color: #64748b;">Something went wrong. Please try again in the UpVPN app.</p>
</div>
</body>
</html>"#;

pub async fn google_sign_in_get_id_token(
    app_handle: AppHandle,
    cancel_token: CancellationToken,
) -> Result<String, GoogleAuthError> {
    let config = upvpn_config::config();

    let client_id = config
        .google_client_id()
        .ok_or(GoogleAuthError::NoClientId)?
        .to_string();

    // Bind to a random port on loopback
    let listener = TcpListener::bind("127.0.0.1:0")
        .await
        .map_err(|e| GoogleAuthError::ListenerError(e.to_string()))?;
    let port = listener
        .local_addr()
        .map_err(|e| GoogleAuthError::ListenerError(e.to_string()))?
        .port();

    let redirect_uri = format!("http://127.0.0.1:{port}");

    // Generate PKCE and state
    let (code_verifier, code_challenge) = generate_pkce();
    let state = generate_state();

    // Build authorization URL
    let auth_url = format!(
        "https://accounts.google.com/o/oauth2/v2/auth?\
         client_id={client_id}\
         &redirect_uri={redirect_uri}\
         &response_type=code\
         &scope=openid%20email\
         &code_challenge={code_challenge}\
         &code_challenge_method=S256\
         &state={state}"
    );

    // Open browser
    app_handle
        .opener()
        .open_url(&auth_url, None::<&str>)
        .map_err(|e| GoogleAuthError::ListenerError(e.to_string()))?;

    // Wait for callback, cancellation, or timeout
    let (auth_code, returned_state) = tokio::select! {
        result = accept_callback(&listener) => {
            result?
        }
        _ = cancel_token.cancelled() => {
            return Err(GoogleAuthError::Cancelled);
        }
        _ = tokio::time::sleep(Duration::from_secs(300)) => {
            return Err(GoogleAuthError::Timeout);
        }
    };

    // Verify state
    if returned_state != state {
        return Err(GoogleAuthError::StateMismatch);
    }

    // Exchange code for id_token via server
    let id_token = tokio::select! {
        result = exchange_code(&auth_code, &redirect_uri, &code_verifier) => {
            result?
        }
        _ = cancel_token.cancelled() => {
            return Err(GoogleAuthError::Cancelled);
        }
    };

    Ok(id_token)
}

async fn accept_callback(listener: &TcpListener) -> Result<(String, String), GoogleAuthError> {
    let (mut stream, _) = listener
        .accept()
        .await
        .map_err(|e| GoogleAuthError::ListenerError(e.to_string()))?;

    // Read the HTTP request
    let mut buf = vec![0u8; 4096];
    let n = tokio::io::AsyncReadExt::read(&mut stream, &mut buf)
        .await
        .map_err(|e| GoogleAuthError::ListenerError(e.to_string()))?;

    let request = String::from_utf8_lossy(&buf[..n]);

    // Parse the GET request line to extract the path
    let request_line = request.lines().next().unwrap_or("");
    let path = request_line.split_whitespace().nth(1).unwrap_or("/");

    let full_url = format!("http://127.0.0.1{path}");
    let parsed = Url::parse(&full_url)
        .map_err(|e| GoogleAuthError::NoAuthCode(format!("failed to parse URL: {e}")))?;

    // Check for error
    if let Some(error) = parsed.query_pairs().find(|(k, _)| k == "error") {
        let error_code = error.1.to_string();
        let is_access_denied = error_code == "access_denied";

        let html = if is_access_denied {
            ACCESS_DENIED_HTML
        } else {
            ERROR_HTML
        };

        let response = format!(
            "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nConnection: close\r\n\r\n{html}"
        );
        let _ = stream.write_all(response.as_bytes()).await;
        let _ = stream.shutdown().await;

        if is_access_denied {
            return Err(GoogleAuthError::Cancelled);
        }
        return Err(GoogleAuthError::NoAuthCode(error_code));
    }

    let code = parsed
        .query_pairs()
        .find(|(k, _)| k == "code")
        .map(|(_, v)| v.to_string())
        .ok_or_else(|| GoogleAuthError::NoAuthCode("no code parameter".into()))?;

    let returned_state = parsed
        .query_pairs()
        .find(|(k, _)| k == "state")
        .map(|(_, v)| v.to_string())
        .unwrap_or_default();

    // Send success HTML response
    let response = format!(
        "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nConnection: close\r\n\r\n{SUCCESS_HTML}"
    );
    let _ = stream.write_all(response.as_bytes()).await;
    let _ = stream.shutdown().await;

    Ok((code, returned_state))
}

async fn exchange_code(
    code: &str,
    redirect_uri: &str,
    code_verifier: &str,
) -> Result<String, GoogleAuthError> {
    let request = ExchangeTokenRequest {
        code: code.to_string(),
        redirect_uri: redirect_uri.to_string(),
        code_verifier: code_verifier.to_string(),
    };

    let response = ServerRestApiNoAuth::new()
        .exchange_token(&request)
        .await
        .map_err(|e| GoogleAuthError::TokenExchangeError(e.to_string()))?;

    Ok(response.id_token)
}
