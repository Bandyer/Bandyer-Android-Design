/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.extensions.ContextExtensions.isScreenOff
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationReceiver
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isScreenOff
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

// TODO change contentText for group calls
internal object NotificationHelper {

    private const val NOTIFICATION_DEFAULT_CHANNEL_ID =
        "com.kaleyra.collaboration_suite_glass_ui.notification_channel_default"
    private const val NOTIFICATION_IMPORTANT_CHANNEL_ID =
        "com.kaleyra.collaboration_suite_glass_ui.notification_channel_important"

    private const val FULL_SCREEN_REQUEST_CODE = 123
    private const val CONTENT_REQUEST_CODE = 456
    private const val ANSWER_REQUEST_CODE = 789
    private const val DECLINE_REQUEST_CODE = 987

    private const val IMAGE_SIZE = 500

    fun notify(notificationId: Int, notification: Notification) {
        NotificationManagerCompat.from(ContextRetainer.context).notify(
            notificationId, notification
        )
    }

    fun cancelNotification(notificationId: Int) {
        val notificationManager =
            ContextRetainer.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    fun <T> buildIncomingCallNotification(
        caller: String,
        image: Uri,
        activityClazz: Class<T>,
        isHighPriority: Boolean,
        onImageLoaded: (Notification) -> Unit
    ): Notification {
        val context = ContextRetainer.context

        if (isHighPriority && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            turnOnScreen(context)

        val builder = CallNotification
            .Builder(
                context = context,
                channelId = if (isHighPriority) NOTIFICATION_IMPORTANT_CHANNEL_ID else NOTIFICATION_DEFAULT_CHANNEL_ID,
                channelName = context.getString(R.string.kaleyra_notification_incoming_call),
                type = CallNotification.Type.INCOMING
            )
            .caller(caller)
            .image(BitmapFactory.decodeResource(context.resources, R.drawable.avatar))
            .importance(isHighPriority)
            .contentText(context.getString(R.string.kaleyra_notification_incoming_call))
            .contentIntent(contentPendingIntent(context, activityClazz))
            .fullscreenIntent(fullScreenPendingIntent(context, activityClazz))
            .answerIntent(answerPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        BitmapUtils.uriToBitmap(image, IMAGE_SIZE, IMAGE_SIZE, onSuccess = {
            it?.also { builder.image(it) }
            onImageLoaded.invoke(builder.build())
        }, onFailure = { })

        return builder.build()
    }

    fun <T> buildOutgoingCallNotification(
        caller: String,
        image: Uri,
        activityClazz: Class<T>,
        onImageLoaded: (Notification) -> Unit
    ): Notification {
        val context = ContextRetainer.context
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = NOTIFICATION_DEFAULT_CHANNEL_ID,
                channelName = context.getString(R.string.kaleyra_notification_outgoing_call),
                type = CallNotification.Type.OUTGOING
            )
            .caller(caller)
            .image(BitmapFactory.decodeResource(context.resources, R.drawable.avatar))
            .contentText(context.getString(R.string.kaleyra_notification_outgoing_call))
            .contentIntent(contentPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        BitmapUtils.uriToBitmap(image, IMAGE_SIZE, IMAGE_SIZE, onSuccess = {
            it?.also { builder.image(it) }
            onImageLoaded.invoke(builder.build())
        }, onFailure = { })

        return builder.build()
    }

    fun <T> buildOngoingCallNotification(
        caller: String,
        image: Uri,
        activityClazz: Class<T>,
        onImageLoaded: (Notification) -> Unit
    ): Notification {
        val context = ContextRetainer.context
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = NOTIFICATION_DEFAULT_CHANNEL_ID,
                channelName = context.getString(R.string.kaleyra_notification_ongoing_call),
                type = CallNotification.Type.ONGOING
            )
            .caller(caller)
            .image(BitmapFactory.decodeResource(context.resources, R.drawable.avatar))
            .contentText(context.getString(R.string.kaleyra_notification_ongoing_call))
            .contentIntent(contentPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        BitmapUtils.uriToBitmap(image, IMAGE_SIZE, IMAGE_SIZE, onSuccess = {
            it?.also { builder.image(it) }
            onImageLoaded.invoke(builder.build())
        }, onFailure = { })

        return builder.build()
    }

    private fun fullScreenPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(
            context,
            FULL_SCREEN_REQUEST_CODE,
            activityClazz
        )

    private fun contentPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(
            context,
            CONTENT_REQUEST_CODE,
            activityClazz
        )

    private fun answerPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(
            context,
            ANSWER_REQUEST_CODE,
            activityClazz,
            true
        )

    private fun <T> createCallActivityPendingIntent(
        context: Context,
        requestCode: Int,
        activityClazz: Class<T>,
        enableAutoAnswer: Boolean = false
    ): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("enableTilt", DeviceUtils.isSmartGlass)
            putExtra("autoAnswer", enableAutoAnswer)
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }

    private fun declinePendingIntent(context: Context) =
        PendingIntent.getBroadcast(
            context,
            DECLINE_REQUEST_CODE,
            Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationReceiver.ACTION_HANGUP
            },
            PendingIntentExtensions.updateFlags
        )

    // Needed for some devices
    private fun turnOnScreen(context: Context) {
        if (!context.isScreenOff()) return
        val pm =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            javaClass.name
        )
        wl.acquire(3000)
    }
}
