package app.upvpn.upvpn.service

import android.os.Parcelable
import android.os.SystemClock
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.ServerReady
import app.upvpn.upvpn.model.VpnSessionStatus
import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
sealed class VPNState : Parcelable {
    data object Disconnected : VPNState()
    data class Requesting(val location: Location) : VPNState()
    data class Accepted(val location: Location) : VPNState()
    data class ServerCreated(val location: Location) : VPNState()
    data class ServerRunning(val location: Location) : VPNState()
    data class ServerReady(val location: Location) : VPNState()
    data class Connecting(val location: Location) : VPNState()
    data class Connected(val location: Location, val time: Long) : VPNState()
    data class Disconnecting(val location: Location) : VPNState()
}

fun VPNState.progress(): Int {
    return when (this) {
        is VPNState.Requesting -> 10
        is VPNState.Accepted -> 25
        is VPNState.ServerCreated -> 50
        is VPNState.ServerRunning -> 75
        is VPNState.ServerReady -> 80
        is VPNState.Connecting -> 95
        is VPNState.Connected -> 100
        is VPNState.Disconnecting,
        is VPNState.Disconnected -> 0
    }
}

sealed class VPNOrchestratorState {
    data object Disconnected : VPNOrchestratorState()
    data class Requesting(val requestId: UUID, val location: Location) : VPNOrchestratorState()
    data class Accepted(
        val location: Location,
        val accepted: app.upvpn.upvpn.model.Accepted,
        val interFace: Interface
    ) :
        VPNOrchestratorState()

    data class ServerCreated(
        val location: Location,
        val serverCreated: app.upvpn.upvpn.model.ServerCreated,
        val interFace: Interface
    ) : VPNOrchestratorState()

    data class ServerRunning(
        val location: Location,
        val serverRunning: app.upvpn.upvpn.model.ServerRunning,
        val interFace: Interface
    ) : VPNOrchestratorState()

    data class ServerReady(
        val location: Location,
        val serverReady: app.upvpn.upvpn.model.ServerReady,
        val config: Config
    ) : VPNOrchestratorState()

    data class Connecting(
        val location: Location,
        val serverReady: app.upvpn.upvpn.model.ServerReady,
        val config: Config
    ) : VPNOrchestratorState()

    data class Connected(
        val location: Location,
        val time: Long,
        val serverReady: app.upvpn.upvpn.model.ServerReady,
        val config: Config
    ) : VPNOrchestratorState()

    data class Disconnecting(val location: Location) : VPNOrchestratorState()

    fun isDisconnected(): Boolean = this is VPNOrchestratorState.Disconnected

    fun isServerReady(): Boolean = this is VPNOrchestratorState.ServerReady

    fun isDisconnectingOrDisconnected(): Boolean = when (this) {
        is VPNOrchestratorState.Disconnected,
        is VPNOrchestratorState.Disconnecting
        -> true

        else -> false
    }

    fun toVPNState(): VPNState {
        return when (this) {
            is Disconnected -> VPNState.Disconnected
            is Requesting -> VPNState.Requesting(this.location)
            is Accepted -> VPNState.Accepted(this.location)
            is ServerCreated -> VPNState.ServerCreated(this.location)
            is ServerRunning -> VPNState.ServerRunning(this.location)
            is ServerReady -> VPNState.ServerReady(this.location)
            is Connecting -> VPNState.Connecting(this.location)
            is Connected -> VPNState.Connected(this.location, this.time)
            is Disconnecting -> VPNState.Disconnecting(this.location)
        }
    }

    // return triple (new orchestrator state, should turn off VPN, request ID of vpn session)
    fun transitionOnDisconnect(): Triple<VPNOrchestratorState, Boolean, UUID?> {
        return when (this) {
            is Disconnected -> Triple(Disconnected, false, null)
            is Requesting -> Triple(Disconnected, false, this.requestId)
            is Accepted -> Triple(Disconnected, false, this.accepted.requestId)
            is ServerCreated -> Triple(Disconnected, false, this.serverCreated.requestId)
            is ServerRunning -> Triple(Disconnected, false, this.serverRunning.requestId)
            is ServerReady -> Triple(Disconnected, false, this.serverReady.requestId)
            // duplicate onDisconnect (previous disconnect would already be in progress)
            is Disconnecting -> Triple(Disconnecting(this.location), false, null)
            is Connecting -> Triple(Disconnecting(this.location), true, this.serverReady.requestId)
            is Connected -> Triple(Disconnecting(this.location), true, this.serverReady.requestId)
        }
    }

    // move state machine of orchestrator state forward only
    fun newStateFromUpdate(
        location: Location,
        status: VpnSessionStatus
    ): VPNOrchestratorState? {
        return when (status) {
            is VpnSessionStatus.Accepted -> {
                when (this is VPNOrchestratorState.Accepted) {
                    true -> this // no update
                    else -> null
                }
            }

            is VpnSessionStatus.Failed -> Disconnected
            is VpnSessionStatus.ServerCreated -> {
                when (this) {
                    is VPNOrchestratorState.Accepted -> ServerCreated(
                        location,
                        status.content,
                        this.interFace
                    )

                    is VPNOrchestratorState.ServerCreated -> this // no update
                    else -> null
                }
            }

            is VpnSessionStatus.ServerRunning -> {
                when (this) {
                    is Accepted -> ServerRunning(location, status.content, this.interFace)
                    is ServerCreated -> ServerRunning(location, status.content, this.interFace)
                    is ServerRunning -> this // no update
                    else -> null
                }
            }

            is VpnSessionStatus.ServerReady -> {
                when (this) {
                    is Accepted -> {
                        ServerReady(
                            location,
                            status.content,
                            (this.interFace to status.content).toConfig()
                        )
                    }

                    is ServerCreated -> {
                        ServerReady(
                            location,
                            status.content,
                            (this.interFace to status.content).toConfig()
                        )
                    }

                    is ServerRunning -> {
                        ServerReady(
                            location,
                            status.content,
                            (this.interFace to status.content).toConfig()
                        )
                    }

                    is ServerReady -> this // no update
                    else -> null
                }
            }

            else -> null
        }
    }

}

fun Pair<Interface, ServerReady>.toConfig(): Config {
    val (interFace, serverReady) = this
    val peer = Peer.Builder()
        .parseEndpoint(serverReady.ipv4Endpoint)
        .parsePublicKey(serverReady.publicKey)
        .parseAllowedIPs("0.0.0.0/0, ::0/0")
        .setPersistentKeepalive(25)
        .build()

    return Config.Builder()
        .setInterface(interFace)
        .addPeer(peer)
        .build()
}


fun VPNOrchestratorState.ServerReady.toConnecting(): VPNOrchestratorState.Connecting {
    return VPNOrchestratorState.Connecting(this.location, this.serverReady, this.config)
}

fun VPNOrchestratorState.Connecting.toConnected(): VPNOrchestratorState.Connected {
    return VPNOrchestratorState.Connected(
        this.location,
        SystemClock.elapsedRealtime(),
        this.serverReady,
        this.config
    )
}
