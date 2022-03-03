package com.bandyer.video_android_core_ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.database.DefaultDatabaseErrorHandler
import android.os.IBinder
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.bandyer.android_common.ContextRetainer
import com.bandyer.collaboration_center.Collaboration
import com.bandyer.collaboration_center.Collaboration.Configuration
import com.bandyer.collaboration_center.Collaboration.Credentials
import com.bandyer.collaboration_center.User
import com.bandyer.collaboration_center.phonebox.PhoneBox
import com.bandyer.collaboration_center.phonebox.PhoneBox.CreationOptions
import com.bandyer.collaboration_center.phonebox.PhoneBox.State.Connecting
import com.bandyer.video_android_core_ui.call.CallActivity
import com.bandyer.video_android_core_ui.call.CallService
import com.bandyer.video_android_core_ui.common.BoundService
import com.bandyer.video_android_core_ui.model.UsersDescription
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object CollaborationUI {

    private var collaboration: Collaboration? = null

    private var lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            phoneBox.connect()
        }
    }

    var usersDescription: UsersDescription? = null

    val phoneBox: PhoneBoxUI
        get() {
            require(collaboration != null) { "setUp the CollaborationUI to use the phoneBox" }
            return PhoneBoxUI(collaboration!!.phoneBox)
        }

    fun <T : CallActivity> setUp(
        credentials: Credentials,
        configuration: Configuration,
        activityClazz: Class<T>
    ): Boolean {
        if (collaboration != null) return false
        Collaboration.create(credentials, configuration).apply { collaboration = this }
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        phoneBox.state.filter { it is Connecting }.onEach { startPhoneBoxService(activityClazz) }.launchIn(MainScope())
        return true
    }

    fun dispose() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        stopPhoneBoxService()
        phoneBox.disconnect()
        collaboration = null
    }

    private fun <T : CallActivity> startPhoneBoxService(activityClazz: Class<T>) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                val service = (binder as BoundService.ServiceBinder).getService<CallService>()
                service.bind(phoneBox, usersDescription)
            }

            override fun onServiceDisconnected(componentName: ComponentName) = Unit
        }

        with(ContextRetainer.context) {
            val intent = Intent(this, CallService::class.java)
            startService(intent)
            bindService(
                intent.apply { putExtra("activityClazzName", activityClazz.name) },
                serviceConnection,
                0
            )
        }
    }

    private fun stopPhoneBoxService() = with(ContextRetainer.context) {
        stopService(Intent(this, CallService::class.java))
    }
}

data class PhoneBoxUI(private val phoneBox: PhoneBox) : PhoneBox by phoneBox {
    fun dial(users: List<User>, conf: (CreationOptions.() -> Unit)? = null) =
        create(users, conf).apply { connect() }

    fun join(url: String) = create(url).apply { connect() }
}