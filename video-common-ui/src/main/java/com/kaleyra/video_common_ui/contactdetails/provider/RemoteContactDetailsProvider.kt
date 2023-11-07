package com.kaleyra.video_common_ui.contactdetails.provider

import com.kaleyra.video.Contacts
import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import kotlinx.coroutines.coroutineScope

internal class RemoteContactDetailsProvider(val contacts: Contacts) : ContactDetailsProvider {

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> = coroutineScope {
        runCatching {
            val userContacts = userIds.mapNotNull { contacts.get(it).getOrNull() }
            userContacts.map { contact -> ContactDetails(userId = contact.userId, name = contact.displayName, image = contact.displayImage) }
        }.getOrNull()?.toSet() ?: setOf()
    }

}