package app.upvpn.upvpn.service.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import android.os.Messenger
import android.util.Log
import app.upvpn.upvpn.service.VPNService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class VPNServiceConnectionManager(private val context: Context) {
    private val tag = "VPNServiceConnectionManager"
    private var bindCalled = false

    private var vpnServiceClient = MutableStateFlow<VPNServiceClient?>(null)
    val vpnServiceClientFlow = vpnServiceClient.asStateFlow()

    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            Log.i(tag, "onServiceConnected: componentName: $componentName, binder: $binder")
            vpnServiceClient.value = VPNServiceClient(Messenger(binder), Looper.getMainLooper())
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.i(tag, "onServiceDisconnected: componentName: $componentName")
            vpnServiceClient.value?.cleanup()
            vpnServiceClient.value = null
            bindCalled = false
        }

        override fun onNullBinding(name: ComponentName?) {
            Log.i(tag, "onNullBinding: componentName: $name")
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.i(tag, "onBindingDied: $name")
            super.onBindingDied(name)
        }
    }

    fun bind() {
        synchronized(this) {
            if (bindCalled.not() && vpnServiceClient.value == null) {
                val intent = Intent(context, VPNService::class.java)
                context.startService(intent)
                context.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT)
                bindCalled = true
            }
        }
    }

    fun unbind() {
        synchronized(this) {
            if (bindCalled || vpnServiceClient.value != null) {
                vpnServiceClient.value?.cleanup()
                context.unbindService(serviceConnection)
                vpnServiceClient.value = null
                bindCalled = false
            }
        }
    }

    fun vpnManager(): VPNManager? = vpnServiceClient.value?.vpnManager

    fun vpnInAppNotificationManager(): VPNInAppNotificationManager? =
        vpnServiceClient.value?.vpnInAppNotificationManager

}
