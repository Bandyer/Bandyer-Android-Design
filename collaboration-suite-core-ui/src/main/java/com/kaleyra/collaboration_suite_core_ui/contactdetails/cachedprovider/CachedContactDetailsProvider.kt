package com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider

import com.kaleyra.collaboration_suite.Contacts
import com.kaleyra.collaboration_suite_core_ui.Cache
import com.kaleyra.collaboration_suite_core_ui.PerpetualCache
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.ContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.DefaultContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.LocalContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.RemoteContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription

internal abstract class CachedContactDetailsProvider(private val provider: ContactDetailsProvider) :
    ContactDetailsProvider {

    private val cache: Cache<String, ContactDetails> = PerpetualCache()

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> {
        val cached: List<ContactDetails> = userIds.mapNotNull { cache[it] }
        val contactsDetails: MutableList<ContactDetails> = cached.toMutableList()

        val cachedIds: List<String> = cached.map { it.userId }
        val fetchIds: Set<String> = userIds.toSet() - cachedIds.toSet()
        if (fetchIds.isNotEmpty()) {
            val fetched =
                provider.fetchContactsDetails(userIds = fetchIds.toTypedArray(), timeout = timeout)
            contactsDetails += fetched
            fetched.forEach { cache[it.userId] = it }
        }
        return contactsDetails.toSet()
    }
}

internal class CachedLocalContactDetailsProvider(val usersDescription: UsersDescription) :
    CachedContactDetailsProvider(LocalContactDetailsProvider(usersDescription))

internal class CachedRemoteContactDetailsProvider(val contacts: Contacts) :
    CachedContactDetailsProvider(RemoteContactDetailsProvider(contacts))

internal class CachedDefaultContactDetailsProvider :
    CachedContactDetailsProvider(DefaultContactDetailsProvider())