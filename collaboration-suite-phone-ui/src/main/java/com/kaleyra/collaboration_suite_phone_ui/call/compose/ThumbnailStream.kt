package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite.phonebox.StreamView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamViewSettings.thumbnailSettings
import com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer.PointerStreamWrapper
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamContainer

@Composable
internal fun ThumbnailStream(
    stream: StreamUi,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null
) {
    Thumbnail(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleClick?.invoke() },
                    onTap = { onClick?.invoke() }
                )
            }
    ) {
        StreamContainer(
            backgroundColor = Color.DarkGray,
            contentColor = Color.White
        ) {
            PointerStreamWrapper(
                streamView = stream.video?.view,
                pointerList = stream.video?.pointers
            ) { hasPointers ->
                val shouldFit = stream.video?.isScreenShare == true || hasPointers
                Stream(
                    streamView = stream.video?.view?.thumbnailSettings(
                        scaleType = if (shouldFit) StreamView.ScaleType.Fit else StreamView.ScaleType.Fill(1f)
                    ),
                    avatar = stream.avatar,
                    avatarVisible = stream.video == null || !stream.video.isEnabled,
                    avatarSize = 64.dp
                )
            }
        }
    }
}
