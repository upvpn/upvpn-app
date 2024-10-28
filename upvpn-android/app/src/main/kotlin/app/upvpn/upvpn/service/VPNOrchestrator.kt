package app.upvpn.upvpn.service

import android.net.VpnService
import android.os.Looper
import android.os.Messenger
import android.util.Log
import app.upvpn.upvpn.BuildConfig
import app.upvpn.upvpn.data.AppContainer
import app.upvpn.upvpn.data.DefaultAppContainer
import app.upvpn.upvpn.model.Accepted
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.VPNNotification
import app.upvpn.upvpn.model.VpnSessionStatus
import app.upvpn.upvpn.notification.VPNNotificationManager
import app.upvpn.upvpn.notification.VPNStateNotification.toNotification
import app.upvpn.upvpn.service.ipc.Event
import app.upvpn.upvpn.service.ipc.MessageHandler
import app.upvpn.upvpn.service.ipc.OrchestratorMessage
import app.upvpn.upvpn.service.ipc.Request
import app.upvpn.upvpn.service.ipc.extensions.sendEvent
import app.upvpn.upvpn.service.ipc.extensions.toMessage
import app.upvpn.upvpn.service.ipc.extensions.toOrchestratorMessage
import app.upvpn.upvpn.service.ipc.extensions.toRequest
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.onFailure
import com.wireguard.config.Interface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class VPNOrchestrator(
    private val vpnService: VPNService,
    private val looper: Looper,
    private val appContainer: AppContainer = DefaultAppContainer(vpnService)
) {

    private val tag = "VPNOrchestrator"
    private var vpnOrchestratorState: VPNOrchestratorState = VPNOrchestratorState.Disconnected
    private val messageHandler = MessageHandler(looper) { msg -> msg.toRequest() }
    private val orchestratorMessageHandler =
        MessageHandler(looper) { msg -> msg.toOrchestratorMessage() }
    private var listeners = mutableMapOf<Int, Messenger>()
    private var listenerId = 0
    private val vpnSessionScope = CoroutineScope(SupervisorJob())
    private val vpnSessionRepository = appContainer.vpnSessionRepository
    private val endVpnSessionScope = appContainer.appScope
    private val vpnNotificationManager = appContainer.vpnNotificationManager
    private var wgConfigJob: Job? = null

    init {
        endVpnSessionScope.launch {
            vpnSessionRepository.runReclaimer(endVpnSessionScope)
        }
    }

    private fun getListenerId(): Int {
        synchronized(this) {
            listenerId++
            return listenerId
        }
    }

    init {
        messageHandler.registerHandler(Request.RegisterListener::class) { request ->
            onRegisterListener(request.messenger)
        }
        messageHandler.registerHandler(Request.DeregisterListener::class) { request ->
            onDeregisterListener(request.id)
        }
        messageHandler.registerHandler(Request.Connect::class) { request ->
            onConnect(request.location)
        }
        messageHandler.registerHandler(Request.Disconnect::class) {
            onDisconnect("user requested")
        }

        orchestratorMessageHandler.registerHandler(OrchestratorMessage.ConnectResponse::class) {
            onConnectResponse(it.location, it.result)
        }
        orchestratorMessageHandler.registerHandler(OrchestratorMessage.VpnSessionUpdate::class) {
            onVpnSessionUpdate(it.location, it.status)
        }
        orchestratorMessageHandler.registerHandler(OrchestratorMessage.GetAndPublishWGConfig::class) {
            onGetAndPublishWGConfig()
        }
    }

    val messenger = Messenger(messageHandler)

    private fun sendOrchestratorMessage(msg: OrchestratorMessage) {
        synchronized(this) {
            orchestratorMessageHandler.sendMessage(msg.toMessage())
        }
    }

    private fun onConnect(location: Location) {
        // see if a session is in progress
        if (vpnOrchestratorState.isDisconnected().not()) {
            dispatchVpnNotification("VPN session is already in progress")
            Log.i(tag, "onConnect VPN session is already in progress: $vpnOrchestratorState")
            return
        }

        // here state is disconnected, first check if vpn permission is available
        // then start foreground service else return
        if (VpnService.prepare(vpnService) != null) {
            dispatchVpnNotification("No VPN permission")
            return
        } else {
            vpnService.startForeground(
                VPNNotificationManager.SESSION_NOTIFICATION_ID,
                vpnOrchestratorState.toVPNState().toNotification(vpnService)
            )
        }

        // update state & send event
        val requestId = UUID.randomUUID()
        updateStateAndNotifyClients(VPNOrchestratorState.Requesting(requestId, location))

        // create new vpn session
        // on success also start watcher
        vpnSessionRepository.newVpnSession(
            vpnSessionScope,
            requestId,
            location,
            onConnectResponseCallback = { result ->
                sendOrchestratorMessage(OrchestratorMessage.ConnectResponse(location, result))
            },
            onVpnSessionUpdateCallback = { status ->
                sendOrchestratorMessage(OrchestratorMessage.VpnSessionUpdate(location, status))
            }
        )
    }

    private fun onConnectResponse(
        location: Location,
        result: Result<Pair<Accepted, Interface>, String>
    ) {
        Log.i(tag, "onConnectResponse $result")

        // before connect response could arrive, vpn might already have been disconnected
        if (vpnOrchestratorState.isDisconnectingOrDisconnected()) {
            Log.i(tag, "no VPN session in progress, dropping on connect response $result")
            return
        }

        result.fold(
            success = {
                Log.i(tag, "onConnectResponse success $it")
                // update state & send event to clients
                updateStateAndNotifyClients(
                    VPNOrchestratorState.Accepted(
                        location,
                        it.first,
                        it.second
                    )
                )
            },
            failure = {
                Log.i(tag, "onConnectResponse failure $it")
                // notify in-app that error occurred
                dispatchVpnNotification(it)
                // update state & send event to clients
                updateStateAndNotifyClients(VPNOrchestratorState.Disconnected)
            }
        )
    }

    private fun onVpnSessionUpdate(location: Location, status: VpnSessionStatus) {
        Log.i(tag, "onVpnSessionUpdate $status")

        // before update could arrive, vpn might already have been disconnected
        if (vpnOrchestratorState.isDisconnectingOrDisconnected()) {
            Log.i(tag, "no VPN session in progress, dropping on onVpnSessionUpdate $status")
            return
        }

        // on Failed status send in-app notification
        if (status is VpnSessionStatus.Failed) {
            dispatchVpnNotification("Can't provision server. Please try again.")
        }

        // check for permission on every update
        if (disconnectAndNotifyIfNoVPNPermission("No VPN permission during onVpnSessionUpdate. Disconnecting")) {
            return
        }

        val newOrchestratorState = vpnOrchestratorState.newStateFromUpdate(location, status)

        if (newOrchestratorState != null) {
            // update state and notify clients
            updateStateAndNotifyClients(newOrchestratorState)

            when (newOrchestratorState) {
                is VPNOrchestratorState.ServerReady -> {
                    // Begin setting up VPN connection
                    turnOnVPN(newOrchestratorState)
                }

                else -> {}
            }
        }
    }

    private fun disconnectAndNotifyIfNoVPNPermission(contextMsg: String): Boolean {
        if (VpnService.prepare(vpnService) != null) {
            Log.i(tag, contextMsg)
            dispatchVpnNotification("No VPN permission")
            onDisconnect("no vpn perms")
            return true
        }
        return false
    }

    // at this point we have all the required info setup tunnel
    private fun turnOnVPN(state: VPNOrchestratorState.ServerReady) {
        Log.i(tag, "Turning VPN on")
        // check again that permission has not been revoked
        if (disconnectAndNotifyIfNoVPNPermission("No VPN permission just on onset of turnOnVPN. Disconnecting")) {
            return
        }

        // transition to connecting
        val connectingState = state.toConnecting()
        updateStateAndNotifyClients(connectingState)

        // setup tunnel
        // on failure end vpn session, set state to disconnected, send notification
        val result = vpnService.turnOnVPN(state.config)
        result.onFailure {
            // send in-app notification
            dispatchVpnNotification(it)
            onDisconnect(it)
            return
        }

        // transition to connected
        val connectedState = connectingState.toConnected()
        updateStateAndNotifyClients(connectedState)

        // start job to send wg config events to client
        startWgConfigJob()

        // TODO: send client connected call to backend
    }

    private fun turnOffVPN() {
        Log.i(tag, "Turning off VPN")

        // if there is any active tunnel turn it off
        vpnService.turnOffVPN()

        // set state to disconnected and notify clients
        updateStateAndNotifyClients(VPNOrchestratorState.Disconnected)
    }

    // called by user UI events or VpnService.onRevoke or disconnect from notification
    private fun onDisconnect(reason: String) {
        synchronized(this) {
            if (vpnOrchestratorState.isDisconnected()) {
                Log.i(tag, "Already disconnected")
                return
            }

            // stop wg config job
            stopWgConfigJob()

            // when disconnect is received before session was even ready and watcher is running
            // cancel those coroutines first
            vpnSessionScope.coroutineContext.cancelChildren()

            val (newOrchestratorState, shouldTurnOff, requestId) = vpnOrchestratorState.transitionOnDisconnect()
            updateStateAndNotifyClients(newOrchestratorState)

            // end vpn session on server
            requestId?.let {
                vpnSessionRepository.endVpnSession(endVpnSessionScope, it, reason)
            }

            // turn of VPN on device
            if (shouldTurnOff) {
                turnOffVPN()
            }

            // clear any client side config
            dispatchEvent(Event.WgConfig(null))

            // service may need to stop here because disconnect
            // can arrive from notification and without activity visible
            // or when vpn is revoked
            ifNoListenersAndDisconnectedThenStopService()
        }

        Log.i(tag, "onDisconnect DONE")
    }

    private fun updateStateAndNotifyClients(vpnOrchestratorState: VPNOrchestratorState) {
        this.vpnOrchestratorState = vpnOrchestratorState
        val vpnState = vpnOrchestratorState.toVPNState()
        vpnNotificationManager.updateVpnSessionNotification(vpnState.toNotification(vpnService))
        dispatchEvent(Event.VpnState(vpnState))
    }

    private fun cleanupIPC() {
        orchestratorMessageHandler.removeCallbacksAndMessages(null)
        messageHandler.removeCallbacksAndMessages(null)
    }

    private fun onGetAndPublishWGConfig() {
        if (listeners.isNotEmpty()) {
            val config = vpnService.getWgConfig()
            dispatchEvent(Event.WgConfig(config))
        }
    }

    private fun stopWgConfigJob() {
        synchronized(this) {
            wgConfigJob?.cancel()
            wgConfigJob = null
        }
    }

    private fun startWgConfigJob() {
        synchronized(this) {
            if (wgConfigJob == null) {
                wgConfigJob = vpnSessionScope.launch {
                    while (true) {
                        sendOrchestratorMessage(OrchestratorMessage.GetAndPublishWGConfig)
                        delay(1000)
                    }
                }
            }
        }
    }

    fun onDestroy() {
        synchronized(this) {
            cleanupIPC()
            onDisconnect("destroyed")
            // no need to stop service as it is already being destroyed
        }
    }

    fun onRevoke() {
        synchronized(this) {
            cleanupIPC()
            onDisconnect("revoked")
            // no need to stop service here as disconnect will take care of it
        }
    }

    private fun stopService() {
        Log.i(tag, "Stopping Service")
        vpnService.stopForeground(VpnService.STOP_FOREGROUND_REMOVE)
        vpnService.stopSelf()
    }

    private fun ifNoListenersAndDisconnectedThenStopService() {
        // if there are no more listeners and vpn is disconnected stop the service.
        // When any UI becomes visible it will start and bind to service again.
        if (vpnOrchestratorState.isDisconnected() && listeners.isEmpty()) {
            stopService()
        }
    }

    private fun onRegisterListener(listener: Messenger) {
        val id = getListenerId()
        listeners[id] = listener

        val events = listOf<Event>(
            Event.VpnState(vpnOrchestratorState.toVPNState()),
            Event.ListenerRegistered(id)
        )

        val allSent = events.all {
            listener.sendEvent(it)
        }

        if (allSent.not()) {
            listeners.remove(id)
        }
    }

    private fun onDeregisterListener(id: Int) {
        listeners.remove(id)
        Log.d(tag, "deregistered $id")

        // when Activity was is no longer visible (registered here), services can be stopped
        ifNoListenersAndDisconnectedThenStopService()
    }

    private fun dispatchVpnNotification(msg: String) {
        dispatchEvent(Event.VpnNotification(VPNNotification(msg)))
    }

    private fun dispatchEvent(event: Event) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, "dispatching: $event")
        }
        val deadListeners = mutableSetOf<Int>()
        for ((id, listener) in listeners) {
            if (listener.sendEvent(event).not()) {
                deadListeners.add(id)
            }
        }
        deadListeners.forEach { listeners.remove(it) }
    }
}
