package com.kaleyra.collaboration_suite_core_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.Contacts
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withTimeout

data class ContactDetails(
    val userId: String,
    val name: String? = null,
    val image: Uri? = null
)

interface ContactDetailsProvider {

    suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long = FETCH_TIMEOUT
    ): Set<ContactDetails>

    companion object {
        const val FETCH_TIMEOUT = 2500L
    }
}

class LocalContactDetailsProvider(val usersDescription: UsersDescription) : ContactDetailsProvider {

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

class RemoteContactDetailsProvider(val contacts: Contacts) : ContactDetailsProvider {

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

class DefaultContactDetailsProvider : ContactDetailsProvider {

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> =
        userIds.map { ContactDetails(userId = it, name = it) }.toSet()
}

abstract class CachedContactDetailsProvider(private val provider: ContactDetailsProvider) : ContactDetailsProvider {

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
            val fetched = provider.fetchContactsDetails(userIds = fetchIds.toTypedArray(), timeout = timeout)
            contactsDetails += fetched
            fetched.forEach { cache[it.userId] = it }
        }
        return contactsDetails.toSet()
    }
}

class CachedLocalContactDetailsProvider(val usersDescription: UsersDescription) :
    CachedContactDetailsProvider(LocalContactDetailsProvider(usersDescription))

class CachedRemoteContactDetailsProvider(val contacts: Contacts) :
    CachedContactDetailsProvider(RemoteContactDetailsProvider(contacts))

class CachedDefaultContactDetailsProvider :
    CachedContactDetailsProvider(DefaultContactDetailsProvider())

class CollaborationContactDetailsProvider : ContactDetailsProvider {

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
            field?.takeIf { it.usersDescription == this } ?: this?.let { CachedLocalContactDetailsProvider(it) }.apply { field = this }
        }

    private var remoteContactDetailsProvider: CachedRemoteContactDetailsProvider? = null
        get() = with(CollaborationUI.collaboration?.contacts) {
            field?.takeIf { it.contacts == this } ?: this?.let { CachedRemoteContactDetailsProvider(it) }.apply { field = this }
        }

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> = coroutineScope {
        val primaryContactDetails = async(Dispatchers.IO) { primaryProvider.fetchContactsDetails(userIds = userIds) }
        val fallbackContactDetails = async(Dispatchers.IO) { fallbackProvider.fetchContactsDetails(userIds = userIds) }
        val defaultProviderContacts = async(Dispatchers.IO) { defaultProvider.fetchContactsDetails(userIds = userIds) }
        primaryContactDetails.await().takeIf { it.isNotEmpty() } ?: fallbackContactDetails.await().takeIf { it.isNotEmpty() } ?: defaultProviderContacts.await()
    }
}

// TODO update the remote details when they change
// TODO use updateDisplayDetails?, invoke when connected on call module
// TODO check if it is right also in the call link
object ContactDetailsManager {

    private val fetchContactDetailsContext = newSingleThreadContext("FetchContactDetailsContext")

    private val collaborationContactDetailsProvider by lazy { CollaborationContactDetailsProvider() }

    private val _contactDetails = MutableStateFlow(setOf<ContactDetails>())
    val contactDetails = _contactDetails.asStateFlow()

    suspend fun fetchContactDetails(vararg userIds: String) {
        collaborationContactDetailsProvider.fetchContactsDetails(userIds = userIds)
    }
}
