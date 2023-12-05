use crate::error::Error;

#[tauri::command]
pub async fn is_daemon_online() -> Result<(), Error> {
    let _client = upvpn_controller::new_grpc_client()
        .await
        .map_err(|_| Error::DaemonIsOffline)?;

    Ok(())
}
