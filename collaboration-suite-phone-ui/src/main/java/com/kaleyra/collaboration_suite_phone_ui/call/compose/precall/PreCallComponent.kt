package com.kaleyra.collaboration_suite_phone_ui.call.compose.precall

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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamViewSettings.featuredSettings
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.DefaultStreamAvatarSize
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamContainer

@Composable
internal fun <T: PreCallUiState<T>> PreCallComponent(
    uiState: T,
    subtitle: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(modifier = modifier.fillMaxSize()) {
            if (uiState.video?.view != null && uiState.video?.isEnabled == true) {
                StreamContainer {
                    Stream(streamView = uiState.video?.view?.featuredSettings(), avatar = null, avatarVisible = false)
                }
            } else {
                val isGroupCall = uiState.participants.count() > 1
                Avatar(
                    uri = if (isGroupCall) ImmutableUri(Uri.EMPTY) else uiState.avatar,
                    contentDescription = stringResource(id = R.string.kaleyra_avatar),
                    placeholder = if (isGroupCall) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar_bold,
                    error = if (isGroupCall) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar_bold,
                    contentColor = LocalContentColor.current,
                    backgroundColor = colorResource(id = R.color.kaleyra_color_background_dark),
                    size = DefaultStreamAvatarSize,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            CallInfoWidget(
                onBackPressed = onBackPressed,
                title = uiState.participants.value.joinToString(separator = ", "),
                subtitle = if (!uiState.isLink && !uiState.isConnecting) subtitle else stringResource(id = R.string.kaleyra_call_status_connecting),
                watermarkInfo = uiState.watermarkInfo,
                recording = false,
                modifier = Modifier.statusBarsPadding()
            )

            content?.invoke(this)
        }
    }
}