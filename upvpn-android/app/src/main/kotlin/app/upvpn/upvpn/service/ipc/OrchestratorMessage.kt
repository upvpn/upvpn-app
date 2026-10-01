package app.upvpn.upvpn.service.ipc

import android.os.Parcelable
import app.upvpn.upvpn.model.Accepted
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.VpnSessionStatus
import com.github.michaelbull.result.Result
import com.wireguard.config.Interface
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.UUID

@Parcelize
sealed class OrchestratorMessage() : Parcelable {
    // requestId identifies the connect request a message was produced for
    data class ConnectResponse(
        val requestId: UUID,
        val location: Location,
        val result: @RawValue Result<Pair<Accepted, Interface>, String>
    ) : OrchestratorMessage()

    // isUnauthorized when status is Failed because session status could not be
    // fetched due to authorization
    data class VpnSessionUpdate(
        val requestId: UUID,
        val location: Location,
        val status: @RawValue VpnSessionStatus,
        val isUnauthorized: Boolean
    ) : OrchestratorMessage()

    data object GetAndPublishWGConfig : OrchestratorMessage()
}
