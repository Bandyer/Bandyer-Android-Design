package com.kaleyra.collaboration_suite_core_ui.contactdetails.provider

import com.kaleyra.collaboration_suite.Contacts
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

internal class RemoteContactDetailsProvider(val contacts: Contacts) : ContactDetailsProvider {

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> = coroutineScope {
        runCatching {
            withTimeout(timeout) {
                val userContacts = userIds.mapNotNull { contacts.get(it).getOrNull() }
                val contactsDetails = userContacts.map { contact ->
                    async(Dispatchers.IO) {
                        val name = contact.displayName.filterNotNull().first()
                        val image = contact.displayImage.filterNotNull().first()
                        ContactDetails(userId = contact.userId, name = name, image = image)
                    }
                }
                contactsDetails.awaitAll()
            }
        }.getOrNull()?.toSet() ?: setOf()
    }
}