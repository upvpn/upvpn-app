use tauri::AppHandle;
use tokio_util::sync::CancellationToken;
use upvpn_controller::proto::{SignInRequest, SsoSignInRequest};

use crate::{error::Error, google_auth, state::AppState};

#[tauri::command]
pub async fn sign_in(
    email: String,
    password: String,
    app_handle: AppHandle,
    state: tauri::State<'_, AppState>,
) -> Result<(), Error> {
    let mut client = upvpn_controller::new_grpc_client()
        .await
        .map_err(|_| Error::DaemonIsOffline)?;

    let req = SignInRequest { email, password };

    let _ = client.account_sign_in(req).await?;

    {
        //start event forwarder
        let mut guard = state.lock().await;
        guard.start_event_forwarder(app_handle).await;
    }

    Ok(())
}

#[tauri::command]
pub async fn sign_out(state: tauri::State<'_, AppState>) -> Result<(), Error> {
    let mut client = upvpn_controller::new_grpc_client()
        .await
        .map_err(|_| Error::DaemonIsOffline)?;

    let _ = client.account_sign_out(()).await?;

    {
        // stop event forwarder
        let mut guard = state.lock().await;
        guard.stop_event_forwarder().await;
    }

    Ok(())
}

#[tauri::command]
pub async fn is_signed_in(
    app_handle: AppHandle,
    state: tauri::State<'_, AppState>,
) -> Result<bool, Error> {
    let mut client = upvpn_controller::new_grpc_client()
        .await
        .map_err(|_| Error::DaemonIsOffline)?;

    let is_authenticated = client.is_authenticated(()).await.map(|r| r.into_inner())?;

    {
        // start event forwarder if needed
        let mut guard = state.lock().await;
        if is_authenticated {
            guard.start_event_forwarder(app_handle).await;
        } else {
            guard.stop_event_forwarder().await;
        }
    }

    Ok(is_authenticated)
}

#[tauri::command]
pub async fn google_sign_in(
    app_handle: AppHandle,
    state: tauri::State<'_, AppState>,
) -> Result<(), Error> {
    // 1. Create cancellation token, store in state
    let cancel_token = CancellationToken::new();
    {
        let mut guard = state.lock().await;
        guard.google_sign_in_cancel = Some(cancel_token.clone());
    }

    // 2. Run OAuth flow (cancellable)
    let result = google_auth::google_sign_in_get_id_token(app_handle.clone(), cancel_token).await;

    // 3. Clear cancellation token from state
    {
        let mut guard = state.lock().await;
        guard.google_sign_in_cancel = None;
    }

    let id_token = result.map_err(|e| Error::GoogleAuthError {
        message: e.to_string(),
    })?;

    // 4. Call daemon via gRPC
    let mut client = upvpn_controller::new_grpc_client()
        .await
        .map_err(|_| Error::DaemonIsOffline)?;

    let req = SsoSignInRequest {
        provider: "google".into(),
        id_token,
    };
    client.sso_sign_in(req).await?;

    // 5. Start event forwarder
    {
        let mut guard = state.lock().await;
        guard.start_event_forwarder(app_handle).await;
    }

    Ok(())
}

#[tauri::command]
pub async fn cancel_google_sign_in(
    state: tauri::State<'_, AppState>,
) -> Result<(), Error> {
    let guard = state.lock().await;
    if let Some(cancel_token) = &guard.google_sign_in_cancel {
        cancel_token.cancel();
    }
    Ok(())
}
