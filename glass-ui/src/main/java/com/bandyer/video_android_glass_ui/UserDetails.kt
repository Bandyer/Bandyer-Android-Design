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
    val defaultFormatter: UserDetailsFormatter,
    val callFormatter: UserDetailsFormatter = defaultFormatter,
    val chatFormatter: UserDetailsFormatter = defaultFormatter,
    val notificationFormatter: UserDetailsFormatter = defaultFormatter
)

interface UserDetailsFormatter {
    val singleDetailsFormat: (UserDetails) -> String
    val groupDetailsFormat: (List<UserDetails>) -> String
}

fun api() {
    val formatter = object : UserDetailsFormatter {
        override val singleDetailsFormat = { it: UserDetails -> "${it.firstName} ${it.lastName}" }
        override val groupDetailsFormat = { it: List<UserDetails> -> "${it.first().nickName} and other ${it.count() - 1}" }
    }

    UserDetailsFormatters(formatter)
}

data class UserDetailsWrapper(
    val data: List<UserDetails>,
    val formatters: UserDetailsFormatters
)