package app.upvpn.upvpn.service

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.system.OsConstants
import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.wireguard.android.util.SharedLibraryLoader
import com.wireguard.config.Config

class VPNService : android.net.VpnService() {
    private val tag = "VPNService"

    private lateinit var vpnOrchestrator: VPNOrchestrator

    private var currentTunnelHandle = -1

    init {
        SharedLibraryLoader.loadSharedLibrary(this, "wg-go")
        Log.i(tag, "WireGuard version: ${wgVersion()}")
    }

    override fun onLowMemory() {
        Log.d(tag, "onLowMemory")
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        Log.d(tag, "onTrimMemory level: $level")
        super.onTrimMemory(level)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate")
        vpnOrchestrator = VPNOrchestrator(this, Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand intent: $intent, flags: $flags, startId: $startId")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        vpnOrchestrator.onDestroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(tag, "onBind intent: $intent")
        return super.onBind(intent) ?: vpnOrchestrator.messenger.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(tag, "onUnbind intent: $intent")
        return false
    }

    override fun onRebind(intent: Intent?) {
        Log.d(tag, "onRebind intent: $intent")
        super.onRebind(intent)

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(tag, "onTaskRemoved rootIntent: $rootIntent")
        super.onTaskRemoved(rootIntent)
    }

    // Calls to this method may not happen on the main thread of the process.
    override fun onRevoke() {
        Log.d(tag, "onRevoke")
        vpnOrchestrator.onRevoke()
    }

    private fun Config.toBuilder(): Builder {
        val builder = Builder()

        builder.setSession(NAME)
        builder.setMtu(this.`interface`.mtu.orElse(1280))
        builder.allowFamily(OsConstants.AF_INET)
        builder.allowFamily(OsConstants.AF_INET6)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) builder.setMetered(false)
        builder.setUnderlyingNetworks(null)
        builder.setBlocking(true)


        // add interface addresses
        this.`interface`.addresses.forEach {
            builder.addAddress(it.address, it.mask)
        }
        // add VPN routes
        this.peers.forEach { peer ->
            peer.allowedIps.forEach { allowedIp ->
                builder.addRoute(allowedIp.address, allowedIp.mask)
            }
        }
        // DNS Servers
        this.`interface`.dnsServers.forEach { dnsServer ->
            builder.addDnsServer(dnsServer)
        }

        return builder
    }

    fun turnOnVPN(config: Config): Result<Unit, String> {
        val builder = config.toBuilder()

        builder.establish().use { tun ->
            if (tun == null) {
                val msg = "Failed to create VPN interface"
                Log.e(tag, msg)
                return Err(msg)
            }

            currentTunnelHandle = wgTurnOn(NAME, tun.detachFd(), config.toWgUserspaceString())
            Log.i(tag, "currentTunnelHandle: $currentTunnelHandle")
        }

        if (currentTunnelHandle < 0) {
            val msg = "WireGuard activation error: $currentTunnelHandle"
            Log.e(tag, msg)
            return Err(msg)
        }

        protect(wgGetSocketV4(currentTunnelHandle))
        protect(wgGetSocketV6(currentTunnelHandle))

        return Ok(Unit)
    }

    fun turnOffVPN() {
        if (currentTunnelHandle != -1) {
            wgTurnOff(currentTunnelHandle)
            currentTunnelHandle = -1
        }
    }

    companion object {

        const val NAME = "upvpn"

        @JvmStatic
        private external fun wgTurnOn(ifName: String, tunFd: Int, settings: String): Int

        @JvmStatic
        private external fun wgTurnOff(handle: Int)

        @JvmStatic
        private external fun wgGetSocketV4(handle: Int): Int


        @JvmStatic
        private external fun wgGetSocketV6(handle: Int): Int

        @JvmStatic
        private external fun wgGetConfig(handle: Int): String?

        @JvmStatic
        private external fun wgVersion(): String?

    }
}
