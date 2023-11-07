package com.kaleyra.video_common_ui.contactdetails.provider

import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails

internal interface ContactDetailsProvider {

    suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long = FETCH_TIMEOUT
    ): Set<ContactDetails>

    companion object {
        const val FETCH_TIMEOUT = 2500L
    }
}