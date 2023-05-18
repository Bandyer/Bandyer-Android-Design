package com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessages

// TODO move to common package between call and chat
interface UiState {
    val userMessages: UserMessages
}