package com.bandyer.video_android_core_ui.common

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService

abstract class BoundService : LifecycleService() {
    @Suppress("UNCHECKED_CAST")
    inner class ServiceBinder : Binder() {
        fun <T : BoundService> getService(): T = this@BoundService as T
    }

    private var binder: ServiceBinder? = null

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return ServiceBinder().also { binder = it }
    }

    override fun onDestroy() {
        super.onDestroy()
        binder = null
    }
}