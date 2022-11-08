@file:OptIn(ExperimentalFoundationApi::class)

package com.kaleyra.collaboration_suite_phone_ui.call.compose.bottomsheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun BottomSheetContent(
    lineState: LineState,
    onLineClick: () -> Unit,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalOverScrollConfiguration provides null) {
        Column {
            Line(
                state = lineState,
                onClickLabel = stringResource(id = R.string.kaleyra_call_show_buttons),
                onClick = onLineClick
            )
            content()
        }
    }
}

@Preview
@Composable
fun BottomSheetContentPreview() {
    KaleyraTheme {
        BottomSheetContent(
            lineState = LineState.Expanded,
            onLineClick = { },
            content = { }
        )
    }
}
