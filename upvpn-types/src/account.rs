use serde::{Deserialize, Serialize};

/// Signed in account info as known to the daemon.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AccountInfo {
    /// None when device signed in before app version that stores email
    pub email: Option<String>,
    /// device token used as bearer token for server rest api
    pub token: Option<String>,
}
