package com.bandyer.video_android_glass_ui.chat.notification

import androidx.annotation.DrawableRes

/**
 * NotificationData
 *
 * @property name The user name
 * @property userAlias The user alias
 * @property message The message sent by the user
 * @property imageRes The local resource id to be set as user avatar
 * @property imageUrl The remote resource url to be set as user avatar
 * @constructor
 */
data class BandyerNotificationData(
    val name: String,
    val userAlias: String,
    val message: String? = null,
    @DrawableRes val imageRes: Int? = null,
    val imageUrl: String? = null
)