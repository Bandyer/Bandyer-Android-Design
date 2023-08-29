package com.kaleyra.collaboration_suite_phone_ui.chat

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.Theme
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel.PhoneChatViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class PhoneChatActivity : ChatActivity() {

    override val viewModel: PhoneChatViewModel by viewModels {
        PhoneChatViewModel.provideFactory(::requestConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val systemUiController = rememberSystemUiController()
            val theme by viewModel.theme.collectAsStateWithLifecycle(Theme())
            val isSystemDarkTheme = isSystemInDarkTheme()
            val isDarkTheme by remember {
                derivedStateOf {
                    when (theme.defaultStyle) {
                        Theme.DefaultStyle.Day -> false
                        Theme.DefaultStyle.Night -> true
                        else -> isSystemDarkTheme
                    }
                }
            }

            LaunchedEffect(Unit) {
                snapshotFlow { isDarkTheme }
                    .onEach {
                        systemUiController.statusBarDarkContentEnabled = !it
                        systemUiController.navigationBarDarkContentEnabled = !it
                    }
                    .launchIn(this)
            }

            KaleyraTheme(
                lightColors = theme.day.colors,
                darkColors = theme.night.colors,
                fontFamily = theme.fontFamily,
                isDarkTheme = when (theme.defaultStyle) {
                    Theme.DefaultStyle.Day -> false
                    Theme.DefaultStyle.Night -> true
                    else -> isDarkTheme
                }
            ) {
                ChatScreen(onBackPressed = this::finishAndRemoveTask, viewModel = viewModel)
            }
        }
    }
}


