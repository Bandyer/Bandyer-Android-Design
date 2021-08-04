package com.bandyer.sdk_design.new_smartglass.chat

import androidx.annotation.DrawableRes

data class SmartGlassChatData(
    val name: String? = null,
    val userAlias: String? = null,
    val message: String? = null,
    val time: Long? = null,
    @DrawableRes val avatar: Int? = null,
    val isFirstMessagePage: Boolean = true
)
