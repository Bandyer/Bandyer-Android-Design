package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.RemoteContactDetailsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteContactDetailsProviderTest : BaseRemoteContactDetailsProviderTest() {

    private val testDispatcher = StandardTestDispatcher()

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
                "userId1" to ContactMock("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)),
                "userId2" to ContactMock("userId2", MutableStateFlow("username2"), MutableStateFlow(uriUser2))
            )
        )
        val provider = RemoteContactDetailsProvider(contacts = contacts, ioDispatcher = testDispatcher)
        val result = provider.fetchContactsDetails("userId1", "userId2")
        val expected = setOf(
            ContactDetails("userId1", "username1", uriUser1),
            ContactDetails("userId2", "username2", uriUser2)
        )
        Assert.assertEquals(expected, result)
    }

    @Test
    fun `test contacts details delayed fetch`() = runTest(testDispatcher) {
        val timeout = 1500L
        val contact1 = ContactMock(
            "userId1",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username1", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriUser1, delay = timeout, backgroundScope)
        )
        val contact2 = ContactMock(
            "userId2",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username2", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriUser2, delay = timeout, backgroundScope)
        )
        val contacts = ContactsMock(hashMapOf("userId1" to contact1, "userId2" to contact2))
        val provider = RemoteContactDetailsProvider(contacts = contacts, ioDispatcher = testDispatcher)
        val result = provider.fetchContactsDetails("userId1", "userId2")
        val expected = setOf(
            ContactDetails("userId1", "username1", uriUser1),
            ContactDetails("userId2", "username2", uriUser2)
        )
        Assert.assertEquals(expected, result)
    }

    @Test
    fun `test contacts details fetch timed out`() = runTest(testDispatcher) {
        val timeout = 1000L
        val contact1 = ContactMock(
            "userId1",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username1", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriUser1, delay = timeout, backgroundScope)
        )
        val contact2 = ContactMock(
            "userId2",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username2", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriUser2, delay = timeout, backgroundScope)
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
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriUser1, delay = timeout, backgroundScope)
        )
        val contact2 = ContactMock(
            "userId2",
            MutableStateFlow<String?>(null).emitWithDelay(newValue = "username2", delay = timeout, backgroundScope),
            MutableStateFlow<Uri?>(null).emitWithDelay(newValue = uriUser2, delay = timeout, backgroundScope)
        )
        val contacts = ContactsMock(hashMapOf("userId1" to contact1, "userId2" to contact2))
        val provider = RemoteContactDetailsProvider(contacts = contacts, ioDispatcher = testDispatcher)

        val result = provider.fetchContactsDetails("userId1", "userId2", timeout = timeout + 1)
        val expected = setOf(
            ContactDetails("userId1", "username1", uriUser1),
            ContactDetails("userId2", "username2", uriUser2)
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