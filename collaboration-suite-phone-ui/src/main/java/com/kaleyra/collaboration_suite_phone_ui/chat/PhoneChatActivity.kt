package com.kaleyra.collaboration_suite_phone_ui.chat

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.chat.screen.ThemedChatScreen
import com.kaleyra.collaboration_suite_phone_ui.chat.screen.viewmodel.PhoneChatViewModel

internal class PhoneChatActivity : ChatActivity() {

    override val viewModel: PhoneChatViewModel by viewModels {
        PhoneChatViewModel.provideFactory(::requestConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ThemedChatScreen(onBackPressed = this::finishAndRemoveTask, viewModel = viewModel)
        }
    }
}


