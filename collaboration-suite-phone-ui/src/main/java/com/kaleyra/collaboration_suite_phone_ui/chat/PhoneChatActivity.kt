package com.kaleyra.collaboration_suite_phone_ui.chat

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.lightColors
import androidx.core.view.WindowCompat
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.Theme
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel.PhoneChatViewModel

internal class PhoneChatActivity : ChatActivity() {

    override val viewModel: PhoneChatViewModel by viewModels {
        PhoneChatViewModel.provideFactory(::requestConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val theme = viewModel.theme.collectAsStateWithLifecycle(Theme()).value
            KaleyraTheme(
                lightColors = theme.day.colors,
                darkColors = theme.night.colors,
                fontFamily = theme.fontFamily,
                isDarkTheme = when (theme.defaultStyle) {
                    Theme.DefaultStyle.Day -> false
                    Theme.DefaultStyle.Night -> true
                    else -> isSystemInDarkTheme()
                },
                content = { ChatScreen(onBackPressed = this::finishAndRemoveTask, viewModel = viewModel) }
            )
        }
    }
}


