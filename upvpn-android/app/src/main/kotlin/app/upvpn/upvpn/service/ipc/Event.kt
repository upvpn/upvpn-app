package app.upvpn.upvpn.service.ipc

import android.os.Parcelable
import app.upvpn.upvpn.model.VPNNotification
import app.upvpn.upvpn.service.VPNState
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Event : Parcelable {
    data class ListenerRegistered(val id: Int) : Event()
    data class VpnState(val vpnState: VPNState) : Event()
    data class VpnNotification(val notification: VPNNotification) : Event()
}
