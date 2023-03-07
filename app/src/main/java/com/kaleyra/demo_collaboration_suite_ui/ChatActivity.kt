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

package com.kaleyra.demo_collaboration_suite_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.ChatScreen
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.model.mockUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel.ChatUiViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockChatViewModel : ViewModel(), ChatUiViewModel {

    override val uiState: StateFlow<ChatUiState> = MutableStateFlow(mockUiState)

    override fun sendMessage(text: String) = Unit

    override fun typing() = Unit

    override fun fetchMessages() = Unit

    override fun onMessageScrolled(messageItem: ConversationItem.MessageItem) = Unit

    override fun onAllMessagesScrolled() = Unit

    override fun showCall() = Unit
}

class ChatActivity : ComponentActivity() {

    private val viewModel: MockChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                ChatScreen(onBackPressed = this::onBackPressed, viewModel = viewModel)

                val isSystemInDarkTheme = isSystemInDarkTheme()
                SideEffect {
                    window.navigationBarColor = if (isSystemInDarkTheme) Color.Black.toArgb() else Color.White.toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !isSystemInDarkTheme
                }
            }
        }
    }
}
