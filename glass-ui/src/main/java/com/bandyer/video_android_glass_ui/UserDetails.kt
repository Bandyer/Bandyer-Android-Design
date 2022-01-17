package com.bandyer.video_android_glass_ui

import android.net.Uri

data class UserDetails(
    val userAlias: String,
    val nickName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val avatarUri: Uri? = null,
    val avatarUrl: String? = null,
    val avatarResId: Int? = null
)

data class UserDetailsFormatters(
    val notificationFormatter: NotificationFormatter,
    val callFormatter: CallFormatter,
    val chatFormatter: ChatFormatter
)

interface NotificationFormatter {
    val contactsFormat: (List<UserDetails>) -> String
}

interface ChatFormatter {
    val contactsFormat: (List<UserDetails>) -> String
}

interface CallFormatter {
    val ringingFormat: (List<UserDetails>) -> String

    val dialingFormat: (List<UserDetails>) -> String

    val streamFormat: (UserDetails) -> String

    val participantFormat: (UserDetails) -> String
}

fun api() {
    val notificationFormatter = object : NotificationFormatter {
        override val contactsFormat = { it: List<UserDetails> -> "${it.first().nickName} and other ${it.count() - 1}" }
    }

    val callFormatter = object : CallFormatter {
        override val ringingFormat = { it: List<UserDetails> -> "${it.first().nickName} and other ${it.count() - 1}" }
        override val dialingFormat = { it: List<UserDetails> -> "${it.first().nickName} and other ${it.count() - 1}" }
        override val streamFormat = { it: UserDetails -> "${it.firstName} ${it.lastName}" }
        override val participantFormat = { it: UserDetails -> "${it.firstName} ${it.lastName}" }
    }

    val chatFormatter = object : ChatFormatter {
        override val contactsFormat = { it: List<UserDetails> -> "${it.first().nickName} and other ${it.count() - 1}" }
    }

    UserDetailsFormatters(notificationFormatter, callFormatter, chatFormatter)
}

data class CallUserDetails(
    val data: List<UserDetails>,
    val formatter: CallFormatter? = null
)