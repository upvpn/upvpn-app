use std::path::PathBuf;

use tauri::{AppHandle, Manager};
use tauri_plugin_opener::OpenerExt;
use upvpn_config::config;

use crate::error::Error;

pub fn copy_to_desktop_and_open(app_handle: &AppHandle, src: PathBuf) {
    let desktop = app_handle.path().desktop_dir().ok();
    if let Some(dir) = desktop {
        let filename = src.file_name().unwrap();
        let new_path = dir.join(filename);
        if std::fs::copy(&src, &new_path).is_ok() {
            let _ = app_handle.opener().open_path(new_path.to_str().unwrap(), None::<&str>);
        } else {
            let _ = app_handle.opener().open_path(src.to_str().unwrap(), None::<&str>);
        }
    } else {
        let _ = app_handle.opener().open_path(src.to_str().unwrap(), None::<&str>);
    }
}

#[tauri::command]
pub async fn open_license(app_handle: AppHandle) -> Result<(), Error> {
    let config = config();
    copy_to_desktop_and_open(&app_handle, config.license_file_path());
    Ok(())
}

#[tauri::command]
pub async fn open_log_file(app_handle: AppHandle) -> Result<(), Error> {
    let config = config();
    copy_to_desktop_and_open(&app_handle, config.daemon_log_file_full_path());
    Ok(())
}
