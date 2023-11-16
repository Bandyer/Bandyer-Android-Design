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

package com.kaleyra.video_common_ui

import android.os.Parcelable
import androidx.annotation.Keep
import com.kaleyra.video.conversation.Chat
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.utils.extensions.mapToSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.parcelize.Parcelize

/**
 * The chat UI
 *
 * @property actions The MutableStateFlow containing the set of actions
 * @property chatActivityClazz The chat activity Class<*>
 * @property chatCustomNotificationActivityClazz The custom chat notification activity Class<*>
 * @constructor
 */
class ChatUI(
    chat: Chat,
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(Action.default),
    private val chatActivityClazz: Class<*>,
    private val chatCustomNotificationActivityClazz: Class<*>? = null
) : Chat by chat {

    private var chatScope = CoroutineScope(Dispatchers.IO)

    /**
     * @suppress
     */
    override val messages: SharedFlow<MessagesUI> = chat.messages.mapToSharedFlow(chatScope) { MessagesUI(it, chatActivityClazz, chatCustomNotificationActivityClazz) }

    /**
     * The chat action sealed class
     */
    @Keep
    sealed class Action : Parcelable {
        /**
         * @suppress
         */
        companion object {

            /**
             * A set of all tools
             */
            val all by lazy {
                setOf(
                    ShowParticipants,
                    CreateCall(preferredType = Call.PreferredType.audioOnly()),
                    CreateCall(preferredType = Call.PreferredType.audioUpgradable()),
                    CreateCall(preferredType = Call.PreferredType.audioVideo())
                )
            }

            val default by lazy {
                setOf(
                    ShowParticipants,
                    CreateCall(preferredType = Call.PreferredType.audioUpgradable()),
                    CreateCall(preferredType = Call.PreferredType.audioVideo())
                )
            }
        }

        /**
         * The create call action
         *
         * @property preferredType The call PreferredType
         * @constructor
         */
        @Parcelize
        data class CreateCall(val preferredType: Call.PreferredType = Call.PreferredType.audioVideo()) : Action()

        /**
         * Show participants action
         */
        @Parcelize
        object ShowParticipants : Action()
    }
}