package com.kaleyra.collaboration_suite_phone_ui.call.compose

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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.DefaultStreamAvatarSize
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo

@Composable
internal fun PreCallComponent(
    title: String,
    subtitle: String?,
    watermarkInfo: WatermarkInfo?,
    groupCall: Boolean,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    stream: StreamUi? = null,
    content: @Composable BoxScope.() -> Unit
) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(modifier = modifier.fillMaxSize()) {
            content()

            if (stream?.video?.view != null && stream.video.isEnabled) {
                Stream(streamView = stream.video.view, avatar = stream.avatar)
            } else {
                Avatar(
                    uri = ImmutableUri(Uri.EMPTY),
                    contentDescription = stringResource(id = R.string.kaleyra_avatar),
                    placeholder = if (groupCall) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar_bold,
                    error = if (groupCall) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar_bold,
                    contentColor = LocalContentColor.current,
                    backgroundColor = colorResource(id = R.color.kaleyra_color_background_dark),
                    size = DefaultStreamAvatarSize,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            CallInfoWidget(
                onBackPressed = onBackPressed,
                title = title,
                subtitle = subtitle,
                watermarkInfo = watermarkInfo,
                recording = false,
                modifier = Modifier.statusBarsPadding()
            )
        }
    }
}