use crate::{datetime_utc_to_timestamp, timestamp_to_datetime_utc};

impl From<upvpn_types::vpn_session::VpnStatus> for crate::proto::VpnStatus {
    fn from(value: upvpn_types::vpn_session::VpnStatus) -> Self {
        match value {
            upvpn_types::vpn_session::VpnStatus::Accepted(location) => crate::proto::VpnStatus {
                vpn_status: Some(crate::proto::vpn_status::VpnStatus::Accepted(
                    crate::proto::vpn_status::Accepted {
                        location: Some(location.into()),
                    },
                )),
            },
            upvpn_types::vpn_session::VpnStatus::Connecting(location) => crate::proto::VpnStatus {
                vpn_status: Some(crate::proto::vpn_status::VpnStatus::Connecting(
                    crate::proto::vpn_status::Connecting {
                        location: Some(location.into()),
                    },
                )),
            },
            upvpn_types::vpn_session::VpnStatus::ServerRunning(location) => {
                crate::proto::VpnStatus {
                    vpn_status: Some(crate::proto::vpn_status::VpnStatus::ServerRunning(
                        crate::proto::vpn_status::ServerRunning {
                            location: Some(location.into()),
                        },
                    )),
                }
            }
            upvpn_types::vpn_session::VpnStatus::ServerReady(location) => crate::proto::VpnStatus {
                vpn_status: Some(crate::proto::vpn_status::VpnStatus::ServerReady(
                    crate::proto::vpn_status::ServerReady {
                        location: Some(location.into()),
                    },
                )),
            },
            upvpn_types::vpn_session::VpnStatus::Connected(location, connected_time) => {
                crate::proto::VpnStatus {
                    vpn_status: Some(crate::proto::vpn_status::VpnStatus::Connected(
                        crate::proto::vpn_status::Connected {
                            location: Some(location.into()),
                            timestamp: Some(datetime_utc_to_timestamp(connected_time)),
                        },
                    )),
                }
            }
            upvpn_types::vpn_session::VpnStatus::Disconnecting(location) => {
                crate::proto::VpnStatus {
                    vpn_status: Some(crate::proto::vpn_status::VpnStatus::Disconnecting(
                        crate::proto::vpn_status::Disconnecting {
                            location: Some(location.into()),
                        },
                    )),
                }
            }
            upvpn_types::vpn_session::VpnStatus::Disconnected => crate::proto::VpnStatus {
                vpn_status: Some(crate::proto::vpn_status::VpnStatus::Disconnected(
                    crate::proto::vpn_status::Disconnected {},
                )),
            },
            upvpn_types::vpn_session::VpnStatus::ServerCreated(location) => {
                crate::proto::VpnStatus {
                    vpn_status: Some(crate::proto::vpn_status::VpnStatus::ServerCreated(
                        crate::proto::vpn_status::ServerCreated {
                            location: Some(location.into()),
                        },
                    )),
                }
            }
        }
    }
}

impl From<crate::proto::VpnStatus> for upvpn_types::vpn_session::VpnStatus {
    fn from(value: crate::proto::VpnStatus) -> Self {
        let vpn_status = value.vpn_status.unwrap();
        match vpn_status {
            crate::proto::vpn_status::VpnStatus::Accepted(accepted) => {
                upvpn_types::vpn_session::VpnStatus::Accepted(accepted.location.unwrap().into())
            }
            crate::proto::vpn_status::VpnStatus::Connecting(connecting) => {
                upvpn_types::vpn_session::VpnStatus::Connecting(connecting.location.unwrap().into())
            }
            crate::proto::vpn_status::VpnStatus::ServerRunning(srun) => {
                upvpn_types::vpn_session::VpnStatus::ServerRunning(srun.location.unwrap().into())
            }
            crate::proto::vpn_status::VpnStatus::ServerReady(sr) => {
                upvpn_types::vpn_session::VpnStatus::ServerReady(sr.location.unwrap().into())
            }
            crate::proto::vpn_status::VpnStatus::Connected(connected) => {
                upvpn_types::vpn_session::VpnStatus::Connected(
                    connected.location.unwrap().into(),
                    timestamp_to_datetime_utc(connected.timestamp).unwrap(),
                )
            }
            crate::proto::vpn_status::VpnStatus::Disconnecting(disconnecting) => {
                upvpn_types::vpn_session::VpnStatus::Disconnecting(
                    disconnecting.location.unwrap().into(),
                )
            }
            crate::proto::vpn_status::VpnStatus::Disconnected(_) => {
                upvpn_types::vpn_session::VpnStatus::Disconnected
            }
            crate::proto::vpn_status::VpnStatus::ServerCreated(server_created) => {
                upvpn_types::vpn_session::VpnStatus::ServerCreated(
                    server_created.location.unwrap().into(),
                )
            }
        }
    }
}
