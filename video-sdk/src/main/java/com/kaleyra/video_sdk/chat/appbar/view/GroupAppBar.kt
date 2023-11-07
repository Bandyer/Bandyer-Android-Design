package com.kaleyra.video_sdk.chat.appbar.view

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantsState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.appbar.model.mockActions
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableMap
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableSet
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun GroupAppBar(
    image: ImmutableUri,
    name: String,
    connectionState: ConnectionState,
    participantsDetails: ImmutableMap<String, ChatParticipantDetails>,
    participantsState: ChatParticipantsState,
    isInCall: Boolean,
    actions: ImmutableSet<ChatAction>,
    onBackPressed: () -> Unit = { }
) {
    ChatAppBar(actions = actions, isInCall = isInCall, onBackPressed = onBackPressed) {
        ChatAppBarContent(
            image = image,
            title = name,
            subtitle = textFor(connectionState, participantsState, participantsDetails),
            typingDots = participantsState.typing.count() > 0
        )
    }
}

@Composable
private fun textFor(
    connectionState: ConnectionState,
    participantsState: ChatParticipantsState,
    participantsDetails: ImmutableMap<String, ChatParticipantDetails>
): String {
    val typingCount = participantsState.typing.count()
    val onlineCount = participantsState.online.count()
    return when {
        connectionState is ConnectionState.Offline -> stringResource(R.string.kaleyra_chat_state_waiting_for_network)
        connectionState is ConnectionState.Connecting -> stringResource(R.string.kaleyra_chat_state_connecting)
        typingCount == 1 -> pluralStringResource(
            id = R.plurals.kaleyra_call_participants_typing,
            count = 1,
            participantsState.typing.value.first()
        )

        typingCount > 1 -> pluralStringResource(
            id = R.plurals.kaleyra_call_participants_typing,
            count = typingCount,
            typingCount
        )

        onlineCount > 0 -> stringResource(
            R.string.kaleyra_chat_participants_online,
            onlineCount,
            onlineCount
        )

        else -> participantsDetails.value.values.joinToString(", ") { it.username }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun GroupAppBarPreview() = KaleyraTheme {
    GroupAppBar(
        image = ImmutableUri(),
        name = "Trip Crashers",
        connectionState = ConnectionState.Connected,
        participantsDetails = ImmutableMap(
            mapOf(
                "userId1" to ChatParticipantDetails("John Smith"),
                "userId2" to ChatParticipantDetails("Jack Daniels")
            )
        ),
        participantsState = ChatParticipantsState(),
        isInCall = false,
        actions = mockActions,
        onBackPressed = { }
    )
}