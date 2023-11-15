package com.kaleyra.demo_video_sdk.ui.custom_views

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@Parcelize
data class ChatConfiguration(
    val audioConfiguration: CallConfiguration? = null,
    val audioUpgradableConfiguration: CallConfiguration? = null,
    val audioVideoConfiguration: CallConfiguration? = null
) : Parcelable {

    fun encode(): String = Json.encodeToString(this)

    companion object {
        fun decode(data: String): ChatConfiguration = Json.decodeFromString(data)
    }
}