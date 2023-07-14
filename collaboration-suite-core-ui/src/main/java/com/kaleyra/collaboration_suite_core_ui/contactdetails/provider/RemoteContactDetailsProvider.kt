package com.kaleyra.collaboration_suite_core_ui.contactdetails.provider

import com.kaleyra.collaboration_suite.Contacts
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

internal class RemoteContactDetailsProvider(val contacts: Contacts, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ContactDetailsProvider {

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> = coroutineScope {
        runCatching {
            withTimeout(timeout) {
                val userContacts = userIds.mapNotNull { contacts.get(it).getOrNull() }
                val contactsDetails = userContacts.map { contact ->
                    async(ioDispatcher) {
                        val name = async { contact.displayName.filterNotNull().first() }
                        val image = async { contact.displayImage.filterNotNull().first() }
                        ContactDetails(userId = contact.userId, name = name.await(), image = image.await())
                    }
                }
                contactsDetails.awaitAll()
            }
        }.getOrNull()?.toSet() ?: setOf()
    }
}