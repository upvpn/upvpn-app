package app.upvpn.upvpn.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import app.upvpn.upvpn.MainActivity
import app.upvpn.upvpn.R
import app.upvpn.upvpn.service.VPNState
import app.upvpn.upvpn.service.progress

object VPNStateNotification {

    fun VPNState.toNotification(context: Context): Notification {
        return when (this) {
            is VPNState.Requesting -> this.notification(context)
            is VPNState.Accepted -> this.notification(context)
            is VPNState.ServerCreated -> this.notification(context)
            is VPNState.ServerRunning -> this.notification(context)
            is VPNState.ServerReady -> this.notification(context)
            is VPNState.Connecting -> this.notification(context)
            is VPNState.Connected -> this.notification(context)
            is VPNState.Disconnecting -> this.notification(context)
            is VPNState.Disconnected -> this.notification(context)
        }
    }

    private fun pendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    private fun activityPendingIntent(context: Context): PendingIntent {
        val intent = Intent().apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = Intent.ACTION_MAIN
        }
        return PendingIntent.getActivity(context, 1, intent, pendingIntentFlags())
    }

    private fun buildNotification(
        context: Context,
        contentTitle: String,
        contentText: String,
        progress: Int? = null,
        @DrawableRes resourceId: Int? = null,
    ): Notification {
        val builder = NotificationCompat.Builder(context, VPNNotificationManager.SESSION_CH_ID)
            .setSmallIcon(R.drawable.upvpn)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(activityPendingIntent(context))

        progress?.let { builder.setProgress(100, it, false) }

        resourceId?.let {

            val drawable = context.resources.getDrawable(resourceId, null)

            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            builder.setLargeIcon(bitmap)
        }

        return builder.build()
    }

    fun VPNState.Disconnected.notification(context: Context): Notification {
        return buildNotification(context, "Status", "VPN is off", null, R.drawable.vpn_off)
    }

    fun VPNState.Requesting.notification(context: Context): Notification {
        return buildNotification(
            context,
            this.location.city.uppercase(),
            "Requesting VPN Session",
            this.progress(),
            R.drawable.vpn_off
        )
    }

    fun VPNState.Accepted.notification(context: Context): Notification {
        return buildNotification(
            context,
            this.location.city.uppercase(),
            "VPN Session Accepted",
            this.progress(),
            R.drawable.vpn_off
        )
    }

    fun VPNState.ServerCreated.notification(context: Context): Notification {
        return buildNotification(
            context,
            this.location.city.uppercase(),
            "VPN Server Created",
            this.progress(),
            R.drawable.vpn_off
        )
    }

    fun VPNState.ServerRunning.notification(context: Context): Notification {
        return buildNotification(
            context,
            this.location.city.uppercase(),
            "VPN Server Running",
            this.progress(),
            R.drawable.vpn_off
        )
    }

    fun VPNState.ServerReady.notification(context: Context): Notification {
        return buildNotification(
            context,
            this.location.city.uppercase(),
            "VPN Server Ready",
            this.progress(),
            R.drawable.vpn_off
        )
    }

    fun VPNState.Connecting.notification(context: Context): Notification {
        return buildNotification(
            context,
            this.location.city.uppercase(),
            "VPN Connecting",
            this.progress(),
            R.drawable.vpn_off
        )
    }

    fun VPNState.Connected.notification(context: Context): Notification {
        return buildNotification(
            context,
            this.location.city.uppercase(),
            "VPN is on",
            null,
            R.drawable.vpn_on
        )
    }

    fun VPNState.Disconnecting.notification(context: Context): Notification {
        return buildNotification(
            context,
            this.location.city.uppercase(),
            "VPN Disconnecting",
            null,
            R.drawable.vpn_off
        )
    }
}
