package com.kaleyra.collaboration_suite_core_ui

import android.os.Parcelable
import androidx.annotation.Keep
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.utils.extensions.mapToStateFlow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize

class ChatUI(
    chat: Chat,
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(setOf()),
    private val chatActivityClazz: Class<*>,
    private val chatNotificationActivityClazz: Class<*>? = null
) : Chat by chat {

    override val messages: StateFlow<MessagesUI> = chat.messages.mapToStateFlow(MainScope()) {
        MessagesUI(it, chatActivityClazz, chatNotificationActivityClazz)
    }

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
                    CreateCall(preferredType = Call.PreferredType(video = Call.Video.Disabled)),
                    CreateCall()
                )
            }
        }

        @Parcelize
        data class CreateCall(val preferredType: Call.PreferredType = Call.PreferredType()) : Action()
    }
}