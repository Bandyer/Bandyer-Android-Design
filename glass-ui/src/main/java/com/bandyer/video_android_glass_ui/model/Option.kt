package com.bandyer.video_android_glass_ui.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
sealed class Option : Parcelable {
    @Parcelize data class MICROPHONE(val toggled: Boolean): Option()
    @Parcelize data class CAMERA(val toggled: Boolean): Option()
    @Parcelize object ZOOM: Option()
    @Parcelize object PARTICIPANTS: Option()
    @Parcelize object CHAT: Option()
}