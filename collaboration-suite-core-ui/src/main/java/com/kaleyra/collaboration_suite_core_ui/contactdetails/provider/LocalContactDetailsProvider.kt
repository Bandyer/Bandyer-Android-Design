package com.kaleyra.collaboration_suite_core_ui.contactdetails.provider

import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescriptionProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeout

internal class LocalContactDetailsProvider(
    private val usersDescriptionProvider: UsersDescriptionProvider,
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
                        val (name: String, image: Uri) = usersDescriptionProvider.fetchUserDescription(userId)
                        ContactDetails(userId = userId, name = MutableStateFlow(name), image = MutableStateFlow(image))
                    }
                }
                contactsDetails.awaitAll()
            }
        }.getOrNull()?.toSet() ?: setOf()
    }
}