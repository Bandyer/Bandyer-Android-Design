package com.kaleyra.video_sdk.call.callinfowidget.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class Logo(
    val light: Uri? = null,
    val dark: Uri? = null
)