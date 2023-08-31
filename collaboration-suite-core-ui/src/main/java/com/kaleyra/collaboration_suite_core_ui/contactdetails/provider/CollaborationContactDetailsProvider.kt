package com.kaleyra.collaboration_suite_core_ui.contactdetails.provider

import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedDefaultContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedLocalContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedRemoteContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.model.UserDetails
import com.kaleyra.collaboration_suite_core_ui.model.UserDetailsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
        get() = with(CollaborationUI.userDetailsProvider) {
            field?.takeIf { it.userDetailsProvider == this } ?: this?.let { CachedLocalContactDetailsProvider(it, ioDispatcher) }.apply { field = this }
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
        val (primaryResult, fallbackResult, defaultResult) = listOf(primaryContactDetails, fallbackContactDetails, defaultProviderContacts).awaitAll()
        val result = userIds.mapNotNull { userId ->
            primaryResult.find { it.userId == userId } ?: fallbackResult.find { it.userId == userId } ?: defaultResult.find { it.userId == userId }
        }
        result.toSet()
    }
}