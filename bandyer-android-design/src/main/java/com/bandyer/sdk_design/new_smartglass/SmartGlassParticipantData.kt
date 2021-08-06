package com.bandyer.sdk_design.new_smartglass

import androidx.annotation.DrawableRes

data class SmartGlassParticipantData(
    val name: String,
    val userAlias: String,
    val userState: UserState,
    @DrawableRes val avatarImageId: Int? = null,
    val avatarImageUrl: String? = null,
    val lastSeenTime: Long = 0
) {
    enum class UserState {
        ONLINE,
        INVITED,
        OFFLINE
    }
}