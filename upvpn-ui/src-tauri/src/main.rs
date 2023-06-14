#![cfg_attr(
    all(not(debug_assertions), target_os = "windows"),
    windows_subsystem = "windows"
)]

mod commands;
mod error;
mod event_forwarder;
mod state;

use commands::auth::{is_signed_in, sign_in, sign_out};
use commands::desktop_notification::send_desktop_notification;
use commands::location::{locations, recent_locations};
use commands::notification::{ack_notification, notifications};
use commands::open_license::open_license;
use commands::version::{current_app_version, update_available};
use commands::vpn_session::{connect, disconnect, get_vpn_status};
use log::LevelFilter;
use state::AppState;
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
        use tauri::Menu;
        use tauri::MenuItem;
        use tauri::Submenu;
        let menu = Menu::new().add_submenu(Submenu::new(
            "upvpn",
            Menu::new().add_native_item(MenuItem::CloseWindow),
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
        .setup(|_app| Ok(()))
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
