package com.kaleyra.collaboration_suite_core_ui.termsandconditions.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TermsAndConditions(val title: String, val message: String, val acceptText: String, val declineText: String): Parcelable