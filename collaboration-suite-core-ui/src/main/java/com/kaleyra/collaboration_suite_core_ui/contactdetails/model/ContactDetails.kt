package com.kaleyra.collaboration_suite_core_ui.contactdetails.model

import android.net.Uri

data class ContactDetails(
    val userId: String,
    val name: String? = null,
    val image: Uri? = null
)