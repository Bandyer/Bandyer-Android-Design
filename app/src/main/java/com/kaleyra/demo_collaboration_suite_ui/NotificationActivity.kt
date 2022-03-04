/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.demo_collaboration_suite_ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Icon
import android.os.*
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.kaleyra.demo_collaboration_suite_ui.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setApi21Listeners()
        setApi31Listeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) stopService(Intent(this, NotificationServiceApi31::class.java))
//        else stopService(Intent(this, NotificationService::class.java))
    }

    private fun initNotification() {
        val callIntent = Intent(this, CallActivity::class.java)
        val ringingIntent = Intent(this, RingingActivity::class.java)

        TaskStackBuilder.create(this).apply {
            addParentStack(CallActivity::class.java)
            addNextIntent(callIntent)
        }

        val callPendingIntent = PendingIntent.getActivity(
            this,
            0,
            callIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val name = "Mario"
        val subText = "Sottotitolo"
        val avatar = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_kaleyra_avatar_bold
        )
        val customView = RemoteViews(packageName, R.layout.kaleyra_notification2)

        customView.setTextViewText(R.id.kaleyra_username, name)
//        customView.setViewVisibility(R.id.kaleyra_subtitle, View.GONE)
        customView.setTextViewText(R.id.kaleyra_subtitle, subText)
//
//        customView.setTextViewText(
//            R.id.kaleyra_accept_btn,
//           "Answer"
//        )
//        customView.setTextViewText(
//            R.id.kaleyra_decline_btn,
//            "Decline"
//        )
        customView.setImageViewBitmap(R.id.kaleyra_avatar, avatar)
//        customView.setOnClickPendingIntent(R.id.answer_btn, answerPendingIntent)
//        customView.setOnClickPendingIntent(R.id.decline_btn, endPendingIntent)

        val builder = NotificationCompat.Builder(applicationContext, "channelId").apply {
            setContentTitle("Kaleyra Call") // or Kaleyra Video Call
                .setContentText(name)
                .setSmallIcon(R.drawable.kaleyra_z_audio_only) // or video icon
                .setSubText(subText)
                .setLargeIcon(avatar)
                .setContentIntent(callPendingIntent)
        }

        builder.setCustomContentView(customView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("channelId", "Incoming call",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableLights(true)
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

//        builder.addAction(R.drawable.ic_call_end_white_24dp, endTitle, endPendingIntent)
//        builder.addAction(R.drawable.ic_call, answerTitle, answerPendingIntent)
//        builder.addPerson("tel:" + user.phone)

        builder.priority = Notification.PRIORITY_MAX
        builder.color = -0xff0033
        builder.setVibrate(LongArray(0))
        builder.setCategory(Notification.CATEGORY_CALL)
        builder.setFullScreenIntent(PendingIntent.getActivity(this, 0, ringingIntent,  0), true)

        NotificationManagerCompat.from(applicationContext).notify(888, builder.build())
    }

    private fun setApi21Listeners() {
        binding.kaleyraIncomingButton.setOnClickListener {
            initNotification()
        }
    }

    private fun setApi31Listeners() {
        val serviceIntentApi31 = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(this, NotificationServiceApi31::class.java).apply {
                putExtra("user", "John Smith")
                putExtra("icon", R.drawable.kaleyra_z_audio_only)
                putExtra("avatar", R.drawable.avatar)
            }
        } else null

        binding.kaleyraIncomingApi31Button.setOnClickListener {
            serviceIntentApi31?.also {
                it.putExtra("type", NotificationServiceApi31.NotificationType.INCOMING_CALL)
                it.putExtra("subtext", "Incoming call")
                startService(it)
            } ?: showToast("Available from API 31")
        }

        binding.kaleyraOngoingApi31Button.setOnClickListener {
            serviceIntentApi31?.also {
                it.putExtra("type", NotificationServiceApi31.NotificationType.ONGOING_CALL)
                it.putExtra("subtext", "Ongoing call")
                startService(it)
            } ?: showToast("Available from API 31")
        }

        binding.kaleyraScreeningApi31Button.setOnClickListener {
            serviceIntentApi31?.also {
                it.putExtra("type", NotificationServiceApi31.NotificationType.SCREENING_CALL)
                it.putExtra("subtext", "Screening call")
                startService(it)
            } ?: showToast("Available from API 31")
        }
    }

    private fun showToast(text: String) = Toast.makeText(this@NotificationActivity, text, Toast.LENGTH_SHORT).show()
}

@RequiresApi(Build.VERSION_CODES.S)
class NotificationServiceApi31 : Service() {

    enum class NotificationType {
        INCOMING_CALL,
        ONGOING_CALL,
        SCREENING_CALL
    }

    companion object {
        const val FOREGROUND_SERVICE_ID = 2200
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val user = intent?.getStringExtra("user") ?: "null"
        val avatar = intent?.getIntExtra("avatar", 0) ?: 0
        val subtext = intent?.getStringExtra("subtext") ?: "null"
        val icon = intent?.getIntExtra("icon", 0) ?: 0
        val type = intent?.getSerializableExtra("type") as? NotificationType
            ?: NotificationType.INCOMING_CALL
        startForeground(FOREGROUND_SERVICE_ID, buildNotification(user, avatar, subtext, icon, type))
        return super.onStartCommand(intent, flags, startId)
    }

    private fun buildNotification(
        user: String,
        @DrawableRes avatar: Int,
        subtext: String,
        @DrawableRes icon: Int,
        type: NotificationType
    ): Notification {
        val channelId = "channelId"
        // Name seen in the app info
        val channelName = type.toString()
        val callIntent = NotificationHelper.createCallPendingIntent(this@NotificationServiceApi31)
        val ringingIntent =
            NotificationHelper.createRingingPendingIntent(this@NotificationServiceApi31)

        val builder = Notification.Builder(applicationContext, channelId).apply {
            setContentText(subtext)
            setSmallIcon(Icon.createWithResource(this@NotificationServiceApi31, icon))
            setCategory(Notification.CATEGORY_CALL)
            setContentIntent(callIntent)
            setFullScreenIntent(ringingIntent, true)
        }

        NotificationHelper.createNotificationChannel(this, channelId, channelName)

        val person = Person.Builder()
            .setName(user)
            .setIcon(Icon.createWithBitmap(DrawableHelper.createCircleBitmap(this, avatar)))
            .build()

        builder.style = when (type) {
            NotificationType.INCOMING_CALL -> Notification.CallStyle.forIncomingCall(
                person,
                callIntent,
                callIntent
            )
            NotificationType.ONGOING_CALL -> Notification.CallStyle.forOngoingCall(
                person,
                callIntent
            )
            NotificationType.SCREENING_CALL -> Notification.CallStyle.forScreeningCall(
                person,
                callIntent,
                callIntent
            )
        }

        return builder.build()
    }
}

object DrawableHelper {

    fun createCircleBitmap(context: Context, @DrawableRes resource: Int): Bitmap {
        val bitmap = BitmapFactory
            .decodeResource(context.resources, resource)
            .copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val path = Path().apply {
            val halfWidth = bitmap.width / 2f
            val halfHeight = bitmap.width / 2f
            addCircle(halfWidth, halfHeight, halfWidth, Path.Direction.CW)
            toggleInverseFillType()
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        canvas.drawPath(path, paint)
        return bitmap
    }
}

object NotificationHelper {

    fun createCallPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, CallActivity::class.java)
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_CANCEL_CURRENT
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    fun createRingingPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, RingingActivity::class.java)
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE
            else 0
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context, channelId: String, channelName: String) {
        val notificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(notificationChannel)
    }

}