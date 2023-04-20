package com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Composable
internal fun PointerStreamWrapper(
    modifier: Modifier = Modifier,
    pointerList: ImmutableList<PointerUi>?,
    stream: @Composable () -> Unit
) {
    var size by remember { mutableStateOf(IntSize(0, 0)) }
    Box(modifier = modifier.onGloballyPositioned { size = it.size }) {
        stream()
        pointerList?.value?.forEach { MovablePointer(it, size) }
    }
}