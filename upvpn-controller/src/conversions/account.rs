impl From<crate::proto::SignInRequest> for upvpn_types::upvpn_server::UserCredentials {
    fn from(value: crate::proto::SignInRequest) -> Self {
        Self {
            email: value.email,
            password: value.password,
        }
    }
}

impl From<upvpn_types::account::AccountInfo> for crate::proto::AccountInfo {
    fn from(value: upvpn_types::account::AccountInfo) -> Self {
        Self {
            email: value.email.unwrap_or_default(),
            token: value.token.unwrap_or_default(),
        }
    }
}
