use serde::Serialize;

#[derive(Debug, thiserror::Error, Serialize)]
#[serde(tag = "type")]
pub enum Error {
    #[error("daemon is offline")]
    DaemonIsOffline,
    #[error("Grpc code: {code}, message: {message}")]
    Grpc { code: u8, message: String },
    #[error("Google auth error: {message}")]
    GoogleAuthError { message: String },
    #[error("{message}")]
    Rest { status: u16, message: String },
    #[error("failed to open browser: {message}")]
    Opener { message: String },
}

impl From<upvpn_server::rest::RestError> for Error {
    fn from(value: upvpn_server::rest::RestError) -> Self {
        match value {
            upvpn_server::rest::RestError::Http(e) => Error::Rest {
                status: 0,
                message: format!("Could not reach server, please try again: {e}"),
            },
            upvpn_server::rest::RestError::Api { status, error } => Error::Rest {
                status,
                message: error.message,
            },
        }
    }
}

impl From<tonic::Status> for Error {
    fn from(value: tonic::Status) -> Self {
        Error::Grpc {
            code: value.code() as u8,
            message: value.message().to_owned(),
        }
    }
}
