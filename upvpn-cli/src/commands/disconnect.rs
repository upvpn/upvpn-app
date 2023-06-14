use clap::Args;
use console::style;

use crate::cli::RunCommand;

use super::error::CliError;

#[derive(Debug, Args)]
pub struct Disconnect {}

#[async_trait::async_trait]
impl RunCommand for Disconnect {
    async fn run(self) -> Result<(), CliError> {
        let mut client = upvpn_controller::new_grpc_client()
            .await
            .map_err(|_| CliError::DaemonUnavailable)?;

        let vpn_status = client
            .disconnect_vpn(())
            .await
            .map(|res| res.into_inner())
            .map(upvpn_types::vpn_session::VpnStatus::from)?;

        println!("{}", style(vpn_status).yellow());

        Ok(())
    }
}
