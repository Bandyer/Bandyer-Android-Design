package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class Logo(
    val light: Uri? = null,
    val dark: Uri? = null
)