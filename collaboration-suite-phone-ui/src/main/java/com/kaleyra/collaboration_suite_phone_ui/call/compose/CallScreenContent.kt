package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun CallScreenContent(callScreenState: CallScreenState, sheetPadding: WindowInsets) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(sheetPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        with(callScreenState) {
            Text(text = "Current value: ${sheetState.currentValue}")
            Text(text = "Target value: ${sheetState.targetValue}")
            Text(text = "Direction: ${sheetState.direction}")
            Text(text = "Fraction: ${sheetState.progress.fraction}")
            Text(text = "Offset: ${sheetState.offset.value}")
            Text(text = "Overflow: ${sheetState.overflow.value}")
        }
    }
}