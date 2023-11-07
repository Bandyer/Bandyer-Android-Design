package com.kaleyra.video_sdk.chat

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.kaleyra.video_common_ui.ChatActivity
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_sdk.chat.screen.ChatScreen
import com.kaleyra.video_sdk.chat.screen.viewmodel.PhoneChatViewModel

internal class PhoneChatActivity : ChatActivity() {

    override val viewModel: PhoneChatViewModel by viewModels {
        PhoneChatViewModel.provideFactory(::requestConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ChatScreen(onBackPressed = this::finishAndRemoveTask, viewModel = viewModel)
        }
    }
}


