package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BottomInsetsSpacer(
    modifier: Modifier = Modifier
) {
    val navigationBarsInsets = WindowInsets.navigationBars

    Spacer(
        modifier
            .windowInsetsBottomHeight(navigationBarsInsets)
            .fillMaxWidth()
    )
}