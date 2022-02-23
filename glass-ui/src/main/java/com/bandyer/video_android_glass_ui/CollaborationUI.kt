package com.bandyer.video_android_glass_ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.bandyer.android_common.ContextRetainer
import com.bandyer.collaboration_center.Collaboration
import com.bandyer.collaboration_center.Collaboration.Authenticator
import com.bandyer.collaboration_center.Collaboration.Configuration
import com.bandyer.collaboration_center.PhoneBox
import com.bandyer.collaboration_center.PhoneBox.CreationOptions
import com.bandyer.collaboration_center.PhoneBox.State.Connecting
import com.bandyer.collaboration_center.User
import com.bandyer.video_android_core_ui.UsersDescription
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object CollaborationUI {

    private var collaboration: Collaboration? = null

    var usersDescription: UsersDescription? = null

    val phoneBox: PhoneBoxUI
        get() {
            require(collaboration != null) { "setUp the CollaborationUI to use the phoneBox" }
            return PhoneBoxUI(collaboration!!.phoneBox)
        }

    fun setUp(authenticator: Authenticator, configuration: Configuration): Boolean {
        if (collaboration != null) return false
        Collaboration.create(authenticator, configuration).apply { collaboration = this }
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
            bindService(intent, serviceConnection, 0)
            startService(intent)
        }
    }

    private fun stopPhoneBoxService() = with(ContextRetainer.context) {
        stopService(Intent(this, CallService::class.java))
    }
}

data class PhoneBoxUI(private val phoneBox: PhoneBox) : PhoneBox by phoneBox {
    fun dial(users: List<User>, conf: (CreationOptions.() -> Unit)? = null) = create(users, conf).apply { connect() }

    fun join(url: String) = create(url).apply { connect() }
}