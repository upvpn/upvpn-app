use tauri::{
    AppHandle, CustomMenuItem, Manager, SystemTray, SystemTrayEvent, SystemTrayMenu,
    SystemTrayMenuItem,
};
use upvpn_types::vpn_session::VpnStatus;

use crate::{commands, state::AppState};

fn create_default_system_tray_menu(window_visible: bool) -> SystemTrayMenu {
    let vpn_status = CustomMenuItem::new("vpn_status", "Loading...").disabled();
    let quit = CustomMenuItem::new("quit".to_string(), "Quit");
    let hide_or_show_title = if window_visible { "Hide" } else { "Show" };
    let hide_or_show = CustomMenuItem::new("hide_or_show".to_string(), hide_or_show_title);
    SystemTrayMenu::new()
        .add_item(vpn_status)
        .add_native_item(SystemTrayMenuItem::Separator)
        .add_item(hide_or_show)
        .add_item(quit)
}

pub fn create_default_system_tray() -> SystemTray {
    SystemTray::new()
        .with_id("upvpn")
        .with_menu(create_default_system_tray_menu(true))
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

fn get_vpn_status_title(vpn_status: &VpnStatus) -> String {
    match vpn_status {
        VpnStatus::Accepted(location) => format!("Accepted, {}", location.city),
        VpnStatus::ServerCreated(location) => format!("Server Created, {}", location.city),
        VpnStatus::ServerRunning(location) => format!("Server Running, {}", location.city),
        VpnStatus::ServerReady(location) => format!("Server Ready, {}", location.city),
        VpnStatus::Connecting(location) => format!("Connecting, {}", location.city),
        VpnStatus::Connected(location, _) => format!("Connected, {}", location.city),
        VpnStatus::Disconnecting(location) => format!("Disconnecting, {}", location.city),
        VpnStatus::Disconnected => format!("Disconnected"),
    }
}

fn show_disconnect(vpn_status: &VpnStatus) -> bool {
    match vpn_status {
        VpnStatus::Accepted(_)
        | VpnStatus::ServerCreated(_)
        | VpnStatus::ServerRunning(_)
        | VpnStatus::ServerReady(_)
        | VpnStatus::Connecting(_)
        | VpnStatus::Connected(_, _) => true,
        VpnStatus::Disconnecting(_) | VpnStatus::Disconnected => false,
    }
}

fn create_system_tray_menu_internal(
    vpn_status: &VpnStatus,
    window_visible: bool,
) -> SystemTrayMenu {
    let mut tray_menu = SystemTrayMenu::new()
        .add_item(CustomMenuItem::new("vpn_status", get_vpn_status_title(vpn_status)).disabled());

    if show_disconnect(vpn_status) {
        tray_menu = tray_menu.add_item(CustomMenuItem::new("disconnect", "Disconnect"));
    }

    let quit = CustomMenuItem::new("quit".to_string(), "Quit");

    let hide_or_show_title = if window_visible { "Hide" } else { "Show" };

    let hide_or_show = CustomMenuItem::new("hide_or_show".to_string(), hide_or_show_title);

    tray_menu = tray_menu
        .add_native_item(SystemTrayMenuItem::Separator)
        .add_item(hide_or_show)
        .add_item(quit);

    tray_menu
}

fn create_system_tray_menu(vpn_status: &Option<VpnStatus>, window_visible: bool) -> SystemTrayMenu {
    if let Some(vpn_status) = vpn_status {
        create_system_tray_menu_internal(vpn_status, window_visible)
    } else {
        create_default_system_tray_menu(window_visible)
    }
}

pub async fn update_system_tray(app_handle: AppHandle) {
    tauri::async_runtime::spawn(async move {
        let state: tauri::State<'_, AppState> = app_handle.state();
        let state = state.lock().await;
        let new_system_tray_menu = create_system_tray_menu(&state.vpn_status, state.window_visible);
        let _ = app_handle.tray_handle().set_menu(new_system_tray_menu);
    });
}
