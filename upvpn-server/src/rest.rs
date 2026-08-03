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

async fn handle_response<Resp: DeserializeOwned>(
    response: reqwest::Response,
) -> Result<Resp, RestError> {
    if !response.status().is_success() {
        let status = response.status().as_u16();
        let body = response.text().await.unwrap_or_default();
        let error =
            serde_json::from_str::<ApiErrorResponse>(&body).unwrap_or(ApiErrorResponse {
                error_type: "unknown".into(),
                message: body,
            });
        return Err(RestError::Api { status, error });
    }

    let body = response.json::<Resp>().await?;
    Ok(body)
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
        handle_response(response).await
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

/// Rest API with device token as bearer token
pub struct ServerRestApiWithAuth {
    client: reqwest::Client,
    base_url: String,
    token: String,
}

impl ServerRestApiWithAuth {
    pub fn new(token: String) -> Self {
        Self {
            client: reqwest::Client::new(),
            base_url: upvpn_config::config().rest_api_host_port().to_string(),
            token,
        }
    }

    async fn get<Resp: DeserializeOwned>(&self, path: &str) -> Result<Resp, RestError> {
        let url = format!("{}{}", self.base_url, path);
        let response = self
            .client
            .get(&url)
            .bearer_auth(&self.token)
            .send()
            .await?;
        handle_response(response).await
    }

    async fn post<Req: serde::Serialize, Resp: DeserializeOwned>(
        &self,
        path: &str,
        request: &Req,
    ) -> Result<Resp, RestError> {
        let url = format!("{}{}", self.base_url, path);
        let response = self
            .client
            .post(&url)
            .bearer_auth(&self.token)
            .json(request)
            .send()
            .await?;
        handle_response(response).await
    }

    pub async fn current_user_plan(&self) -> Result<UserPlan, RestError> {
        self.get("/api/v1/plan/current").await
    }

    pub async fn checkout(&self, purchase_plan: &PurchasePlan) -> Result<CheckoutUrl, RestError> {
        let checkout_request = CheckoutRequest {
            purchase_plan: purchase_plan.clone(),
            desktop: true,
        };
        self.post("/api/v1/checkout", &checkout_request).await
    }
}
