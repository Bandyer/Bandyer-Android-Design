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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.ChatScreen
import com.kaleyra.collaboration_suite_phone_ui.chat.model.mockChatUiState

class ChatActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                ChatScreen(
                    uiState = mockChatUiState,
                    onBackPressed = this::onBackPressed,
                    onMessageScrolled = { },
                    onResetMessagesScroll = { },
                    onFetchMessages = { },
                    onShowCall = { },
                    onSendMessage = { },
                    onTyping = { }
                )

                val isSystemInDarkTheme = isSystemInDarkTheme()
                SideEffect {
                    window.navigationBarColor = if (isSystemInDarkTheme) Color.Black.toArgb() else Color.White.toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !isSystemInDarkTheme
                }
            }
        }
    }
}
