package com.example.quietfeed.app

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.quietfeed.MainActivity
import com.example.quietfeed.R
import com.example.quietfeed.data.sync.ChannelUploads

private const val MAX_INBOX_LINES: Int = 5
private const val NOTIFICATION_ID: Int = 1

private const val CHANNEL_ID: String = "new_videos"

class NewVideoNotifier(private val context: Context) {
    companion object {
        const val EXTRA_CHANNEL_ID: String = "com.example.quietfeed.extra.CHANNEL_ID"
    }

    private val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)

    fun cancel(channelId: String) = manager.cancel(channelId, NOTIFICATION_ID)

    fun notify(uploads: List<ChannelUploads>) {
        if (uploads.isEmpty() || !canNotify()) return
        createChannel()
        uploads.forEach { upload -> post(upload) }
    }

    private fun canNotify(): Boolean {
        val isPermitted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return isPermitted && manager.areNotificationsEnabled()
    }

    private fun createChannel() {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setDescription(context.getString(R.string.notification_channel_description))
            .setName(context.getString(R.string.notification_channel_name))
            .build()
        manager.createNotificationChannel(channel)
    }

    private fun post(upload: ChannelUploads) {
        val count = upload.titles.size
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setColor(ContextCompat.getColor(context, R.color.brand_navy))
            .setContentIntent(openChannelIntent(upload.channelId))
            .setContentTitle(upload.channelName)
            .setSmallIcon(R.drawable.ic_notification)
        if (count == 1) {
            builder.setContentText(upload.titles.first())
        } else {
            val style = NotificationCompat.InboxStyle()
            upload.titles.take(MAX_INBOX_LINES).forEach { title -> style.addLine(title) }
            builder.setContentText(context.resources.getQuantityString(R.plurals.notification_new_videos, count, count)).setNumber(count).setStyle(style)
        }
        try {
            manager.notify(upload.channelId, NOTIFICATION_ID, builder.build())
        } catch (exception: SecurityException) {
            return
        }
    }

    private fun openChannelIntent(channelId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).putExtra(EXTRA_CHANNEL_ID, channelId).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(context, channelId.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
