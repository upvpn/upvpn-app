use tauri::{
    menu::{Menu, MenuItem, PredefinedMenuItem},
    tray::TrayIconBuilder,
    AppHandle, Manager,
};
use upvpn_types::vpn_session::VpnStatus;

use crate::{commands, state::AppState};

fn build_default_tray_menu(
    app: &impl Manager<tauri::Wry>,
    window_visible: bool,
) -> tauri::Result<Menu<tauri::Wry>> {
    let hide_or_show_title = if window_visible { "Hide" } else { "Show" };
    let hide_or_show =
        MenuItem::with_id(app, "hide_or_show", hide_or_show_title, true, None::<&str>)?;
    let quit = MenuItem::with_id(app, "quit", "Quit", true, None::<&str>)?;
    Menu::with_items(app, &[&hide_or_show, &quit])
}

pub fn create_tray_icon(app: &tauri::App) -> tauri::Result<()> {
    let menu = build_default_tray_menu(app, true)?;

    TrayIconBuilder::with_id("upvpn")
        .icon(app.default_window_icon().cloned().unwrap())
        .menu(&menu)
        .on_menu_event(|app, event| match event.id().as_ref() {
            "hide_or_show" => {
                toggle_window_visibility(app.clone());
            }
            "disconnect" => {
                tauri::async_runtime::spawn(async {
                    let _ = commands::vpn_session::disconnect().await;
                });
            }
            "quit" => {
                let app = app.clone();
                tauri::async_runtime::spawn(async move {
                    let _ = commands::vpn_session::disconnect().await;
                    app.exit(0);
                });
            }
            _ => {}
        })
        .build(app)?;

    Ok(())
}

pub fn toggle_window_visibility(app_handle: AppHandle) {
    let window = app_handle.get_webview_window("main").unwrap();
    let app_handle_clone = app_handle.clone();
    let state: tauri::State<'_, AppState> = app_handle_clone.state();

    tauri::async_runtime::block_on(async move {
        let mut state = state.lock().await;
        let new_window_visible = !state.window_visible;
        state.window_visible = new_window_visible;

        if new_window_visible {
            // On Linux, window decorations (minimize/close buttons) stop responding
            // after hide() + show().
            #[cfg(target_os = "linux")]
            {
                // close and minimize buttons stop working after hide() + show()
                // https://github.com/tauri-apps/tauri/issues/13440
                // WORKAROUND: toggle resizable property
                let _ = window.set_resizable(true);
                let _ = window.set_resizable(false);
            }
            let _ = window.show();
            let _ = window.set_focus();
        } else {
            let _ = window.hide();
        }

        update_tray_menu(&app_handle, &state.vpn_status, new_window_visible);
    })
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

fn update_tray_menu(app_handle: &AppHandle, vpn_status: &Option<VpnStatus>, window_visible: bool) {
    if let Some(tray) = app_handle.tray_by_id("upvpn") {
        let menu_result = build_tray_menu(app_handle, vpn_status, window_visible);
        if let Ok(menu) = menu_result {
            let _ = tray.set_menu(Some(menu));
        }
    }
}

fn build_tray_menu(
    app: &AppHandle,
    vpn_status: &Option<VpnStatus>,
    window_visible: bool,
) -> tauri::Result<Menu<tauri::Wry>> {
    let hide_or_show_title = if window_visible { "Hide" } else { "Show" };

    match vpn_status {
        Some(vpn_status) => {
            let status_item = MenuItem::with_id(
                app,
                "vpn_status",
                get_vpn_status_title(vpn_status),
                false,
                None::<&str>,
            )?;

            let hide_or_show =
                MenuItem::with_id(app, "hide_or_show", hide_or_show_title, true, None::<&str>)?;
            let quit = MenuItem::with_id(app, "quit", "Quit", true, None::<&str>)?;
            let separator = PredefinedMenuItem::separator(app)?;

            if show_disconnect(vpn_status) {
                let disconnect =
                    MenuItem::with_id(app, "disconnect", "Disconnect", true, None::<&str>)?;
                Menu::with_items(
                    app,
                    &[&status_item, &disconnect, &separator, &hide_or_show, &quit],
                )
            } else {
                Menu::with_items(app, &[&status_item, &separator, &hide_or_show, &quit])
            }
        }
        None => build_default_tray_menu(app, window_visible),
    }
}

pub async fn update_system_tray(app_handle: AppHandle) {
    tauri::async_runtime::spawn(async move {
        let state: tauri::State<'_, AppState> = app_handle.state();
        let state = state.lock().await;
        update_tray_menu(&app_handle, &state.vpn_status, state.window_visible);
    });
}
