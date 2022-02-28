package com.bandyer.video_android_glass_ui.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.bandyer.video_android_core_ui.R
import com.bandyer.video_android_glass_ui.GlassUIProvider
import com.bandyer.video_android_glass_ui.NotificationReceiver
import com.bandyer.video_android_glass_ui.utils.extensions.PendingIntentExtensions

object NotificationHelper {

    private const val NOTIFICATION_DEFAULT_CHANNEL_ID =
        "com.bandyer.video_android_glass_ui.notification_channel_default"
    private const val NOTIFICATION_IMPORTANT_CHANNEL_ID =
        "com.bandyer.video_android_glass_ui.notification_channel_important"

    fun buildIncomingCallNotification(context: Context, usersDescription: String, isFullScreen: Boolean = true): Notification {
        val contextText = context.getString(R.string.bandyer_notification_incoming_call)
        val fullScreenIntent = if(isFullScreen) GlassUIProvider.createCallPendingIntent(context.applicationContext) else null
        val answerAction = NotificationCompat.Action(
            R.drawable.bandyer_z_audio_only,
            context.getString(R.string.bandyer_notification_answer),
            createBroadcastPendingIntent(context, NotificationReceiver.ACTION_ANSWER)
        )
        val declineAction = NotificationCompat.Action(
            R.drawable.bandyer_z_end_call,
            context.getString(R.string.bandyer_notification_decline),
            createBroadcastPendingIntent(context, NotificationReceiver.ACTION_HANGUP)
        )

        return context.buildNotification(
            usersDescription = usersDescription,
            channelId = NOTIFICATION_IMPORTANT_CHANNEL_ID,
            channelName = context.getString(R.string.bandyer_notification_incoming_call),
            isHighPriority = true,
            contentText = contextText,
            contentIntent = fullScreenIntent,
            fullscreenIntent = fullScreenIntent,
            actions = listOf(answerAction, declineAction)
        )
    }

    fun buildOngoingCallNotification(context: Context, usersDescription: String): Notification {
        val contentIntent = GlassUIProvider.createCallPendingIntent(context.applicationContext)
        val contextText = context.getString(R.string.bandyer_notification_ongoing_call)
        val hangUpAction = NotificationCompat.Action(
            R.drawable.bandyer_z_end_call,
            context.getString(R.string.bandyer_notification_hangup),
            createBroadcastPendingIntent(context, NotificationReceiver.ACTION_HANGUP)
        )

        return context.buildNotification(
            usersDescription = usersDescription,
            channelId = NOTIFICATION_DEFAULT_CHANNEL_ID,
            channelName = context.getString(R.string.bandyer_notification_ongoing_call),
            isHighPriority = false,
            contentText = contextText,
            contentIntent = contentIntent,
            actions = listOf(hangUpAction)
        )
    }

    private fun createBroadcastPendingIntent(context: Context, action: String) =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, NotificationReceiver::class.java).apply {
                this.action = action
            },
            PendingIntentExtensions.updateFlags
        )

    private fun Context.buildNotification(
        usersDescription: String,
        channelId: String,
        channelName: String,
        isHighPriority: Boolean,
        contentText: String,
        contentIntent: PendingIntent? = null,
        fullscreenIntent: PendingIntent? = null,
        actions: List<NotificationCompat.Action> = listOf()
    ): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(channelId, channelName, isHighPriority)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setOngoing(true)
            .setSmallIcon(R.drawable.bandyer_z_audio_only)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentText(contentText)
            .setContentTitle(usersDescription)

        contentIntent?.also { builder.setContentIntent(it) }
        fullscreenIntent?.also { builder.setFullScreenIntent(it, true) }
        actions.forEach { builder.addAction(it) }

        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Context.createNotificationChannel(
        channelId: String,
        channelName: String,
        isHighImportance: Boolean
    ) {
        val notificationManager =
            getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            if (isHighImportance) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(notificationChannel)
    }
}