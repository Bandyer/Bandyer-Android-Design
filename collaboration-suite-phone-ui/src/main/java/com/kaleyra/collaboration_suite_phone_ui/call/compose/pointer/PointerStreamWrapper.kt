package com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList


@Composable
internal fun PointerStreamWrapper(
    modifier: Modifier = Modifier,
    pointerList: ImmutableList<PointerUi>?,
    stream: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        stream()
        pointerList?.value?.forEach { PointerLayer(it) }
    }
}