package com.kaleyra.video_sdk.call.stream.view.core

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.theme.KaleyraTheme

const val StreamOverlayTestTag = "StreamOverlayTestTag"
const val StreamViewTestTag = "StreamTestTag"
val DefaultStreamAvatarSize = 128.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun Stream(
    streamView: ImmutableView,
    avatar: ImmutableUri?,
    @DrawableRes  avatarPlaceholder: Int = R.drawable.ic_kaleyra_avatar_bold,
    @DrawableRes  avatarError: Int = R.drawable.ic_kaleyra_avatar_bold,
    avatarSize: Dp = DefaultStreamAvatarSize,
    avatarVisible: Boolean = false,
    showOverlay: Boolean = false
) {
    Box {
        AnimatedContent(
            targetState = !avatarVisible,
            transitionSpec = {
                if (targetState) fadeIn(tween(500)) with fadeOut(tween(500))
                else EnterTransition.None with ExitTransition.None
            },
            label = "avatarVisibility"
        ) {
            if (it) {
                key(streamView) {
                    AndroidView(
                        factory = {
                            streamView.value.also {
                                val parentView = it.parent as? ViewGroup
                                parentView?.removeView(it)
                            }
                        },
                        update = { view ->
                            val newLayoutParams = view.layoutParams
                            newLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                            newLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            view.layoutParams = newLayoutParams
                        },
                        modifier = Modifier.testTag(StreamViewTestTag)
                    )
                }
                if (showOverlay) {
                    Spacer(
                        Modifier
                            .fillMaxSize()
                            .background(colorResource(id = R.color.kaleyra_color_featured_stream_overlay))
                            .testTag(StreamOverlayTestTag)
                    )
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Avatar(
                        uri = avatar,
                        contentDescription = stringResource(id = R.string.kaleyra_avatar),
                        placeholder = avatarPlaceholder,
                        error = avatarError,
                        contentColor = LocalContentColor.current,
                        backgroundColor = colorResource(id = R.color.kaleyra_color_background_dark),
                        size = avatarSize
                    )
                }
            }
        }
    }
}

@Preview
@Composable
internal fun StreamPreview() {
    KaleyraTheme {
        Stream(
            streamView = streamUiMock.video?.view ?: ImmutableView(View(LocalContext.current)),
            avatar = streamUiMock.avatar,
            avatarVisible = false,
            showOverlay = true
        )
    }
}

