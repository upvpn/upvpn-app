impl From<crate::proto::SignInRequest> for upvpn_types::upvpn_server::UserCredentials {
    fn from(value: crate::proto::SignInRequest) -> Self {
        Self {
            email: value.email,
            password: value.password,
        }
    }
}
