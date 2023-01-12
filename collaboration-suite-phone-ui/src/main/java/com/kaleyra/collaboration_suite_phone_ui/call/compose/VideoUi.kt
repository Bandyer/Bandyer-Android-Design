package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.view.View
import androidx.compose.runtime.Immutable

@Immutable
data class VideoUi(
    val id: String,
    val view: View?,
    val isEnabled: Boolean
)