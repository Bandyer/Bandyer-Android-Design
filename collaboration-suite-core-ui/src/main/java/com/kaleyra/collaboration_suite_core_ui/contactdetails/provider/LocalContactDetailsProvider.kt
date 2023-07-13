package com.kaleyra.collaboration_suite_core_ui.contactdetails.provider

import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout

internal class LocalContactDetailsProvider(
    val usersDescription: UsersDescription,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContactDetailsProvider {

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> = coroutineScope {
        runCatching {
            withTimeout(timeout) {
                val contactsDetails = userIds.map { userId ->
                    async(ioDispatcher) {
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