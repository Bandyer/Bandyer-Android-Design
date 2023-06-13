package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite.phonebox.StreamView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.IconButton
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamViewSettings.featuredSettings
import com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer.PointerStreamWrapper
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

const val FeaturedStreamTag = "FeaturedStreamTag"

@Composable
internal fun FeaturedStream(
    stream: StreamUi,
    isFullscreen: Boolean = false,
    onFullscreenClick: () -> Unit,
    onBackPressed: (() -> Unit)? = null,
    headerModifier: Modifier = Modifier,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.testTag(FeaturedStreamTag)
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            StreamContainer {
                PointerStreamWrapper(
                    streamView = stream.video?.view,
                    pointerList = stream.video?.pointers
                ) {
                    val shouldFit = stream.video?.isScreenShare == true
                    Stream(
                        streamView = stream.video?.view?.featuredSettings(
                            if (shouldFit) StreamView.ScaleType.Fit else StreamView.ScaleType.Fill()
                        ),
                        avatar = stream.avatar,
                        avatarVisible = stream.video == null || !stream.video.isEnabled
                    )
                }
            }

            Header(
                username = stream.username,
                fullscreen = isFullscreen,
                onBackPressed = onBackPressed,
                onFullscreenClick = onFullscreenClick,
                modifier = headerModifier
            )
        }
    }
}

@Composable
private fun Header(
    username: String,
    fullscreen: Boolean,
    onBackPressed: (() -> Unit)? = null,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(horizontal = 4.dp)) {
        if (onBackPressed != null) {
            IconButton(
                icon = rememberVectorPainter(image = Icons.Filled.ArrowBack),
                iconDescription = stringResource(id = R.string.kaleyra_back),
                onClick = onBackPressed
            )
        }

        Text(
            text = username,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .weight(1f),
            fontWeight = FontWeight.SemiBold
        )

        IconButton(
            icon = painterResource(
                id = if (fullscreen) R.drawable.ic_kaleyra_exit_fullscreen else R.drawable.ic_kaleyra_enter_fullscreen
            ),
            iconDescription = stringResource(
                id = if (fullscreen) R.string.kaleyra_exit_fullscreen else R.string.kaleyra_enter_fullscreen
            ),
            onClick = onFullscreenClick
        )
    }
}

@Preview
@Composable
internal fun FeaturedStreamPreview() {
    KaleyraTheme {
        FeaturedStream(
            stream = streamUiMock,
            onBackPressed = { },
            onFullscreenClick = { }
        )
    }
}