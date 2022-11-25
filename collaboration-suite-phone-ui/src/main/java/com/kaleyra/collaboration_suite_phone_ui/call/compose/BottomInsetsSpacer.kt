package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BottomInsetsSpacer() {
    val navigationBarsInsets = WindowInsets.navigationBars

    Spacer(
        Modifier
            .windowInsetsBottomHeight(navigationBarsInsets)
            .fillMaxWidth()
    )
}