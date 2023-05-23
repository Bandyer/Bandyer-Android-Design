package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer.PointerStreamWrapper
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamContainer
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.highlightOnFocus

// TODO fix avatar size for large screens
@Composable
internal fun ThumbnailStream(
    stream: StreamUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Thumbnail(
        modifier = modifier
            .highlightOnFocus(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = stringResource(id = R.string.kaleyra_move_to_featured_streams),
                role = Role.Button,
                onClick = onClick
            )
    ) {
        StreamContainer(
            backgroundColor = Color.DarkGray,
            contentColor = Color.White
        ) {
            PointerStreamWrapper(pointerList = stream.video?.pointers) {
                Stream(
                    streamView = stream.video?.view,
                    avatar = stream.avatar,
                    avatarVisible = stream.video == null || !stream.video.isEnabled,
                    avatarSize = 64.dp
                )
            }
        }

    }
}