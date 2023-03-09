package com.kaleyra.collaboration_suite_core_ui.termsandconditions

sealed interface TermsAndConditionsConfig {

    val title: String

    val message: String

    data class NotificationConfig(
        override val title: String,
        override val message: String,
        val dismissCallback: () -> Unit,
        val enableFullscreen: Boolean = false,
        val timeout: Long? = null
    ) : TermsAndConditionsConfig

    data class ActivityConfig(
        override val title: String,
        override val message: String,
        val acceptText: String,
        val declineText: String,
        val acceptCallback: () -> Unit,
        val declineCallback: () -> Unit
    ) : TermsAndConditionsConfig
}