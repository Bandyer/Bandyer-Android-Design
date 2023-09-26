package com.kaleyra.collaboration_suite_phone_ui.common.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.UserMessage
import kotlinx.coroutines.flow.Flow

interface UserMessageViewModel {

    val userMessage: Flow<UserMessage>

}