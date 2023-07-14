package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.collaboration_suite.Contact
import com.kaleyra.collaboration_suite.Contacts
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.RemoteContactDetailsProvider
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import kotlin.math.exp

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteContactDetailsProviderTest {

    private class ContactMock(
        override val userId: String,
        override val displayName: StateFlow<String?> = MutableStateFlow(null),
        override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
    ): Contact {
        override val restrictions: Contact.Restrictions = mockk()
    }

    private class ContactsMock(private val contacts: Map<String, Contact>): Contacts {
        override val collection: Map<String, Contact> = HashMap()
        override suspend fun get(userId: String): Result<Contact> {
            return contacts[userId]?.let {
                Result.success(it)
            } ?: Result.failure(Exception("contact not found"))
        }
        override suspend fun me(): Contact.Me = mockk()
        override fun clear() = Unit
    }


    private val testDispatcher = StandardTestDispatcher()

    private val uriContact1 = mockk<Uri>()

    private val uriContact2 = mockk<Uri>()

    @Test
    fun `test contacts details empty user ids`() = runTest(testDispatcher) {
        val provider = RemoteContactDetailsProvider(contacts = ContactsMock(HashMap()), ioDispatcher = testDispatcher)
        val result = provider.fetchContactsDetails()
        Assert.assertEquals(setOf<ContactDetails>(), result)
    }

    @Test
    fun `test contacts details timed out when contact have null name and image`() = runTest(testDispatcher) {
        val contacts = ContactsMock(hashMapOf("userId1" to ContactMock("userId1")))
        val provider = RemoteContactDetailsProvider(contacts = contacts, ioDispatcher = testDispatcher)
        val result = provider.fetchContactsDetails("userId1")
        Assert.assertEquals(setOf<ContactDetails>(), result)
    }

    @Test
    fun `test contacts details immediate fetch`() = runTest(testDispatcher) {
        val contacts = ContactsMock(
            hashMapOf(
                "userId1" to ContactMock("userId1", MutableStateFlow("username1"), MutableStateFlow(uriContact1)),
                "userId2" to ContactMock("userId2", MutableStateFlow("username2"), MutableStateFlow(uriContact2))
            )
        )
        val provider = RemoteContactDetailsProvider(contacts = contacts, ioDispatcher = testDispatcher)
        val result = provider.fetchContactsDetails("userId1", "userId2")
        val expected = setOf(
            ContactDetails("userId1", "username1", uriContact1),
            ContactDetails("userId2", "username2", uriContact2)
        )
        Assert.assertEquals(expected, result)
    }

    @Test
    fun `test contacts details delayed fetch`() = runTest(testDispatcher) {
        val timeout = 1500L
        val contact1 = ContactMock(
            "userId1",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username1", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriContact1, delay = timeout, backgroundScope)
        )
        val contact2 = ContactMock(
            "userId2",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username2", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriContact2, delay = timeout, backgroundScope)
        )
        val contacts = ContactsMock(hashMapOf("userId1" to contact1, "userId2" to contact2))
        val provider = RemoteContactDetailsProvider(contacts = contacts, ioDispatcher = testDispatcher)
        val result = provider.fetchContactsDetails("userId1", "userId2")
        val expected = setOf(
            ContactDetails("userId1", "username1", uriContact1),
            ContactDetails("userId2", "username2", uriContact2)
        )
        Assert.assertEquals(expected, result)
    }

    @Test
    fun `test contacts details fetch timed out`() = runTest(testDispatcher) {
        val timeout = 1000L
        val contact1 = ContactMock(
            "userId1",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username1", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriContact1, delay = timeout, backgroundScope)
        )
        val contact2 = ContactMock(
            "userId2",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username2", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriContact2, delay = timeout, backgroundScope)
        )
        val contacts = ContactsMock(hashMapOf("userId1" to contact1, "userId2" to contact2))
        val provider = RemoteContactDetailsProvider(contacts = contacts, ioDispatcher = testDispatcher)

        val result = provider.fetchContactsDetails("userId1", "userId2", timeout = timeout)
        Assert.assertEquals(setOf<ContactDetails>(), result)
    }

    @Test
    fun `test contacts details fetch timeout limit`() = runTest(testDispatcher) {
        val timeout = 1000L
        val contact1 = ContactMock(
            "userId1",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username1", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriContact1, delay = timeout, backgroundScope)
        )
        val contact2 = ContactMock(
            "userId2",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username2", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriContact2, delay = timeout, backgroundScope)
        )
        val contacts = ContactsMock(hashMapOf("userId1" to contact1, "userId2" to contact2))
        val provider = RemoteContactDetailsProvider(contacts = contacts, ioDispatcher = testDispatcher)

        val result = provider.fetchContactsDetails("userId1", "userId2", timeout = timeout + 1)
        val expected = setOf(
            ContactDetails("userId1", "username1", uriContact1),
            ContactDetails("userId2", "username2", uriContact2)
        )
        Assert.assertEquals(expected, result)
    }

    private fun <T> MutableStateFlow<T>.emitWithDelay(newValue: T, delay: Long, scope: CoroutineScope): MutableStateFlow<T> =
        apply {
            scope.launch {
                delay(delay)
                value = newValue
            }
        }

}