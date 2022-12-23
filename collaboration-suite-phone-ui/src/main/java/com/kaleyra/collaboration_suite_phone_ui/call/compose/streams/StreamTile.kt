package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Avatar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.IconButton
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

const val StreamTestTag = "StreamTestTag"
const val StreamHeaderTestTag = "StreamHeaderTestTag"

@Composable
internal fun StreamTile(
    stream: StreamUi,
    showHeader: Boolean = true,
    isFullscreen: Boolean = false,
    onFullscreenClick: () -> Unit,
    onBackPressed: (() -> Unit)? = null,
    headerModifier: Modifier = Modifier,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Black,
        contentColor = Color.White
    ) {
        Box {
            if (stream.view != null) {
                AndroidView(
                    factory = { stream.view },
                    modifier = Modifier.testTag(StreamTestTag)
                )
            }

            if (!stream.isVideoEnabled) {
                Avatar(
                    uri = stream.avatar,
                    contentDescription = stringResource(id = R.string.kaleyra_avatar),
                    placeholder = R.drawable.ic_kaleyra_avatar_bold,
                    error = R.drawable.ic_kaleyra_avatar_bold,
                    contentColor = Color.White,
                    backgroundColor = colorResource(id = R.color.kaleyra_color_background_dark),
                    size = 128.dp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            AnimatedVisibility(
                visible = showHeader,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = headerModifier
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .testTag(StreamHeaderTestTag)
                ) {
                    if (onBackPressed != null) {
                        IconButton(
                            icon = rememberVectorPainter(image = Icons.Filled.ArrowBack),
                            iconDescription = stringResource(id = R.string.kaleyra_back),
                            onClick = onBackPressed
                        )
                    }

                    Text(
                        text = stream.username,
                        modifier = Modifier
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                            .weight(1f),
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        icon = painterResource(
                            id = if (isFullscreen) R.drawable.ic_kaleyra_exit_fullscreen else R.drawable.ic_kaleyra_enter_fullscreen
                        ),
                        iconDescription = stringResource(
                            id = if (isFullscreen) R.string.kaleyra_exit_fullscreen else R.string.kaleyra_enter_fullscreen
                        ),
                        onClick = onFullscreenClick
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun StreamPreview() {
    KaleyraTheme() {
        StreamTile(
            stream = streamUiMock,
            onBackPressed = { },
            onFullscreenClick = { }
        )
    }
}