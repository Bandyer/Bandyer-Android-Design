package com.kaleyra.collaboration_suite_core_ui.contactdetails.provider

import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedDefaultContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedLocalContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedRemoteContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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
        get() = with(CollaborationUI.usersDescriptionProvider) {
            field?.takeIf { it.usersDescriptionProvider == this } ?: this?.let { CachedLocalContactDetailsProvider(it, ioDispatcher) }.apply { field = this }
        }

    private var remoteContactDetailsProvider: CachedRemoteContactDetailsProvider? = null
        get() = with(CollaborationUI.collaboration?.contacts) {
            field?.takeIf { it.contacts == this } ?: this?.let { CachedRemoteContactDetailsProvider(it) }.apply { field = this }
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