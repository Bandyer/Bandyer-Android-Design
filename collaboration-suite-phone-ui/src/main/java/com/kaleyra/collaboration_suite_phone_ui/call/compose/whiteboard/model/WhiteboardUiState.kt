package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model

import android.view.View
import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessages

@Immutable
internal data class WhiteboardUiState(
    val whiteboardView: View? = null,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val upload: WhiteboardUploadUi? = null,
    val text: String? = null,
    override val userMessages: UserMessages = UserMessages()
) : UiState

