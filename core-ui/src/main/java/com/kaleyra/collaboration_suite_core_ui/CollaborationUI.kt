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

package com.kaleyra.collaboration_suite_core_ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite.Collaboration.Configuration
import com.kaleyra.collaboration_suite.Collaboration.Credentials
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite.phonebox.PhoneBox.CreationOptions
import com.kaleyra.collaboration_suite.phonebox.PhoneBox.State.Connecting
import com.kaleyra.collaboration_suite_core_ui.call.CallActivity
import com.kaleyra.collaboration_suite_core_ui.call.CallService
import com.kaleyra.collaboration_suite_core_ui.common.BoundService
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

object CollaborationUI {

    private var collaboration: Collaboration? = null

    private var wasPhoneBoxConnected = false

    private var lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            if (wasPhoneBoxConnected) phoneBox.connect()
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            wasPhoneBoxConnected =
                phoneBox.state.value !is PhoneBox.State.Disconnected && phoneBox.state.value !is PhoneBox.State.Disconnecting
            val call = phoneBox.call.replayCache.firstOrNull() ?: return
            call.state.takeWhile { it !is Call.State.Disconnected.Ended }
                .onCompletion { stopPhoneBoxService() }.launchIn(MainScope())
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
        phoneBox.state.filter { it is Connecting }.onEach { startPhoneBoxService(activityClazz) }
            .launchIn(MainScope())
        return true
    }

    fun dispose() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        wasPhoneBoxConnected = false
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