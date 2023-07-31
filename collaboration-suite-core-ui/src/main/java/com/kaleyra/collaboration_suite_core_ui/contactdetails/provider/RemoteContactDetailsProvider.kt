package com.kaleyra.collaboration_suite_core_ui.contactdetails.provider

import com.kaleyra.collaboration_suite.Contacts
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope

internal class RemoteContactDetailsProvider(val contacts: Contacts, val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ContactDetailsProvider {

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