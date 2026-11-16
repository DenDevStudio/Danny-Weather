package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.R
import com.example.ui.i18n.AppStrings

object UpdateNotificationHelper {

    private const val CHANNEL_ID = "danny_weather_updates"
    private const val NOTIFICATION_ID = 2104

    fun playMarketWebUrl(packageName: String = BuildConfig.APPLICATION_ID): String =
        "https://play.google.com/store/apps/details?id=$packageName"

    fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showAccountUpdateNotification(
        context: Context,
        strings: AppStrings,
        targetVersion: String
    ) {
        if (!canPostNotifications(context)) return

        val appContext = context.applicationContext
        ensureChannel(appContext, strings)

        val playUrl = playMarketWebUrl()
        val openStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            NOTIFICATION_ID,
            openStoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val versionLabel = targetVersion.trim().ifBlank { BuildConfig.VERSION_NAME }
        val title = "${strings.accountUpdateTitle} (v$versionLabel)"
        val bodyText = "${strings.accountUpdateBody}\n${strings.playMarketLinkLabel}: $playUrl"

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_update_notification)
            .setContentTitle(title)
            .setContentText(strings.accountUpdateSubtitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                strings.openPlayMarketButton,
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Ignored gracefully if notification permission was revoked
        }
    }

    private fun ensureChannel(context: Context, strings: AppStrings) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            strings.cloudUpdatesTitle,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = strings.accountUpdateSubtitle
        }
        manager.createNotificationChannel(channel)
    }
}
