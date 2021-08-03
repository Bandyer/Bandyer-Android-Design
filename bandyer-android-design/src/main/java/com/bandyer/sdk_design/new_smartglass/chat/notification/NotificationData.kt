package com.bandyer.sdk_design.new_smartglass.chat.notification

import androidx.annotation.DrawableRes

data class NotificationData(val name: String, val userAlias: String, val message: String? = null, @DrawableRes val imageRes: Int? = null)