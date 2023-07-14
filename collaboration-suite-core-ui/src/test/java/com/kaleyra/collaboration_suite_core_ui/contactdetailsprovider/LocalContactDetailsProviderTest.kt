package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.LocalContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.model.DefaultUsersDescription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal open class LocalContactDetailsProviderTest : BaseLocalContactDetailsProviderTest() {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `test contacts details empty user ids`() = runTest(testDispatcher) {
        val provider = LocalContactDetailsProvider(
            usersDescription = usersDescriptionMock(),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails()
        assertEquals(setOf<ContactDetails>(), result)
    }

    @Test
    fun `test contacts details immediate fetch`() = runTest(testDispatcher) {
        val provider = LocalContactDetailsProvider(
            usersDescription = usersDescriptionMock(fetchDelay = 0L),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2")
        val expected = setOf(
            ContactDetails("userId1", "username1", uriUser1),
            ContactDetails("userId2", "username2", uriUser2)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `test contacts details delayed fetch`() = runTest(testDispatcher) {
        val provider = LocalContactDetailsProvider(
            usersDescription = usersDescriptionMock(fetchDelay = 1500L),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2")
        val expected = setOf(
            ContactDetails("userId1", "username1", uriUser1),
            ContactDetails("userId2", "username2", uriUser2)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `test contacts details fetch timed out`() = runTest(testDispatcher) {
        val timeout = 1000L
        val provider = LocalContactDetailsProvider(
            usersDescription = usersDescriptionMock(fetchDelay = timeout),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2", timeout = timeout)
        assertEquals(setOf<ContactDetails>(), result)
    }

    @Test
    fun `test contacts details fetch timeout limit`() = runTest(testDispatcher) {
        val timeout = 1000L
        val provider = LocalContactDetailsProvider(
            usersDescription = usersDescriptionMock(fetchDelay = timeout - 1),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2", timeout = timeout)
        val expected = setOf(
            ContactDetails("userId1", "username1", uriUser1),
            ContactDetails("userId2", "username2", uriUser2)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `test contacts details fetch exception occurrence`() = runTest(testDispatcher) {
        val provider = LocalContactDetailsProvider(
            usersDescription = DefaultUsersDescription(name = { throw Exception() }),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2")
        assertEquals(setOf<ContactDetails>(), result)
    }

}