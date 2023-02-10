package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri

@Immutable
data class SharedFileUi(
    val id: String,
    val file: FileUi,
    val sender: String,
    val time: Long,
    val state: State,
    val type: Type
) {

    @Immutable
    enum class Type {
        Upload,
        Download
    }

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

