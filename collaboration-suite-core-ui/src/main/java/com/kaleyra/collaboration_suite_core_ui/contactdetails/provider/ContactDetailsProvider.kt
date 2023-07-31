package com.kaleyra.collaboration_suite_core_ui.contactdetails.provider

import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails

internal interface ContactDetailsProvider {

    suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long = FETCH_TIMEOUT
    ): Set<ContactDetails>

    companion object {
        const val FETCH_TIMEOUT = 2500L
    }
}