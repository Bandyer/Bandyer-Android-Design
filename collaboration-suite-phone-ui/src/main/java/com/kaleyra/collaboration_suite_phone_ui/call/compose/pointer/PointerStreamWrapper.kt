package com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import com.kaleyra.collaboration_suite.phonebox.VideoStreamView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

@Composable
internal fun PointerStreamWrapper(
    modifier: Modifier = Modifier,
    streamView: ImmutableView?,
    pointerList: ImmutableList<PointerUi>?,
    stream: @Composable (Boolean) -> Unit
) {
    if (streamView != null) {
        val size by getSize(streamView).collectAsStateWithLifecycle(IntSize(0,0))
        Box(contentAlignment = Alignment.Center) {
            stream(!pointerList.isNullOrEmpty())
            Box(modifier = modifier.size(size.toDpSize())) {
                pointerList?.value?.forEach { MovablePointer(it, size) }
            }
        }
    }
}

@Composable
internal fun IntSize.toDpSize() = with(LocalDensity.current) {
    DpSize(width.toDp(), height.toDp())
}

internal fun getSize(view: ImmutableView): Flow<IntSize> {
    val streamView = view.value as? VideoStreamView ?: return MutableStateFlow(IntSize(0,0))
    return streamView.videoSize.map { IntSize(it.width, it.height) }
}

private fun ImmutableList<PointerUi>?.isNullOrEmpty() = this == null || this.count() == 0