package com.bandyer.app_design

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.app_design.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setApi31Listeners()
    }

    private fun setApi31Listeners() {
        val serviceIntentApi31 = Intent(this, NotificationServiceApi31::class.java).apply {
            putExtra("user", "Mario Draghi")
        }

        binding.bandyerIncomingApi31Button.setOnClickListener {
            serviceIntentApi31.also {
                it.putExtra("type", NotificationServiceApi31.NotificationType.INCOMING_CALL)
                it.putExtra("subtext", "Incoming call")
                startService(it)
            }
        }

        binding.bandyerOngoingApi31Button.setOnClickListener {
            serviceIntentApi31.also {
                it.putExtra("type", NotificationServiceApi31.NotificationType.ONGOING_CALL)
                it.putExtra("subtext", "Ongoing call")
                startService(it)
            }
        }

        binding.bandyerScreeningApi31Button.setOnClickListener {
            serviceIntentApi31.also {
                it.putExtra("type", NotificationServiceApi31.NotificationType.SCREENING_CALL)
                it.putExtra("subtext", "Screening call")
                startService(it)
            }
        }
    }
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
        val subtext = intent?.getStringExtra("subtext") ?: "null"
        val type = intent?.getSerializableExtra("type") as? NotificationType ?: NotificationType.INCOMING_CALL
        startForeground(FOREGROUND_SERVICE_ID, buildNotification(user, subtext, type))
        return super.onStartCommand(intent, flags, startId)
    }

    private fun buildNotification(
        user: String,
//        avatar: Icon,
        subtext: String,
//        applicationIcon: Icon,
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
            setSmallIcon(R.drawable.bandyer_z_audio_only)
            setCategory(Notification.CATEGORY_CALL)
            setContentIntent(callIntent)
            setFullScreenIntent(ringingIntent, true)
        }

        NotificationHelper.createNotificationChannel(this, channelId, channelName)

        val person = Person.Builder()
            .setName(user)
//            .setIcon(avatar)
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