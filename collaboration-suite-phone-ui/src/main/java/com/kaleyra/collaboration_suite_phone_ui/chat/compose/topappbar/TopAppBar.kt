@file:OptIn(ExperimentalFoundationApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.custom.MarqueeText
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.custom.TypingDots
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.*
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.theme.KaleyraTheme

const val SubtitleTag = "SubtitleTag"
const val BouncingDots = "BouncingDots"
const val ActionsTag = "ActionsTag"

@Composable
internal fun TopAppBar(
    state: ChatState,
    info: ChatInfo,
    actions: ImmutableSet<ClickableAction>,
    onBackPressed: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier
            .focusGroup(),
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = { NavigationIcon(onBackPressed = onBackPressed) }
            )

            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                content = { ChatDetails(info, state) }
            )

            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .testTag(ActionsTag),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = { Actions(actions = actions) }
            )
        }
    }
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
            if (state.timestamp.isNullOrBlank()) stringResource(R.string.kaleyra_chat_user_status_offline)
            else stringResource(R.string.kaleyra_chat_user_status_last_login, state.timestamp)
        }
        is ChatState.UserState.Typing -> stringResource(R.string.kaleyra_chat_user_status_typing)
        else -> ""
    }

@Composable
internal fun Actions(actions: ImmutableSet<ClickableAction>) {
    val value = actions.value
    value.getClickableAction<ChatAction.AudioCall>()?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_audio_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_audio_call)
        )
    }

    value.getClickableAction<ChatAction.AudioUpgradableCall>()?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_audio_upgradable_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_audio_upgradable_call)
        )
    }

    value.getClickableAction<ChatAction.VideoCall>()?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_video_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_video_call)
        )
    }
}

private inline fun <reified T : ChatAction> Set<ClickableAction>.getClickableAction(): ClickableAction? =
    firstOrNull { (act, _) -> act is T }

@Preview
@Composable
internal fun TopAppBarPreview() = KaleyraTheme {
    TopAppBar(
        state = ChatState.UserState.Typing,
        info = ChatInfo("John Smith"),
        actions = mockClickableActions,
        onBackPressed = { }
    )
}

@Preview
@Composable
internal fun TopAppBarDarkPreview() = KaleyraTheme(isDarkTheme = true) {
    TopAppBar(
        state = ChatState.UserState.Offline("15:00"),
        info = ChatInfo("John Smith"),
        actions = mockClickableActions,
        onBackPressed = { }
    )
}