use tauri::{
    AppHandle, CustomMenuItem, Manager, SystemTray, SystemTrayEvent, SystemTrayMenu,
    SystemTrayMenuItem,
};

use crate::state::AppState;

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

pub fn handle_system_tray_event(app: &AppHandle, event: SystemTrayEvent) {
    match event {
        SystemTrayEvent::MenuItemClick { id, .. } => {
            let item_handle = app.tray_handle().get_item(&id);
            match id.as_str() {
                "hide_or_show" => {
                    let window = app.get_window("main").unwrap();
                    let handle = app.app_handle();
                    let state: tauri::State<'_, AppState> = handle.state();

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

                        item_handle.set_title(new_title).unwrap();
                    })
                }
                _ => {}
            }
        }

        _ => {}
    }
}
