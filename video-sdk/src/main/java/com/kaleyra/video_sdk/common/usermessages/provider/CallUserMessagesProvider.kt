/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.common.usermessages.provider

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_sdk.call.mapper.InputMapper.toAudioConnectionFailureMessage
import com.kaleyra.video_sdk.call.mapper.InputMapper.toMutedMessage
import com.kaleyra.video_sdk.call.mapper.InputMapper.toUsbCameraMessage
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

object CallUserMessagesProvider {

    private var coroutineScope: CoroutineScope? = null

    private val userMessageChannel = Channel<UserMessage>(Channel.BUFFERED)
    val userMessage: Flow<UserMessage> = userMessageChannel.receiveAsFlow()

    fun start(call: Flow<CallUI>, scope: CoroutineScope = MainScope() + CoroutineName("CallUserMessagesProvider")) {
        if (coroutineScope != null) dispose()
        coroutineScope = scope
        userMessageChannel.sendRecordingEvents(call, scope)
        userMessageChannel.sendMutedEvents(call, scope)
        userMessageChannel.sendUsbCameraEvents(call, scope)
        userMessageChannel.sendFailedAudioOutputEvents(call, scope)
    }

    fun sendUserMessage(userMessage: UserMessage) {
        coroutineScope?.launch {
            userMessageChannel.send(userMessage)
        }
    }

    fun dispose() {
        coroutineScope?.cancel()
        coroutineScope = null
    }

    private fun Channel<UserMessage>.sendRecordingEvents(call: Flow<CallUI>, scope: CoroutineScope) {
        call.toRecordingMessage().dropWhile { it is RecordingMessage.Stopped }.onEach { send(it) }.launchIn(scope)
    }

    private fun Channel<UserMessage>.sendMutedEvents(call: Flow<CallUI>, scope: CoroutineScope) {
        call.toMutedMessage().onEach { send(it) }.launchIn(scope)
    }

    private fun Channel<UserMessage>.sendUsbCameraEvents(call: Flow<CallUI>, scope: CoroutineScope) {
        call.toUsbCameraMessage().dropWhile { it is UsbCameraMessage.Disconnected }.onEach { send(it) }.launchIn(scope)
    }

    private fun Channel<UserMessage>.sendFailedAudioOutputEvents(call: Flow<CallUI>, scope: CoroutineScope) {
        call.toAudioConnectionFailureMessage().onEach { send(it) }.launchIn(scope)
    }
}