use serde::Serialize;
use tauri::AppHandle;
use tauri_plugin_opener::OpenerExt;
use upvpn_server::rest::ServerRestApiWithAuth;
use upvpn_types::rest::{PurchasePlan, UserPlan};

use crate::error::Error;

#[derive(Debug, Clone, Serialize)]
pub struct AccountInfo {
    pub email: String,
}

async fn daemon_account_info() -> Result<upvpn_controller::proto::AccountInfo, Error> {
    let mut client = upvpn_controller::new_grpc_client()
        .await
        .map_err(|_| Error::DaemonIsOffline)?;

    Ok(client.get_account_info(()).await?.into_inner())
}

#[tauri::command]
pub async fn account_info() -> Result<AccountInfo, Error> {
    let account_info = daemon_account_info().await?;

    Ok(AccountInfo {
        email: account_info.email,
    })
}

#[tauri::command]
pub async fn user_plan() -> Result<UserPlan, Error> {
    let account_info = daemon_account_info().await?;

    let api = ServerRestApiWithAuth::new(account_info.token);

    Ok(api.current_user_plan().await?)
}

#[tauri::command]
pub async fn checkout(purchase_plan: PurchasePlan, app_handle: AppHandle) -> Result<(), Error> {
    let account_info = daemon_account_info().await?;

    let api = ServerRestApiWithAuth::new(account_info.token);

    let checkout_url = api.checkout(&purchase_plan).await?;

    // complete the purchase in browser
    app_handle
        .opener()
        .open_url(checkout_url.url, None::<&str>)
        .map_err(|e| Error::Opener {
            message: e.to_string(),
        })?;

    Ok(())
}
