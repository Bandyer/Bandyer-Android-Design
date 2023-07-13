package com.kaleyra.collaboration_suite_core_ui.contactdetails.provider

import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout

internal class LocalContactDetailsProvider(val usersDescription: UsersDescription) :
    ContactDetailsProvider {

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> = coroutineScope {
        runCatching {
            withTimeout(timeout) {
                val contactsDetails = userIds.map { userId ->
                    async(Dispatchers.IO) {
                        val name = async { usersDescription.name(listOf(userId)) }
                        val image = async { usersDescription.image(listOf(userId)) }
                        ContactDetails(userId = userId, name = name.await(), image = image.await())
                    }
                }
                contactsDetails.awaitAll()
            }
        }.getOrNull()?.toSet() ?: setOf()
    }
}