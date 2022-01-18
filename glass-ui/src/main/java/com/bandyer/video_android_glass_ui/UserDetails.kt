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

// guardare i plurals
data class UserDetailsFormatters(
    val defaultFormatter: UserDetailsFormatter,
    val callFormatter: UserDetailsFormatter = defaultFormatter,
    val chatFormatter: UserDetailsFormatter = defaultFormatter,
    val notificationFormatter: UserDetailsFormatter = defaultFormatter
)

interface UserDetailsFormatter {
    fun format(vararg userDetails: UserDetails): String
}

fun api() {
    val formatter = object : UserDetailsFormatter {
        override fun format(vararg userDetails: UserDetails): String =
            if(userDetails.count() > 1) "${userDetails.first().nickName} and other ${userDetails.count() - 1}"
            else "${userDetails.first().firstName} ${userDetails.first().lastName}"
    }

    UserDetailsFormatters(formatter)
}

data class UserDetailsWrapper(
    val data: List<UserDetails>,
    val formatters: UserDetailsFormatters
)