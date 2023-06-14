use tauri::{api::shell, AppHandle, Manager};
use upvpn_config::config;

use crate::error::Error;

#[tauri::command]
pub async fn open_license(app_handle: AppHandle) -> Result<(), Error> {
    let config = config();
    if let Err(e) = shell::open(&app_handle.shell_scope(), &config.license_file_path(), None) {
        println!(
            "failed to open html oss license file: {e:?}: {}",
            &config.license_file_path()
        );
    }
    Ok(())
}
