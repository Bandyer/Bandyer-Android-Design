package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Avatar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri

const val StreamViewTestTag = "StreamTestTag"
val DefaultStreamAvatarSize = 128.dp

@Composable
internal fun Stream(
    streamView: View? = null,
    avatar: ImmutableUri,
    backgroundColor: Color = Color.Black,
    contentColor: Color = Color.White,
    avatarSize: Dp = DefaultStreamAvatarSize,
    avatarVisible: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Box {
            if (streamView != null) {
                AndroidView(
                    factory = { streamView },
                    modifier = Modifier.testTag(StreamViewTestTag)
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
                    size = avatarSize,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

