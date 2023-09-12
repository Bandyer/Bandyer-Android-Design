package com.kaleyra.collaboration_suite_phone_ui.call.component.fileshare.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri

@Immutable
data class SharedFileUi(
    val id: String,
    val name: String,
    val uri: ImmutableUri,
    val size: Long?,
    val sender: String,
    val time: Long,
    val state: State,
    val isMine: Boolean
) {

    @Immutable
    sealed class State {
        object Available : State()
        object Pending : State()
        data class InProgress(val progress: Float) : State()
        data class Success(val uri: ImmutableUri) : State()
        object Error : State()
        object Cancelled : State()
    }
}

