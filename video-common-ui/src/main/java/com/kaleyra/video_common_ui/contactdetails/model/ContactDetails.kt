package com.kaleyra.video_common_ui.contactdetails.model

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal data class ContactDetails(
    val userId: String,
    val name: StateFlow<String?> = MutableStateFlow(null),
    val image: StateFlow<Uri?> = MutableStateFlow(null)
)