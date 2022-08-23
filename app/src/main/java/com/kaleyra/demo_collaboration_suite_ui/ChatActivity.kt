/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import androidx.lifecycle.ViewModel
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.ChatScreen
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.CallType
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ChatUiViewModel
import kotlinx.coroutines.flow.StateFlow

class MockChatViewModel : ViewModel(), ChatUiViewModel {

    override val uiState: StateFlow<ChatUiState>
        get() = TODO("Not yet implemented")

    override fun readAllMessages() {
        TODO("Not yet implemented")
    }

    override fun sendMessage(text: String) {
        TODO("Not yet implemented")
    }

    override fun fetchMessages() {
        TODO("Not yet implemented")
    }

    override fun onMessageScrolled(messageItem: ConversationItem.MessageItem) {
        TODO("Not yet implemented")
    }

    override fun onAllMessagesScrolled() {
        TODO("Not yet implemented")
    }

    override fun call(callType: CallType) {
        TODO("Not yet implemented")
    }

    override fun showCall() {
        TODO("Not yet implemented")
    }

    init {
        fetchMessages()
    }
}

class ChatActivity : ComponentActivity() {

    private val viewModel: MockChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                ChatScreen(onBackPressed = { onBackPressed() }, viewModel = viewModel)
            }
        }
    }
}