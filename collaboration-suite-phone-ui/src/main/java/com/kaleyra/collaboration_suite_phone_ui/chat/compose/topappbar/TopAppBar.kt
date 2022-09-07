package com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar

import android.net.Uri
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.mockClickableActions
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId

const val ActionsTag = "ActionsTag"

internal typealias ClickableAction = Pair<ChatAction, () -> Unit>

@Composable
internal fun TopAppBar(
    state: ChatState,
    info: ChatInfo,
    actions: Set<ClickableAction>,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = {
                        val themeResId =
                            it.theme.getAttributeResourceId(R.attr.kaleyra_chatInfoWidgetStyle)
                        KaleyraChatInfoWidget(ContextThemeWrapper(it, themeResId))
                    },
                    update = {
                        it.contactNameView!!.text = info.name
                        it.contactNameView!!.visibility = View.VISIBLE
                        it.contactStatusView!!.visibility = View.VISIBLE
                        it.state = when {
                            state is ChatState.NetworkState.Offline -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.WAITING_FOR_NETWORK()
                            state is ChatState.NetworkState.Connecting -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.CONNECTING()
                            state is ChatState.UserState.Online -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.ONLINE()
                            state is ChatState.UserState.Offline && state.timestamp != null -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.OFFLINE(
                                state.timestamp
                            )
                            state is ChatState.UserState.Typing -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.TYPING()
                            else -> null
                        }
                        if (info.image != Uri.EMPTY) it.contactImageView!!.setImageUri(info.image)
                    }
                )
            }

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
internal fun Actions(actions: Set<ClickableAction>) {
    actions.getClickableAction<ChatAction.AudioCall>()?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_audio_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_audio_call)
        )
    }

    actions.getClickableAction<ChatAction.AudioUpgradableCall>()?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_audio_upgradable_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_audio_upgradable_call)
        )
    }

    actions.getClickableAction<ChatAction.VideoCall>()?.let { (_, onClick) ->
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
internal fun TopAppBarPreview() {
    KaleyraTheme {
        TopAppBar(
            state = ChatState.UserState.Online,
            info = ChatInfo("John Smith"),
            actions = mockClickableActions,
            onBackPressed = { }
        )
    }
}

@Preview
@Composable
internal fun TopAppBarDarkPreview() {
    KaleyraTheme(isDarkTheme = true) {
        TopAppBar(
            state = ChatState.UserState.Online,
            info = ChatInfo("John Smith"),
            actions = mockClickableActions,
            onBackPressed = { }
        )
    }
}