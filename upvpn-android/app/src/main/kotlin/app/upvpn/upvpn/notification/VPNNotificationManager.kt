package app.upvpn.upvpn.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

class VPNNotificationManager(val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)


    private fun hasPermission(): Boolean {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun updateVpnSessionNotification(notification: Notification) {
        if (hasPermission()) {
            notificationManager.notify(SESSION_NOTIFICATION_ID, notification)
        }
    }

    fun createChannels() {
        val vpnSessionChannel = NotificationChannelCompat.Builder(
            SESSION_CH_ID,
            NotificationManagerCompat.IMPORTANCE_LOW // no sound
        )
            .setName(SESSION_CH_NAME)
            .setDescription(SESSION_CH_DESC)
            .setVibrationEnabled(false)
            .setShowBadge(false)
            .build()

        notificationManager.createNotificationChannel(vpnSessionChannel)
    }

    companion object {
        const val SESSION_NOTIFICATION_ID = 7133

        const val SESSION_CH_ID = "7133"
        private const val SESSION_CH_NAME = "VPN Session Status"
        private const val SESSION_CH_DESC = "Get updates about current VPN session and its progress"

    }

}
