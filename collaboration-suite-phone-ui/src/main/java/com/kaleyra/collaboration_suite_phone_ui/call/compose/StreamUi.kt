package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.net.Uri
import android.view.View
import androidx.compose.runtime.Immutable

@Immutable
data class StreamUi(
    val view: View?,
    val username: String,
    val avatar: Uri?,
    val videoEnabled: Boolean
)