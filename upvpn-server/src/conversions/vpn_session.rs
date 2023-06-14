use std::{net::Ipv4Addr, str::FromStr};

use uuid::Uuid;

impl From<crate::proto::Accepted> for upvpn_types::upvpn_server::Accepted {
    fn from(value: crate::proto::Accepted) -> Self {
        Self {
            request_id: Uuid::parse_str(&value.request_id).unwrap(),
            vpn_session_uuid: Uuid::parse_str(&value.vpn_session_uuid).unwrap(),
        }
    }
}

impl From<crate::proto::Ended> for upvpn_types::upvpn_server::Ended {
    fn from(value: crate::proto::Ended) -> Self {
        Self {
            request_id: Uuid::parse_str(&value.request_id).unwrap(),
            vpn_session_uuid: Uuid::parse_str(&value.vpn_session_uuid).unwrap(),
            reason: value.reason,
        }
    }
}

impl From<crate::proto::ServerRunning> for upvpn_types::upvpn_server::ServerRunning {
    fn from(value: crate::proto::ServerRunning) -> Self {
        Self {
            request_id: Uuid::parse_str(&value.request_id).unwrap(),
            vpn_session_uuid: Uuid::parse_str(&value.vpn_session_uuid).unwrap(),
        }
    }
}

impl From<crate::proto::ServerReady> for upvpn_types::upvpn_server::ServerReady {
    fn from(value: crate::proto::ServerReady) -> Self {
        Self {
            request_id: Uuid::parse_str(&value.request_id).unwrap(),
            vpn_session_uuid: Uuid::parse_str(&value.vpn_session_uuid).unwrap(),
            public_key: value.public_key,
            ipv4_endpoint: value.ipv4_endpoint.parse().unwrap(),
            private_ipv4: Ipv4Addr::from_str(&value.private_ipv4).unwrap(),
        }
    }
}

impl From<crate::proto::ServerCreated> for upvpn_types::upvpn_server::ServerCreated {
    fn from(value: crate::proto::ServerCreated) -> Self {
        Self {
            request_id: Uuid::parse_str(&value.request_id).unwrap(),
            vpn_session_uuid: Uuid::parse_str(&value.vpn_session_uuid).unwrap(),
        }
    }
}

impl From<crate::proto::Failed> for upvpn_types::upvpn_server::Failed {
    fn from(value: crate::proto::Failed) -> Self {
        Self {
            request_id: Uuid::parse_str(&value.request_id).unwrap(),
            vpn_session_uuid: Uuid::parse_str(&value.vpn_session_uuid).unwrap(),
        }
    }
}

impl From<crate::proto::ClientConnected> for upvpn_types::upvpn_server::ClientConnected {
    fn from(value: crate::proto::ClientConnected) -> Self {
        Self {
            request_id: Uuid::parse_str(&value.request_id).unwrap(),
            device_unique_id: Uuid::parse_str(&value.device_unique_id).unwrap(),
            vpn_session_uuid: Uuid::parse_str(&value.vpn_session_uuid).unwrap(),
        }
    }
}

impl From<crate::proto::VpnSessionStatus> for upvpn_types::upvpn_server::VpnSessionStatus {
    fn from(value: crate::proto::VpnSessionStatus) -> Self {
        match value.state.unwrap() {
            crate::proto::vpn_session_status::State::Accepted(accepted) => {
                upvpn_types::upvpn_server::VpnSessionStatus::Accepted(accepted.into())
            }
            crate::proto::vpn_session_status::State::Failed(failed) => {
                upvpn_types::upvpn_server::VpnSessionStatus::Failed(failed.into())
            }
            crate::proto::vpn_session_status::State::ServerCreated(server_created) => {
                upvpn_types::upvpn_server::VpnSessionStatus::ServerCreated(server_created.into())
            }
            crate::proto::vpn_session_status::State::ServerRunning(server_running) => {
                upvpn_types::upvpn_server::VpnSessionStatus::ServerRunning(server_running.into())
            }
            crate::proto::vpn_session_status::State::ServerReady(server_ready) => {
                upvpn_types::upvpn_server::VpnSessionStatus::ServerReady(server_ready.into())
            }
            crate::proto::vpn_session_status::State::ClientConnected(client_connected) => {
                upvpn_types::upvpn_server::VpnSessionStatus::ClientConnected(
                    client_connected.into(),
                )
            }
            crate::proto::vpn_session_status::State::Ended(ended) => {
                upvpn_types::upvpn_server::VpnSessionStatus::Ended(ended.into())
            }
        }
    }
}

impl From<upvpn_types::upvpn_server::NewSession> for crate::proto::NewSession {
    fn from(value: upvpn_types::upvpn_server::NewSession) -> Self {
        Self {
            request_id: value.request_id.to_string(),
            device_unique_id: value.device_unique_id.to_string(),
            location_code: value.location_code,
        }
    }
}

impl From<upvpn_types::upvpn_server::EndSession> for crate::proto::EndSession {
    fn from(value: upvpn_types::upvpn_server::EndSession) -> Self {
        Self {
            request_id: value.request_id.to_string(),
            device_unique_id: value.device_unique_id.to_string(),
            vpn_session_uuid: value.vpn_session_uuid.to_string(),
            reason: value.reason,
        }
    }
}

impl From<upvpn_types::upvpn_server::ClientConnected> for crate::proto::ClientConnected {
    fn from(value: upvpn_types::upvpn_server::ClientConnected) -> Self {
        Self {
            request_id: value.request_id.to_string(),
            device_unique_id: value.device_unique_id.to_string(),
            vpn_session_uuid: value.vpn_session_uuid.to_string(),
        }
    }
}

impl From<upvpn_types::upvpn_server::VpnSessionStatusRequest>
    for crate::proto::VpnSessionStatusRequest
{
    fn from(value: upvpn_types::upvpn_server::VpnSessionStatusRequest) -> Self {
        Self {
            request_id: value.request_id.to_string(),
            device_unique_id: value.device_unique_id.to_string(),
            vpn_session_uuid: value.vpn_session_uuid.to_string(),
        }
    }
}
