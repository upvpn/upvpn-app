#![cfg_attr(
    all(not(debug_assertions), target_os = "windows"),
    windows_subsystem = "windows"
)]

mod commands;
mod error;
mod event_forwarder;
mod state;
mod system_tray;

use commands::auth::{is_signed_in, sign_in, sign_out};
use commands::desktop_notification::send_desktop_notification;
use commands::file_ops::{open_license, open_log_file};
use commands::location::{locations, recent_locations};
use commands::notification::{ack_notification, notifications};
use commands::version::{current_app_version, update_available};
use commands::vpn_session::{connect, disconnect, get_vpn_status};
use log::LevelFilter;
use state::AppState;
use system_tray::{create_default_system_tray, handle_system_tray_event, toggle_window_visibility};
use tauri::Manager;
use tauri_plugin_log::LogTarget;
use upvpn_config::config;

fn main() {
    let _config = config();

    #[cfg(target_os = "linux")]
    let builder = tauri::Builder::default();
    #[cfg(target_os = "macos")]
    let mut builder = tauri::Builder::default();
    #[cfg(target_os = "windows")]
    let builder = tauri::Builder::default();

    #[cfg(target_os = "macos")]
    {
        use tauri::CustomMenuItem;
        use tauri::Menu;
        use tauri::MenuItem;
        use tauri::Submenu;

        let quit_item = CustomMenuItem::new("macos_quit", "Quit").accelerator("Cmd+Q");
        let menu = Menu::new().add_submenu(Submenu::new(
            "UpVPN",
            Menu::new()
                .add_native_item(MenuItem::Copy)
                .add_native_item(MenuItem::Paste)
                .add_native_item(MenuItem::SelectAll)
                .add_native_item(MenuItem::Cut)
                .add_native_item(MenuItem::Separator)
                .add_native_item(MenuItem::CloseWindow)
                .add_item(quit_item),
        ));
        builder = builder.menu(menu);
    }

    builder
        .manage(AppState::default())
        .invoke_handler(tauri::generate_handler![
            is_signed_in,
            sign_in,
            sign_out,
            locations,
            recent_locations,
            connect,
            disconnect,
            get_vpn_status,
            notifications,
            ack_notification,
            current_app_version,
            update_available,
            send_desktop_notification,
            open_license,
            open_log_file,
        ])
        .plugin(
            tauri_plugin_log::Builder::default()
                .level_for("h2", LevelFilter::Info)
                .level_for("tower", LevelFilter::Info)
                .level(LevelFilter::Debug)
                .targets([LogTarget::LogDir, LogTarget::Stdout, LogTarget::Webview])
                .build(),
        )
        .plugin(tauri_plugin_single_instance::init(|_, _, _| {}))
        .system_tray(create_default_system_tray())
        .on_system_tray_event(handle_system_tray_event)
        .on_window_event(|event| {
            match event.event() {
                // Run frontend in the background
                tauri::WindowEvent::CloseRequested { api, .. } => {
                    api.prevent_close();
                    let app_handle = event.window().app_handle();
                    toggle_window_visibility(app_handle);
                }
                _ => {}
            }
        })
        .on_menu_event(|event| match event.menu_item_id() {
            "macos_quit" => {
                let _ = tauri::async_runtime::block_on(commands::vpn_session::disconnect());
                event.window().app_handle().exit(0);
            }
            _ => {}
        })
        .setup(|_app| Ok(()))
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
