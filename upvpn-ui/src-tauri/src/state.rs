use std::sync::Arc;

use tauri::AppHandle;
use upvpn_types::{location::Location, vpn_session::VpnStatus};

use crate::event_forwarder::EventForwarderHandler;

pub type AppState = Arc<tokio::sync::Mutex<UiState>>;

#[derive(Debug)]
pub struct UiState {
    pub event_fwd_handler: Option<EventForwarderHandler>,
    pub locations: Vec<Location>,
    pub vpn_status: Option<VpnStatus>,
    pub window_visible: bool,
}

impl Default for UiState {
    fn default() -> Self {
        Self {
            event_fwd_handler: Default::default(),
            locations: Default::default(),
            vpn_status: None,
            window_visible: true,
        }
    }
}

impl UiState {
    pub async fn start_event_forwarder(&mut self, app_handle: AppHandle) {
        if self.event_fwd_handler.is_none() {
            log::info!("starting event forwarder");
            let event_fwd_handler = EventForwarderHandler::start(app_handle).await;
            self.event_fwd_handler = Some(event_fwd_handler);
        }
    }

    pub async fn stop_event_forwarder(&mut self) {
        if let Some(event_fwd_handler) = self.event_fwd_handler.take() {
            log::info!("stopping event forwarder");
            drop(event_fwd_handler);
        }
    }
}
