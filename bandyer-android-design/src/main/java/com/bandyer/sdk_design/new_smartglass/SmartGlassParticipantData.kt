package com.bandyer.sdk_design.new_smartglass

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SmartGlassParticipantData(
    val name: String,
    val userAlias: String,
    val userState: UserState,
    @DrawableRes val avatarImageId: Int? = null,
    val avatarImageUrl: String? = null,
    val lastSeenTime: Long = 0
) : Parcelable {
    enum class UserState {
        ONLINE,
        INVITED,
        OFFLINE
    }

}