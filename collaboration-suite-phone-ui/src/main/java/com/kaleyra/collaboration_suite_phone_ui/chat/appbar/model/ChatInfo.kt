package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri

// Image is nullable for testing purpose. It is not possible
// to mock a static field, since it has no getter.
@Immutable
data class ChatInfo(
    val name: String = "",
    val image: ImmutableUri? = null
)