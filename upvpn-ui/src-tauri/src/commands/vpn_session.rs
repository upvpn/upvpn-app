use tauri::AppHandle;
use upvpn_types::{location::Location, vpn_session::VpnStatus};

use crate::{error::Error, state::update_app_state};

#[tauri::command]
pub async fn connect(location: Location) -> Result<VpnStatus, Error> {
    let mut client = upvpn_controller::new_grpc_client()
        .await
        .map_err(|_| Error::DaemonIsOffline)?;

    Ok(client
        .connect_vpn(upvpn_controller::proto::Location::from(location))
        .await?
        .into_inner()
        .into())
}

#[tauri::command]
pub async fn disconnect() -> Result<VpnStatus, Error> {
    let mut client = upvpn_controller::new_grpc_client()
        .await
        .map_err(|_| Error::DaemonIsOffline)?;

    Ok(client.disconnect_vpn(()).await?.into_inner().into())
}

#[tauri::command]
pub async fn get_vpn_status(app_handle: AppHandle) -> Result<VpnStatus, Error> {
    let mut client = upvpn_controller::new_grpc_client()
        .await
        .map_err(|_| Error::DaemonIsOffline)?;

    let vpn_status: VpnStatus = client.get_vpn_status(()).await?.into_inner().into();

    update_app_state(app_handle, vpn_status.clone()).await;

    Ok(vpn_status)
}
