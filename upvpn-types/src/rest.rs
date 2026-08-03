use serde::{Deserialize, Serialize};
use uuid::Uuid;

/// Server error response format
#[derive(Serialize, Deserialize, Clone, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ApiErrorResponse {
    pub error_type: String,
    pub message: String,
}

/// POST /api/v1/sso/exchange-token
#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ExchangeTokenRequest {
    pub code: String,
    pub redirect_uri: String,
    pub code_verifier: String,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ExchangeTokenResponse {
    pub id_token: String,
}

/// POST /api/v1/sso/devices
#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct SsoCredentials {
    pub provider: String,
    pub id_token: String,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct SsoDeviceInfo {
    pub name: String,
    pub version: String,
    pub arch: String,
    pub public_key: String,
    pub unique_id: Uuid,
    pub device_type: String,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct SsoAddDeviceRequest {
    pub sso_credentials: SsoCredentials,
    pub device_info: SsoDeviceInfo,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SsoAddDeviceResponse {
    pub token: String,
    pub device_addresses: SsoDeviceAddresses,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SsoDeviceAddresses {
    pub ipv4_address: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserPlanPayAsYouGo {
    pub balance: i32,
}

// internally tagged so that AnnualSubscription ignores
// any content sent by server
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(tag = "type")]
pub enum UserPlan {
    PayAsYouGo { content: UserPlanPayAsYouGo },
    AnnualSubscription,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(tag = "type", content = "content")]
pub enum PurchasePlan {
    PayAsYouGo(u32),
    AnnualSubscription,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CheckoutUrl {
    pub url: String,
}
