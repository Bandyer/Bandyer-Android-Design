package com.bandyer.video_android_glass_ui

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

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

//@Parcelize
class UserDetailsDelegate {
    // TODO make this mandatory
    var data: List<UserDetails>? = null

    // TODO make this mandatory
    var defaultFormatter: Formatter? = null

    var callFormatter: Formatter? = defaultFormatter

    var chatFormatter: Formatter? = defaultFormatter

    var notificationFormatter: Formatter? = defaultFormatter
}

fun userDetailsDelegate(lambda: UserDetailsDelegate.() -> Unit) =
    UserDetailsDelegate().apply(lambda)

typealias Formatter = (userDetails: List<UserDetails>) -> String

fun test() {
    userDetailsDelegate {
        data = listOf(
            UserDetails("ste1", "Mario", "Mario", "Rossi", "mario@gmail.com", null, null, null),
            UserDetails("ste2", "Luigi", "Luigi", "Gialli", "luigi@gmail.com", null, "https://randomuser.me/api/portraits/men/86.jpg", null)
        )
        defaultFormatter = { userDetails ->
            if (userDetails.count() > 1) {
                var text = ""
                userDetails.forEach { text += "${it.firstName} ${it.lastName}, " }
                text
            } else "${userDetails.first().firstName} ${userDetails.first().lastName}"
        }
    }
}