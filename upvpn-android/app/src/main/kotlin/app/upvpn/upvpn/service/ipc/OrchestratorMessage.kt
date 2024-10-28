package app.upvpn.upvpn.service.ipc

import android.os.Parcelable
import app.upvpn.upvpn.model.Accepted
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.VpnSessionStatus
import com.github.michaelbull.result.Result
import com.wireguard.config.Interface
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
sealed class OrchestratorMessage() : Parcelable {
    data class ConnectResponse(
        val location: Location,
        val result: @RawValue Result<Pair<Accepted, Interface>, String>
    ) : OrchestratorMessage()

    data class VpnSessionUpdate(val location: Location, val status: @RawValue VpnSessionStatus) :
        OrchestratorMessage()

    data object GetAndPublishWGConfig : OrchestratorMessage()
}
