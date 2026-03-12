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
