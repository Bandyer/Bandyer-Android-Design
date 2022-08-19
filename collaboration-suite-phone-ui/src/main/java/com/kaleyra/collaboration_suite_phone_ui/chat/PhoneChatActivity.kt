@file:OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat

import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutEventListener
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId
import kotlinx.coroutines.launch

internal class PhoneChatActivity : ChatActivity() {

    override val viewModel: PhoneChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                ChatScreen(onBackPressed = { finishAndRemoveTask() }, viewModel = viewModel)
            }
        }
    }
}

@Composable
internal fun ChatScreen(
    onBackPressed: () -> Unit,
    viewModel: ChatComposeViewModel
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val showFab by scrollState.shouldShowFab()

    val lazyColumnItems = viewModel.conversationItems.collectAsState(initial = emptyList()).value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                testTagsAsResourceId = true
            }) {
        TopAppBar(
            stateInfo = viewModel.stateInfo.collectAsState(
                initial = StateInfo(
                    State.None,
                    Info.Empty
                )
            ).value,
            onBackPressed = onBackPressed,
            actions = viewModel.chatActions.collectAsState(initial = setOf()).value.mapToClickableAction(makeCall = { viewModel.call(it) })
        )

        Box(Modifier.weight(1f)) {
            if (!viewModel.areMessagesFetched.collectAsState(initial = false).value) LoadingMessagesLabel()
            else if (lazyColumnItems.isEmpty()) NoMessagesLabel()
            else Messages(
                items = lazyColumnItems,
                onFetch = { viewModel.fetchMessages() },
                scrollState = scrollState,
                onMessageItemScrolled = { viewModel.onMessageScrolled(it) },
                onNewMessageItems = { viewModel.readAllMessages() },
                modifier = Modifier.fillMaxSize()
            )

            this@Column.AnimatedVisibility(
                visible = showFab,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                ScrollToBottomFab(
                    counter = viewModel.unseenMessagesCount.collectAsState(0).value,
                    onClick = {
                        scope.launch { scrollState.scrollToItem(0) }
                        viewModel.onAllMessagesScrolled()
                    }
                )
            }

            if (viewModel.isCallActive.collectAsState(initial = false).value)
                OngoingCallLabel(onClick = { viewModel.showCall() })
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                val themeResId =
                    it.theme.getAttributeResourceId(R.attr.kaleyra_chatInputWidgetStyle)
                KaleyraChatInputLayoutWidget(ContextThemeWrapper(it, themeResId))
            },
            update = {
                it.callback = object : KaleyraChatInputLayoutEventListener {
                    override fun onTextChanged(text: String) = Unit

                    override fun onSendClicked(text: String) {
                        viewModel.sendMessage(text)
                    }
                }
            }
        )
    }
}

@Composable
private fun LazyListState.shouldShowFab(): androidx.compose.runtime.State<Boolean> =
    remember { derivedStateOf { firstVisibleItemIndex > 0 && firstVisibleItemScrollOffset > 0 } }

private fun Set<Action>.mapToClickableAction(makeCall: (CallType) -> Unit): Set<ClickableAction> {
    return map {
        when (it) {
            is Action.AudioCall -> ClickableAction(it) { makeCall(CallType.Audio) }
            is Action.AudioUpgradableCall -> ClickableAction(it) { makeCall(CallType.AudioUpgradable) }
            else -> ClickableAction(it) { makeCall(CallType.Video) }
        }
    }.toSet()
}

@Preview
@Composable
private fun NoMessagesLabel() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_chat_no_messages),
            style = MaterialTheme.typography.body2,
        )
    }
}

@Preview
@Composable
private fun LoadingMessagesLabel() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_chat_channel_loading),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun OngoingCallLabel(onClick: () -> Unit) {
    Text(
        text = stringResource(id = R.string.kaleyra_ongoing_call_label),
        color = Color.White,
        style = MaterialTheme.typography.body2,
        modifier = Modifier
            .clickable(onClick = onClick, role = Role.Button)
            .fillMaxWidth()
            .background(
                shape = RectangleShape,
                color = colorResource(id = R.color.kaleyra_color_answer_button)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

