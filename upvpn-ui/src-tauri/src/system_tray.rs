use tauri::{
    AppHandle, CustomMenuItem, Manager, SystemTray, SystemTrayEvent, SystemTrayMenu,
    SystemTrayMenuItem,
};

use crate::{commands, state::AppState};

pub fn create_system_tray() -> SystemTray {
    let vpn_status = CustomMenuItem::new("vpn_status", "Loading...").disabled();
    let disconnect = CustomMenuItem::new("disconnect", "Disconnect").disabled();
    let quit = CustomMenuItem::new("quit".to_string(), "Quit");
    let hide_or_show = CustomMenuItem::new("hide_or_show".to_string(), "Hide");
    let tray_menu = SystemTrayMenu::new()
        .add_item(vpn_status)
        .add_item(disconnect)
        .add_native_item(SystemTrayMenuItem::Separator)
        .add_item(hide_or_show)
        .add_item(quit);

    SystemTray::new().with_id("upvpn").with_menu(tray_menu)
}

pub fn toggle_window_visibility(app_handle: AppHandle) {
    let window = app_handle.get_window("main").unwrap();
    let item_handle = app_handle.tray_handle().try_get_item("hide_or_show");
    let state: tauri::State<'_, AppState> = app_handle.state();

    tauri::async_runtime::block_on(async move {
        let mut state = state.lock().await;
        let new_window_visible = !state.window_visible;
        state.window_visible = new_window_visible;

        let new_title = match new_window_visible {
            true => {
                window.show().unwrap();
                "Hide"
            }
            false => {
                window.hide().unwrap();
                "Show"
            }
        };

        if let Some(item_handle) = item_handle {
            item_handle.set_title(new_title).unwrap();
        }
    })
}

pub fn handle_system_tray_event(app: &AppHandle, event: SystemTrayEvent) {
    match event {
        SystemTrayEvent::MenuItemClick { id, .. } => match id.as_str() {
            "hide_or_show" => {
                toggle_window_visibility(app.app_handle());
            }
            "disconnect" => {
                let _ = tauri::async_runtime::block_on(commands::vpn_session::disconnect());
            }
            "quit" => {
                let _ = tauri::async_runtime::block_on(commands::vpn_session::disconnect());
                app.exit(0)
            }
            _ => {}
        },
        _ => {}
    }
}
