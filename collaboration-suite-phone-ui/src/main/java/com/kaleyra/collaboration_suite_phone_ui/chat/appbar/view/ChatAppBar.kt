package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.mockActions
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.view.Avatar
import com.kaleyra.collaboration_suite_phone_ui.common.button.BackIconButton
import com.kaleyra.collaboration_suite_phone_ui.common.button.IconButton
import com.kaleyra.collaboration_suite_phone_ui.common.text.Ellipsize
import com.kaleyra.collaboration_suite_phone_ui.common.text.EllipsizeText
import com.kaleyra.collaboration_suite_phone_ui.common.topappbar.TopAppBar
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme

internal const val SubtitleTag = "SubtitleTag"
internal const val BouncingDotsTag = "BouncingDots"
internal const val ChatActionsTag = "ChatActionsTag"

@Composable
internal fun ChatAppBar(
    state: ConnectionState,
    info: ChatInfo,
    isInCall: Boolean,
    actions: ImmutableSet<ChatAction>,
    onBackPressed: () -> Unit,
) {
    TopAppBar(
        navigationIcon = { BackIconButton(onClick = onBackPressed) },
        content = { ChatDetails(info, state) },
        actions = { if (!isInCall) Actions(actions = actions) }
    )
}

@Composable
internal fun ChatDetails(info: ChatInfo, state: ConnectionState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(
            uri = info.image,
            contentDescription = stringResource(id = R.string.kaleyra_chat_avatar_desc),
            placeholder = R.drawable.ic_kaleyra_avatar,
            error = R.drawable.ic_kaleyra_avatar,
            contentColor = MaterialTheme.colors.onPrimary,
            backgroundColor = colorResource(R.color.kaleyra_color_grey_light),
            size = 40.dp
        )
        Column(Modifier.padding(start = 12.dp)) {
            EllipsizeText(
                text = info.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                ellipsize = Ellipsize.Marquee
            )
            Row {
                EllipsizeText(
                    text = textFor(state),
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(alpha = 0.5f),
                    modifier = Modifier.testTag(SubtitleTag),
                    ellipsize = Ellipsize.Marquee
                )
                if (state is ConnectionState.UserState.Typing) {
                    TypingDots(
                        color = LocalContentColor.current.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(start = 4.dp, bottom = 4.dp)
                            .testTag(BouncingDotsTag)
                    )
                }
            }
        }
    }
}

@Composable
private fun textFor(state: ConnectionState): String =
    when (state) {
        is ConnectionState.NetworkState.Offline -> stringResource(R.string.kaleyra_chat_state_waiting_for_network)
        is ConnectionState.NetworkState.Connecting -> stringResource(R.string.kaleyra_chat_state_connecting)
        is ConnectionState.UserState.Online -> stringResource(R.string.kaleyra_chat_user_status_online)
        is ConnectionState.UserState.Offline -> {
            val timestamp = state.timestamp
            if (timestamp == null) stringResource(R.string.kaleyra_chat_user_status_offline)
            else {
                stringResource(
                    R.string.kaleyra_chat_user_status_last_login,
                    TimestampUtils.parseTimestamp(LocalContext.current, timestamp)
                )
            }
        }
        is ConnectionState.UserState.Typing -> stringResource(R.string.kaleyra_chat_user_status_typing)
        else -> ""
    }

@Composable
internal fun Actions(actions: ImmutableSet<ChatAction>) {
    Row(Modifier.testTag(ChatActionsTag)) {
        actions.value.forEach {
            when (it) {
                is ChatAction.AudioCall -> {
                    IconButton(
                        icon = painterResource(R.drawable.ic_kaleyra_audio_call),
                        iconDescription = stringResource(id = R.string.kaleyra_start_audio_call),
                        onClick = it.onClick
                    )
                }
                is ChatAction.AudioUpgradableCall -> {
                    IconButton(
                        icon = painterResource(R.drawable.ic_kaleyra_audio_upgradable_call),
                        iconDescription = stringResource(id = R.string.kaleyra_start_audio_upgradable_call),
                        onClick = it.onClick
                    )
                }
                is ChatAction.VideoCall -> {
                    IconButton(
                        icon = painterResource(R.drawable.ic_kaleyra_video_call),
                        iconDescription = stringResource(id = R.string.kaleyra_start_video_call),
                        onClick = it.onClick
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun TopAppBarPreview() = KaleyraTheme {
    ChatAppBar(
        state = ConnectionState.UserState.Typing,
        info = ChatInfo("John Smith"),
        actions = mockActions,
        isInCall = false,
        onBackPressed = { }
    )
}