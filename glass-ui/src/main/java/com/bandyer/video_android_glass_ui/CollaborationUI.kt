package com.bandyer.video_android_glass_ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.bandyer.android_common.ContextRetainer
import com.bandyer.collaboration_center.Collaboration
import com.bandyer.collaboration_center.Collaboration.Configuration
import com.bandyer.collaboration_center.Collaboration.Credentials
import com.bandyer.collaboration_center.User
import com.bandyer.collaboration_center.phonebox.PhoneBox
import com.bandyer.collaboration_center.phonebox.PhoneBox.CreationOptions
import com.bandyer.collaboration_center.phonebox.PhoneBox.State.Connecting
import com.bandyer.video_android_core_ui.UsersDescription
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object CollaborationUI {

    private var collaboration: Collaboration? = null
    private var notificationReceiver: NotificationReceiver? = null

    var usersDescription: UsersDescription? = null

    val phoneBox: PhoneBoxUI
        get() {
            require(collaboration != null) { "setUp the CollaborationUI to use the phoneBox" }
            return PhoneBoxUI(collaboration!!.phoneBox)
        }

    fun setUp(credentials: Credentials, configuration: Configuration): Boolean {
        if (collaboration != null) return false
        Collaboration.create(credentials, configuration).apply { collaboration = this }
        phoneBox.state.filter { it is Connecting }.onEach { startPhoneBoxService() }.launchIn(MainScope())
        return true
    }

    fun dispose() {
        stopPhoneBoxService()
        phoneBox.disconnect()
        collaboration = null
    }

    private fun startPhoneBoxService() {
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
            bindService(intent, serviceConnection, 0)
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