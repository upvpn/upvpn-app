package app.upvpn.upvpn.service.client

import android.os.Messenger
import android.util.Log
import app.upvpn.upvpn.BuildConfig
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.service.VPNState
import app.upvpn.upvpn.service.ipc.Event
import app.upvpn.upvpn.service.ipc.MessageHandler
import app.upvpn.upvpn.service.ipc.Request
import app.upvpn.upvpn.service.ipc.extensions.sendRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class VPNManager(
    private val vpnServiceMessenger: Messenger,
    private val eventHandler: MessageHandler<Event>
) {
    private val tag = "VPNManager"
    private val vpnState = MutableStateFlow<VPNState?>(null)

    val vpnStateFlow = vpnState.asStateFlow()

    init {
        eventHandler.registerHandler(Event.VpnState::class) {
            onVpnStateUpdate(it.vpnState)
        }
    }

    private fun onVpnStateUpdate(newVPNState: VPNState) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, "Received $newVPNState")
        }
        vpnState.value = newVPNState
    }

    fun connect(location: Location) {
        vpnServiceMessenger.sendRequest(Request.Connect(location))
    }

    fun disconnect() {
        vpnServiceMessenger.sendRequest(Request.Disconnect)
    }
}
