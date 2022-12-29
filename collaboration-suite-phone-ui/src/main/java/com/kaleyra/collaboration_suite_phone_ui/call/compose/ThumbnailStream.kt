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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Stream

@Composable
internal fun ThumbnailStream(
    stream: StreamUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Thumbnail(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClickLabel = stringResource(id = R.string.kaleyra_move_to_featured_streams),
            role = Role.Button,
            onClick = onClick
        )
    ) {
        Stream(
            streamView = stream.view,
            avatar = stream.avatar,
            avatarVisible = !stream.isVideoEnabled,
            backgroundColor = Color.DarkGray,
            avatarSize = 64.dp
        )
    }
}