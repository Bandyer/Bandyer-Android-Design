package com.kaleyra.video_common_ui.contactdetails.cachedprovider

import com.kaleyra.video_common_ui.Cache
import com.kaleyra.video_common_ui.PerpetualCache
import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import com.kaleyra.video_common_ui.contactdetails.provider.ContactDetailsProvider

internal abstract class CachedContactDetailsProvider(private val provider: ContactDetailsProvider) :
    ContactDetailsProvider {

    private val cache: com.kaleyra.video_common_ui.Cache<String, ContactDetails> = com.kaleyra.video_common_ui.PerpetualCache()

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