package com.kaleyra.collaboration_suite_phone_ui.call

import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite.conference.StreamView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.utils.StreamViewSettings.thumbnailSettings
import com.kaleyra.collaboration_suite_phone_ui.call.component.pointer.view.PointerStreamWrapper
import com.kaleyra.collaboration_suite_phone_ui.call.streams.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.streams.StreamContainer

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ThumbnailStream(
    stream: StreamUi,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTesting: Boolean = false
) {
    Thumbnail(
        modifier = modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClickLabel = stringResource(id = R.string.kaleyra_move_to_featured_streams),
            role = Role.Button,
            onClick = onClick,
            onDoubleClick = onDoubleClick
        )
    ) {
        StreamContainer(
            backgroundColor = Color.DarkGray,
            contentColor = Color.White
        ) {
            PointerStreamWrapper(
                streamView = stream.video?.view,
                pointerList = stream.video?.pointers,
                isTesting = isTesting
            ) { hasPointers ->
                val shouldFit = stream.video?.isScreenShare == true || hasPointers
                Stream(
                    streamView = stream.video?.view?.thumbnailSettings(
                        scaleType = if (shouldFit) StreamView.ScaleType.Fit else StreamView.ScaleType.Fill(1f)
                    ) ?: ImmutableView(View(LocalContext.current)),
                    avatar = stream.avatar,
                    avatarVisible = stream.video?.view == null || !stream.video.isEnabled,
                    avatarSize = 64.dp
                )
            }
        }
    }
}
