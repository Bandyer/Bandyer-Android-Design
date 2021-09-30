package com.bandyer.video_android_glass_ui.participants

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.bandyer.video_android_glass_ui.common.UserState
import kotlinx.parcelize.Parcelize

/**
 * ParticipantData
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
data class ParticipantData(
    val name: String,
    val userAlias: String,
    val userState: UserState,
    @DrawableRes val avatarImageId: Int? = null,
    val avatarImageUrl: String? = null,
    val lastSeenTime: Long = 0
) : Parcelable