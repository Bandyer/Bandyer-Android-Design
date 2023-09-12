package com.kaleyra.collaboration_suite_phone_ui.call.component.precall

import android.net.Uri
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.StreamViewSettings.featuredSettings
import com.kaleyra.collaboration_suite_phone_ui.call.component.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.streams.CallInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.call.streams.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.streams.StreamContainer
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.view.UserMessageSnackbarHandler

@Composable
internal fun <T: PreCallUiState<T>> PreCallComponent(
    uiState: T,
    subtitle: String,
    userMessage: UserMessage? = null,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(modifier = modifier.fillMaxSize()) {
            StreamContainer {
                val isGroupCall = uiState.participants.count() > 1
                Stream(
                    streamView = uiState.video?.view?.featuredSettings() ?: ImmutableView(View(LocalContext.current)),
                    avatar = if (isGroupCall) ImmutableUri(Uri.EMPTY) else uiState.avatar,
                    avatarPlaceholder = if (isGroupCall) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar_bold,
                    avatarError = if (isGroupCall) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar_bold,
                    avatarVisible = (uiState.video == null && !uiState.isVideoIncoming) || (uiState.video != null && uiState.video?.view == null && uiState.video?.isEnabled == false) || uiState.video?.isEnabled == false
                )
            }

            Column {
                CallInfoWidget(
                    onBackPressed = onBackPressed,
                    title = uiState.participants.value.joinToString(separator = ", "),
                    subtitle = if (!uiState.isLink && !uiState.isConnecting) subtitle else stringResource(id = R.string.kaleyra_call_status_connecting),
                    watermarkInfo = uiState.watermarkInfo,
                    recording = false,
                    modifier = Modifier.statusBarsPadding()
                )

                UserMessageSnackbarHandler(userMessage = userMessage)
            }

            content?.invoke(this)
        }
    }
}