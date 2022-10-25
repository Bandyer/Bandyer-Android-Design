@file:OptIn(ExperimentalMaterialApi::class)

package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.launch

// I bottoni di mostrano con una trasparenza che l'alto
// Deve essere possibile togliere il background? Fare qui o altrove?
// Se ho meno di 5 elementi, il bottom sheet è fisso in modalità half expanded e la linea deve essere un puntino
// Rendere icone ruotabili

@Composable
internal fun BottomSheetContent(
    sheetState: BottomSheetState,
    callActions: ImmutableList<CallAction>
) {
    val contentColor = LocalContentColor.current
    val scope = rememberCoroutineScope()
    val sheetCollapsing by sheetCollapsing(sheetState)
    val sheetCollapsed by sheetCollapsed(sheetState)
    val columnCount = columnCount(callActions)
    val halfExpand = remember {
        {
            if (sheetState.isCollapsed) {
                scope.launch {
                    sheetState.halfExpand()
                }
            }
        }
    }

    Line(
        collapsed = sheetCollapsing,
        onClickLabel = "clickLabel",
        color = if (sheetCollapsed) Color.White else contentColor.copy(alpha = 0.8f),
        onClick = halfExpand
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(count = columnCount),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(items = callActions.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 20.dp, bottom = 8.dp)
            ) {
                CallAction(
                    toggled =,
                    onToggled = ,
                    text =,
                    icon =,
                    enabled = it.enabled
                )
            }
        }
    }
}

@Composable
private fun sheetCollapsing(sheetState: BottomSheetState): State<Boolean> {
    return remember(sheetState) {
        derivedStateOf {
            sheetState.targetValue == BottomSheetValue.Collapsed
        }
    }
}

@Composable
private fun sheetCollapsed(sheetState: BottomSheetState): State<Boolean> {
    return remember(sheetState) {
        derivedStateOf {
            sheetState.targetValue == BottomSheetValue.Collapsed && sheetState.progress.fraction == 1f
        }
    }
}

@Composable
private fun columnCount(callActions: ImmutableList<CallAction>): Int {
    return remember(callActions) {
        callActions.count.coerceAtMost(4)
    }
}

@Composable
private fun painterFor(action: CallAction): Painter =
    painterResource(
        id = when (action) {
            is CallAction.Camera -> R.drawable.ic_kaleyra_camera_off
            is CallAction.Microphone -> R.drawable.ic_kaleyra_mic_off
            is CallAction.SwitchCamera -> R.drawable.ic_kaleyra_switch_camera
            is CallAction.HungUp -> R.drawable.ic_kaleyra_hangup
            is CallAction.Chat -> R.drawable.ic_kaleyra_camera_off
            is CallAction.Whiteboard -> R.drawable.ic_kaleyra_whiteboard
            is CallAction.FileSharing -> R.drawable.ic_kaleyra_file_share
            is CallAction.Audio -> R.drawable.ic_kaleyra_earpiece
            is CallAction.ScreenSharing -> R.drawable.ic_kaleyra_screen_share
        }
    )


