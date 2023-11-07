package com.kaleyra.video_sdk.common.viewmodel

import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import kotlinx.coroutines.flow.Flow

interface UserMessageViewModel {

    val userMessage: Flow<UserMessage>

}