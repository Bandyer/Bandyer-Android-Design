package com.kaleyra.demo_video_sdk.ui.custom_views
data class ChatConfiguration(
    val audioConfiguration: CallConfiguration? = null,
    val audioUpgradableConfiguration: CallConfiguration? = null,
    val audioVideoConfiguration: CallConfiguration? = null
) {

    fun encode(): String = gson.toJson(this)

    companion object {
        fun decode(data: String): ChatConfiguration = gson.fromJson(data, ChatConfiguration::class.java)
    }
}