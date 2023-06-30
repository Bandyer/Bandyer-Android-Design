package com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessage
import kotlinx.coroutines.flow.Flow

interface UserMessageViewModel {

    val userMessage: Flow<UserMessage>

}