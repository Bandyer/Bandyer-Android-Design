package com.kaleyra.collaboration_suite_core_ui

import android.os.Parcelable
import androidx.annotation.Keep
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.mapToSharedFlow
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
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(setOf()),
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
                    CreateCall(preferredType = Call.PreferredType(video = Call.Video.Disabled)),
                    CreateCall()
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
        data class CreateCall(val preferredType: Call.PreferredType = Call.PreferredType()) : Action()

        /**
         * Show participants action
         */
        @Parcelize
        object ShowParticipants : Action()
    }
}