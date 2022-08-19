package com.kaleyra.collaboration_suite_phone_ui.chat

import android.net.Uri
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId

//@Preview
//@Composable
//internal fun TopAppBarPreview() {
//    MaterialTheme {
//        Row(Modifier.fillMaxWidth()) {
//            TopAppBar(
//                stateInfo = StateInfo(State.UserState.Online, Info("John Smith", Uri.EMPTY)),
//                actions = setOf(),
//                onBackPressed = { }
//            )
//        }
//    }
//}

internal typealias ClickableAction = Pair<Action, () -> Unit>

@Composable
internal fun TopAppBar(
    stateInfo: StateInfo,
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
                        val (state, info) = stateInfo
                        it.contactNameView!!.text = info.title
                        it.contactNameView!!.visibility = View.VISIBLE
                        it.contactStatusView!!.visibility = View.VISIBLE
                        it.state = when {
                            state is State.NetworkState.Offline -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.WAITING_FOR_NETWORK()
                            state is State.NetworkState.Connecting -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.CONNECTING()
                            state is State.UserState.Online -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.ONLINE()
                            state is State.UserState.Offline && state.timestamp != null -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.OFFLINE(
                                state.timestamp
                            )
                            state is State.UserState.Typing -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.TYPING()
                            else -> null
                        }
                        if (info.image != Uri.EMPTY) it.contactImageView!!.setImageUri(info.image)
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = { Actions(actions = actions) }
            )
        }
    }
}

@Composable
internal fun Actions(actions: Set<ClickableAction>) {
    actions.getClickableAction<Action.AudioCall>()?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_audio_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_audio_call)
        )
    }

    actions.getClickableAction<Action.AudioUpgradableCall>()?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_audio_upgradable_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_audio_upgradable_call)
        )
    }

    actions.getClickableAction<Action.VideoCall>()?.let { (_, onClick) ->
        MenuIcon(
            painter = painterResource(R.drawable.ic_kaleyra_video_call),
            onClick = onClick,
            contentDescription = stringResource(id = R.string.kaleyra_start_video_call)
        )
    }
}

private inline fun <reified T : Action> Set<ClickableAction>.getClickableAction(): ClickableAction? = firstOrNull { (act, _) -> act is T }
