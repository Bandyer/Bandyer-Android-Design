package com.bandyer.video_android_glass_ui

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.bandyer.collaboration_center.CollaborationSession

abstract class CallService : LifecycleService(), CallUIDelegate, CallUIController,
    DeviceStatusDelegate {

//    interface CallServiceListener {
//        fun onConnected()
//        fun onFailed()
//    }

    @Suppress("UNCHECKED_CAST")
    inner class ServiceBinder : Binder() {
        fun <T : CallService> getService(): T = this@CallService as T
    }

    private val binder = ServiceBinder()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    abstract fun dial(otherUsers: List<String>, withVideoOnStart: Boolean? = null)

    abstract fun joinUrl(joinUrl: String)

    abstract fun updateSession(session: CollaborationSession)
}