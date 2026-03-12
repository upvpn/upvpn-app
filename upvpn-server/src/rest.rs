use std::fmt;

use serde::de::DeserializeOwned;
use upvpn_types::rest::*;

#[derive(Debug)]
pub enum RestError {
    Http(reqwest::Error),
    Api { status: u16, error: ApiErrorResponse },
}

impl fmt::Display for RestError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            RestError::Http(e) => write!(f, "HTTP error: {e}"),
            RestError::Api { status, error } => {
                write!(
                    f,
                    "API error (status {status}): {}: {}",
                    error.error_type, error.message
                )
            }
        }
    }
}

impl std::error::Error for RestError {}

impl From<reqwest::Error> for RestError {
    fn from(e: reqwest::Error) -> Self {
        RestError::Http(e)
    }
}

pub struct ServerRestApiNoAuth {
    client: reqwest::Client,
    base_url: String,
}

impl ServerRestApiNoAuth {
    pub fn new() -> Self {
        Self {
            client: reqwest::Client::new(),
            base_url: upvpn_config::config().rest_api_host_port().to_string(),
        }
    }

    async fn post<Req: serde::Serialize, Resp: DeserializeOwned>(
        &self,
        path: &str,
        request: &Req,
    ) -> Result<Resp, RestError> {
        let url = format!("{}{}", self.base_url, path);
        let response = self.client.post(&url).json(request).send().await?;

        if !response.status().is_success() {
            let status = response.status().as_u16();
            let body = response.text().await.unwrap_or_default();
            let error = serde_json::from_str::<ApiErrorResponse>(&body).unwrap_or(
                ApiErrorResponse {
                    error_type: "unknown".into(),
                    message: body,
                },
            );
            return Err(RestError::Api { status, error });
        }

        let body = response.json::<Resp>().await?;
        Ok(body)
    }

    pub async fn exchange_token(
        &self,
        request: &ExchangeTokenRequest,
    ) -> Result<ExchangeTokenResponse, RestError> {
        self.post("/api/v1/sso/exchange-token", request).await
    }

    pub async fn sso_add_device(
        &self,
        request: &SsoAddDeviceRequest,
    ) -> Result<SsoAddDeviceResponse, RestError> {
        self.post("/api/v1/sso/devices", request).await
    }
}
