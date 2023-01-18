package com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Avatar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.DefaultStreamAvatarSize
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Stream

@Composable
internal fun PreCallComponent(
    uiState: PreCallUiState,
    subtitle: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(modifier = modifier.fillMaxSize()) {
            content?.invoke(this)

            val video = uiState.stream?.video

            if (video?.view != null && video.isEnabled) {
                Stream(streamView = video.view, avatar = uiState.stream.avatar)
            } else {
                Avatar(
                    uri = ImmutableUri(Uri.EMPTY),
                    contentDescription = stringResource(id = R.string.kaleyra_avatar),
                    placeholder = if (uiState.isGroupCall) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar_bold,
                    error = if (uiState.isGroupCall) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar_bold,
                    contentColor = LocalContentColor.current,
                    backgroundColor = colorResource(id = R.color.kaleyra_color_background_dark),
                    size = DefaultStreamAvatarSize,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // TODO how to show connecting when user answered on dialing?
            CallInfoWidget(
                onBackPressed = onBackPressed,
                title = uiState.participants.joinToString(separator = ", "),
                subtitle = subtitle,
                watermarkInfo = uiState.watermarkInfo,
                recording = false,
                modifier = Modifier.statusBarsPadding()
            )
        }
    }
}