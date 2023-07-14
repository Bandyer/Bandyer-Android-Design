package com.kaleyra.collaboration_suite_core_ui.contactdetails

import com.kaleyra.collaboration_suite.Contact
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedDefaultContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedLocalContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedRemoteContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.ContactDetailsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class CollaborationContactDetailsProvider(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ContactDetailsProvider {

    private val primaryProvider: ContactDetailsProvider
        get() = localContactDetailsProvider ?: remoteContactDetailsProvider ?: defaultProvider

    private val fallbackProvider: ContactDetailsProvider
        get() = when (primaryProvider) {
            localContactDetailsProvider -> remoteContactDetailsProvider ?: defaultProvider
            else -> defaultProvider
        }

    private val defaultProvider: ContactDetailsProvider by lazy { CachedDefaultContactDetailsProvider() }

    private var localContactDetailsProvider: CachedLocalContactDetailsProvider? = null
        get() = with(CollaborationUI.usersDescription) {
            field?.takeIf { it.usersDescription == this } ?: this?.let { CachedLocalContactDetailsProvider(it, ioDispatcher) }.apply { field = this }
        }

    private var remoteContactDetailsProvider: CachedRemoteContactDetailsProvider? = null
        get() = with(CollaborationUI.collaboration?.contacts) {
            field?.takeIf { it.contacts == this } ?: this?.let { CachedRemoteContactDetailsProvider(it, ioDispatcher) }.apply { field = this }
        }

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> = coroutineScope {
        val primaryContactDetails = async(ioDispatcher) { primaryProvider.fetchContactsDetails(userIds = userIds) }
        val fallbackContactDetails = async(ioDispatcher) { fallbackProvider.fetchContactsDetails(userIds = userIds) }
        val defaultProviderContacts = async(ioDispatcher) { defaultProvider.fetchContactsDetails(userIds = userIds) }
        primaryContactDetails.await().takeIf { it.isNotEmpty() } ?: fallbackContactDetails.await().takeIf { it.isNotEmpty() } ?: defaultProviderContacts.await()
    }
}

// TODO update the remote details when they change
// TODO use updateDisplayDetails?, invoke when connected on call module
// TODO check if it is right also in the call link
object ContactDetailsManager {

    private val collaborationContactDetailsProvider by lazy { CollaborationContactDetailsProvider() }

    private val mutex = Mutex()

    private val collection = HashMap<String, ContactDetails>()

    private val contactDetails = MutableStateFlow(setOf<ContactDetails>())

    suspend fun fetchContactDetails(vararg userIds: String) = mutex.withLock {
        val fetchedContactDetails = collaborationContactDetailsProvider.fetchContactsDetails(userIds = userIds)
        fetchedContactDetails.forEach { collection[it.userId] = it }
        contactDetails.value = collection.values.toSet()
    }

    val Contact.userDetails: Flow<ContactDetails?>
        get() = contactDetails
            .map { contactsDetails -> contactsDetails.firstOrNull { it.userId == this.userId } }
            .distinctUntilChanged()
}
