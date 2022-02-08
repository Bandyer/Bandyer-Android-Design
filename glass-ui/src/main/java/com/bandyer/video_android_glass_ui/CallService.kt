package com.bandyer.video_android_glass_ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.bandyer.collaboration_center.CollaborationSession

abstract class CallService : LifecycleService(), CallUIDelegate, CallUIController,
    DeviceStatusDelegate {

    @Suppress("UNCHECKED_CAST")
    inner class ServiceBinder : Binder() {
        fun <T : CallService> getService(): T = this@CallService as T
    }

    inner class ServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            when (intent.action) {
                ServiceManager.ACTION_DIAL -> {
                    val users = intent.getStringArrayExtra(ServiceManager.EXTRA_USERS) ?: return
                    onDial(users.toList(), true)
                }
                ServiceManager.ACTION_UPDATE_SESSION -> {
                    val session =
                        intent.getParcelableExtra<CollaborationSession>(ServiceManager.EXTRA_SESSION)
                            ?: return
                    onUpdateSession(session)
                }
                else -> Unit
            }
        }
    }

    private val binder = ServiceBinder()

    private val broadcastReceiver = ServiceReceiver()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(broadcastReceiver, IntentFilter().apply {
            addAction(ServiceManager.ACTION_DIAL)
            addAction(ServiceManager.ACTION_SEND_USER_DETAILS)
            addAction(ServiceManager.ACTION_UPDATE_SESSION)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    abstract fun onUserDetails()

    abstract fun onDial(otherUsers: List<String>, withVideoOnStart: Boolean? = null)

    abstract fun onUpdateSession(session: CollaborationSession)
}