package com.bandyer.video_android_glass_ui.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
sealed class Option : Parcelable {

    companion object {
        val all = listOf(ZOOM, PARTICIPANTS, CHAT)
    }

    @Parcelize object ZOOM: Option()
    @Parcelize object PARTICIPANTS: Option()
    @Parcelize object CHAT: Option()
}