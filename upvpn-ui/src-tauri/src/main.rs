#![cfg_attr(
    all(not(debug_assertions), target_os = "windows"),
    windows_subsystem = "windows"
)]

mod commands;
mod error;
mod event_forwarder;
mod gnome;
mod state;
mod system_tray;

use commands::auth::{is_signed_in, sign_in, sign_out};
use commands::daemon_online::is_daemon_online;
use commands::desktop_notification::send_desktop_notification;
use commands::file_ops::{open_license, open_log_file};
use commands::location::{locations, recent_locations};
use commands::notification::{ack_notification, notifications};
use commands::version::{current_app_version, update_available};
use commands::vpn_session::{connect, disconnect, get_vpn_status};
use log::LevelFilter;
use state::AppState;
use system_tray::{create_tray_icon, toggle_window_visibility};
use tauri::Manager;
use tauri_plugin_log::{Target, TargetKind};
use upvpn_config::config;

fn main() {
    let _config = config();

    tauri::Builder::default()
        .plugin(tauri_plugin_notification::init())
        .plugin(tauri_plugin_os::init())
        .plugin(tauri_plugin_clipboard_manager::init())
        .plugin(tauri_plugin_shell::init())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_opener::init())
        .manage(AppState::default())
        .invoke_handler(tauri::generate_handler![
            is_daemon_online,
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
                .level_for("zbus", LevelFilter::Warn)
                .level(LevelFilter::Debug)
                .targets([
                    Target::new(TargetKind::LogDir { file_name: None }),
                    Target::new(TargetKind::Stdout),
                    Target::new(TargetKind::Webview),
                ])
                .build(),
        )
        .plugin(tauri_plugin_single_instance::init(|_, _, _| {}))
        .setup(|app| {
            #[cfg(target_os = "macos")]
            {
                use tauri::menu::{Menu, MenuItem, PredefinedMenuItem, Submenu};

                let quit_item =
                    MenuItem::with_id(app, "macos_quit", "Quit", true, Some("CmdOrCtrl+Q"))?;
                let submenu = Submenu::with_id_and_items(
                    app,
                    "upvpn_menu",
                    "UpVPN",
                    true,
                    &[
                        &PredefinedMenuItem::copy(app, None)?,
                        &PredefinedMenuItem::paste(app, None)?,
                        &PredefinedMenuItem::select_all(app, None)?,
                        &PredefinedMenuItem::cut(app, None)?,
                        &PredefinedMenuItem::separator(app)?,
                        &PredefinedMenuItem::close_window(app, None)?,
                        &quit_item,
                    ],
                )?;
                let menu = Menu::with_items(app, &[&submenu])?;
                app.set_menu(menu)?;

                app.on_menu_event(|app_handle, event| {
                    if event.id().as_ref() == "macos_quit" {
                        let app_handle = app_handle.clone();
                        tauri::async_runtime::spawn(async move {
                            let _ = commands::vpn_session::disconnect().await;
                            app_handle.exit(0);
                        });
                    }
                });
            }

            create_tray_icon(app)?;

            Ok(())
        })
        .on_window_event(|window, event| {
            match event {
                // Run frontend in the background
                tauri::WindowEvent::CloseRequested { api, .. } => {
                    api.prevent_close();
                    let app_handle = window.app_handle().clone();
                    if gnome::is_gnome() {
                        let _ = window.minimize();
                    } else {
                        toggle_window_visibility(app_handle);
                    }
                }
                tauri::WindowEvent::Focused(true) => {
                    // On Linux, window decorations (minimize/close buttons) stop responding after hide() + show().
                    #[cfg(target_os = "linux")]
                    {
                        // close and minimize buttons stop working after hide() + show()
                        // https://github.com/tauri-apps/tauri/issues/13440
                        // WORKAROUND: toggle resizable property
                        let _ = window.set_resizable(true);
                        let _ = window.set_resizable(false);
                    }
                }
                _ => {}
            };
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
