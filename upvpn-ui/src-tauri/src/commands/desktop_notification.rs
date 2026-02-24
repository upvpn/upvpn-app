use tauri_plugin_notification::NotificationExt;

#[tauri::command]
pub async fn send_desktop_notification(
    app_handle: tauri::AppHandle,
    title: String,
    body: String,
) -> Result<(), String> {
    app_handle
        .notification()
        .builder()
        .title(title)
        .body(&body)
        .show()
        .map_err(|e| {
            log::error!("failed to send desktop notification: {body}: {e}");
            format!("failed to send desktop notification")
        })
}
