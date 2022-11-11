@file:OptIn(ExperimentalFoundationApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat.topappbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.IconButton
import com.kaleyra.collaboration_suite_phone_ui.chat.custom.MarqueeText
import com.kaleyra.collaboration_suite_phone_ui.chat.custom.TypingDots
import com.kaleyra.collaboration_suite_phone_ui.chat.model.*
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

internal const val SubtitleTag = "SubtitleTag"
internal const val BouncingDots = "BouncingDots"

@Composable
internal fun ChatAppBar(
    state: ChatState,
    info: ChatInfo,
    actions: ImmutableSet<ChatAction>,
    onBackPressed: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                icon = Icons.Filled.ArrowBack,
                iconDescription = stringResource(id = R.string.kaleyra_back),
                onClick = onBackPressed
            )
        },
        content = { ChatDetails(info, state) },
        actions = { Actions(actions = actions) }
    )
}

@Composable
internal fun ChatDetails(info: ChatInfo, state: ChatState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val placeholderFilter = ColorFilter.tint(color = MaterialTheme.colors.onPrimary)
        var colorFilter by remember { mutableStateOf<ColorFilter?>(placeholderFilter) }
        AsyncImage(
            model = info.image,
            contentDescription = stringResource(id = R.string.kaleyra_chat_avatar_desc),
            modifier = Modifier
                .clip(CircleShape)
                .background(color = colorResource(R.color.kaleyra_color_grey_light))
                .size(40.dp),
            placeholder = painterResource(R.drawable.ic_kaleyra_avatar),
            error = painterResource(R.drawable.ic_kaleyra_avatar),
            contentScale = ContentScale.Crop,
            onSuccess = { colorFilter = null },
            colorFilter = colorFilter
        )
        Column(Modifier.padding(start = 12.dp)) {
            MarqueeText(
                text = info.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                gradientEdgeColor = MaterialTheme.colors.primary
            )
            Row {
                MarqueeText(
                    text = textFor(state),
                    fontSize = 12.sp,
                    gradientEdgeColor = MaterialTheme.colors.primary,
                    color = LocalContentColor.current.copy(alpha = 0.5f),
                    textModifier = Modifier.testTag(SubtitleTag)
                )
                if (state is ChatState.UserState.Typing) {
                    TypingDots(
                        color = LocalContentColor.current.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(start = 4.dp, bottom = 4.dp)
                            .testTag(BouncingDots)
                    )
                }
            }
        }
    }
}

@Composable
private fun textFor(state: ChatState): String =
    when (state) {
        is ChatState.NetworkState.Offline -> stringResource(R.string.kaleyra_chat_state_waiting_for_network)
        is ChatState.NetworkState.Connecting -> stringResource(R.string.kaleyra_chat_state_connecting)
        is ChatState.UserState.Online -> stringResource(R.string.kaleyra_chat_user_status_online)
        is ChatState.UserState.Offline -> {
            val timestamp = state.timestamp
            if (timestamp == null) stringResource(R.string.kaleyra_chat_user_status_offline)
            else {
                stringResource(
                    R.string.kaleyra_chat_user_status_last_login,
                    TimestampUtils.parseTimestamp(LocalContext.current, timestamp)
                )
            }
        }
        is ChatState.UserState.Typing -> stringResource(R.string.kaleyra_chat_user_status_typing)
        else -> ""
    }

@Composable
internal fun Actions(actions: ImmutableSet<ChatAction>) {
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

@Preview
@Composable
internal fun TopAppBarPreview() = KaleyraTheme {
    ChatAppBar(
        state = ChatState.UserState.Typing,
        info = ChatInfo("John Smith"),
        actions = mockActions,
        onBackPressed = { }
    )
}

@Preview
@Composable
internal fun TopAppBarDarkPreview() = KaleyraTheme(isDarkTheme = true) {
    ChatAppBar(
        state = ChatState.UserState.Offline(5654635),
        info = ChatInfo("John Smith"),
        actions = mockActions,
        onBackPressed = { }
    )
}