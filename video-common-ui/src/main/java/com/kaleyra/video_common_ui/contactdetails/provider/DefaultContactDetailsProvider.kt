package com.kaleyra.video_common_ui.contactdetails.provider

import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultContactDetailsProvider : ContactDetailsProvider {

    override suspend fun fetchContactsDetails(vararg userIds: String, timeout: Long): Set<ContactDetails> =
        userIds.map { ContactDetails(userId = it, name = MutableStateFlow(it)) }.toSet()
}