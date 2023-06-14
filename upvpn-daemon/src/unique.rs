pub async fn already_running() -> bool {
    match upvpn_controller::new_grpc_client().await {
        Ok(_) => true,
        Err(e) => {
            tracing::info!(
                "cannot connect to GRPC controller({}), assuming none running",
                e.to_string()
            );
            false
        }
    }
}
