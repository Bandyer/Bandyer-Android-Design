package com.kaleyra.collaboration_suite_core_ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite_core_ui.notification.DisplayedChatActivity
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

abstract class ChatActivity : FragmentActivity() {

    protected open val viewModel: ChatViewModel by viewModels()

    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChatOrCloseActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setChatOrCloseActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        chatId?.let { sendChatAction(DisplayedChatActivity.ACTION_CHAT_VISIBLE, it) }
    }

    override fun onStop() {
        super.onStop()
        sendChatAction(DisplayedChatActivity.ACTION_CHAT_NOT_VISIBLE)
    }

    private fun setChatOrCloseActivity(intent: Intent) {
        viewModel.isCollaborationConfigured
            .take(1)
            .onEach {
                if (it) setChat(intent)
                else {
                    finishAndRemoveTask()
                    ContextRetainer.context.goToLaunchingActivity()
                }
            }.launchIn(lifecycleScope)
    }

    private fun setChat(intent: Intent) {
        val userId = intent.extras?.getString("userId") ?: return
        val chat = viewModel.setChat(userId) ?: return
        chatId = chat.id
        sendChatAction(DisplayedChatActivity.ACTION_CHAT_VISIBLE, chat.id)
    }

    private fun sendChatAction(action: String, chatId: String? = null) {
        sendBroadcast(Intent(this, DisplayedChatActivity::class.java).apply {
            this.action = action
            chatId?.let { putExtra(DisplayedChatActivity.EXTRA_CHAT_ID, it) }
        })
    }
}