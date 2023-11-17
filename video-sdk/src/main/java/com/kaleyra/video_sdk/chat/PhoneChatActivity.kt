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


