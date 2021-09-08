package com.bandyer.sdk_design.new_smartglass.contact

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

/**
 * BandyerContactData
 *
 * @property name The name
 * @property userAlias The user alias
 * @property userState UserState
 * @property avatarImageId The local avatar resource
 * @property avatarImageUrl The remote avatar url resource
 * @property lastSeenTime The last time the user was online expressed as a long timestamp
 * @constructor
 */
@Keep
@Parcelize
data class BandyerContactData(
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