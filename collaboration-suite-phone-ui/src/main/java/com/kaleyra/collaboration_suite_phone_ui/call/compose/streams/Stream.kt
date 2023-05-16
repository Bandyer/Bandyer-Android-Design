package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import android.graphics.Rect
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Avatar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

const val StreamViewTestTag = "StreamTestTag"
val DefaultStreamAvatarSize = 128.dp

@Composable
internal fun Stream(
    streamView: ImmutableView? = null,
    avatar: ImmutableUri?,
    avatarSize: Dp = DefaultStreamAvatarSize,
    avatarVisible: Boolean = false,
    onStreamViewPositioned: ((Rect) -> Unit)? = null
) {
    Box {
        if (streamView != null && !avatarVisible) {
            AndroidView(
                factory = {
                    streamView.value.also {
                        val parentView = streamView.value.parent as? ViewGroup
                        parentView?.removeView(it)
                    }
                },
                modifier = Modifier
                    .testTag(StreamViewTestTag)
                    .onGloballyPositioned {
                        onStreamViewPositioned?.invoke(it.boundsInRoot().toAndroidRect())
                    }
            )
        }

        if (avatarVisible) {
            Avatar(
                uri = avatar,
                contentDescription = stringResource(id = R.string.kaleyra_avatar),
                placeholder = R.drawable.ic_kaleyra_avatar_bold,
                error = R.drawable.ic_kaleyra_avatar_bold,
                contentColor = LocalContentColor.current,
                backgroundColor = colorResource(id = R.color.kaleyra_color_background_dark),
                size = avatarSize
            )
        }
    }
}

@Preview
@Composable
internal fun StreamPreview() {
    KaleyraTheme {
        Stream(
            streamView = streamUiMock.video?.view,
            avatar = streamUiMock.avatar,
            avatarVisible = false
        )
    }
}

