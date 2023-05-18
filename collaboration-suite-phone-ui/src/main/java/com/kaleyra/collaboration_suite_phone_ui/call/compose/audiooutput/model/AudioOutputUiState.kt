package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessages
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
internal data class AudioOutputUiState(
    val audioDeviceList: ImmutableList<AudioDeviceUi> = ImmutableList(emptyList()),
    val playingDeviceId: String? = null,
    override val userMessages: UserMessages = UserMessages()
) : UiState