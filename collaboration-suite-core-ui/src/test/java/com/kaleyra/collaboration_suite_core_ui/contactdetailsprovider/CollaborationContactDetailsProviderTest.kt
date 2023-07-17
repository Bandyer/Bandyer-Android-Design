package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedDefaultContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedLocalContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedRemoteContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.CollaborationContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider.LocalContactDetailsProviderTestHelper.usersDescriptionMock
import com.kaleyra.collaboration_suite_core_ui.model.DefaultUsersDescription
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollaborationContactDetailsProviderTest {

    private val testDispatcher = StandardTestDispatcher()

    private val contactsMock = RemoteContactDetailsProviderTestHelper.ContactsMock(
        hashMapOf(
            "userId1" to RemoteContactDetailsProviderTestHelper.ContactMock(
                "userId1",
                MutableStateFlow("username1"),
                MutableStateFlow(RemoteContactDetailsProviderTestHelper.uriUser1)
            ),
            "userId2" to RemoteContactDetailsProviderTestHelper.ContactMock(
                "userId2",
                MutableStateFlow("username2"),
                MutableStateFlow(RemoteContactDetailsProviderTestHelper.uriUser2)
            )
        )
    )

    @Before
    fun setUp() {
        mockkObject(CollaborationUI)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `local provider is used as primary provider`() = runTest(testDispatcher) {
        val usersDescription = usersDescriptionMock(fetchDelay = 1500L)
        every { CollaborationUI.usersDescription } returns usersDescription
        every { CollaborationUI.collaboration } returns mockk(relaxed = true)
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val localProvider = CachedLocalContactDetailsProvider(usersDescription, testDispatcher)

        val userIds = arrayOf("userId1", "userId2")
        val result = provider.fetchContactsDetails(*userIds)
        val expected = localProvider.fetchContactsDetails(*userIds)
        assertEquals(expected, result)
    }

    @Test
    fun `local provider is not available, the remote provider is used as fallback`() = runTest(testDispatcher) {
        every { CollaborationUI.usersDescription } returns null
        every { CollaborationUI.collaboration } returns mockk {
            every { this@mockk.contacts } returns contactsMock
        }
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val remoteProvider = CachedRemoteContactDetailsProvider(contactsMock, testDispatcher)

        val userIds = arrayOf("userId1", "userId2")
        val result = provider.fetchContactsDetails(*userIds)
        val expected = remoteProvider.fetchContactsDetails(*userIds)
        assertEquals(expected, result)
    }

    @Test
    fun `both local and remote providers are not available, the default provider is used as fallback`() = runTest(testDispatcher) {
        every { CollaborationUI.usersDescription } returns null
        every { CollaborationUI.collaboration } returns null
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val defaultProvider = CachedDefaultContactDetailsProvider()

        val userIds = arrayOf("userId1", "userId2")
        val result = provider.fetchContactsDetails(*userIds)
        val expected = defaultProvider.fetchContactsDetails(*userIds)
        assertEquals(expected, result)
    }

    @Test
    fun `local provider provides empty results, the remote provider is used as fallback`() = runTest(testDispatcher) {
        val usersDescription = DefaultUsersDescription(name = { throw Exception() })
        every { CollaborationUI.usersDescription } returns usersDescription
        every { CollaborationUI.collaboration } returns mockk {
            every { this@mockk.contacts } returns contactsMock
        }
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val remoteProvider = CachedRemoteContactDetailsProvider(contactsMock, testDispatcher)

        val userIds = arrayOf("userId1", "userId2")
        val result = provider.fetchContactsDetails(*userIds)
        val expected = remoteProvider.fetchContactsDetails(*userIds)
        assertEquals(expected, result)
    }

    @Test
    fun `both local and remote providers provide empty results, the default provider is used as fallback`() = runTest(testDispatcher) {
        val usersDescription = DefaultUsersDescription(name = { throw Exception() })
        every { CollaborationUI.usersDescription } returns usersDescription
        every { CollaborationUI.collaboration } returns mockk(relaxed = true)
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val defaultProvider = CachedDefaultContactDetailsProvider()

        val userIds = arrayOf("userId1", "userId2")
        val result = provider.fetchContactsDetails(*userIds)
        val expected = defaultProvider.fetchContactsDetails(*userIds)
        assertEquals(expected, result)
    }

    @Test
    fun `users description changes, the local provider is updated`() = runTest(testDispatcher) {
        val usersDescription = usersDescriptionMock()
        every { CollaborationUI.usersDescription } returns usersDescription
        every { CollaborationUI.collaboration } returns mockk(relaxed = true)
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val localProvider = CachedLocalContactDetailsProvider(usersDescription, testDispatcher)
        val userIds = arrayOf("userId1", "userId2")

        val result = provider.fetchContactsDetails(*userIds)
        val expected = localProvider.fetchContactsDetails(*userIds)
        assertEquals(expected, result)

        val uriMock = mockk<Uri>()
        val newUsersDescription = DefaultUsersDescription(name = { it.joinToString() }, image = { uriMock })
        every { CollaborationUI.usersDescription } returns newUsersDescription
        val newLocalProvider = CachedLocalContactDetailsProvider(newUsersDescription, testDispatcher)

        val newResult = provider.fetchContactsDetails(*userIds)
        val newExpected = newLocalProvider.fetchContactsDetails(*userIds)
        assertEquals(newExpected, newResult)
    }

    @Test
    fun `collaboration changes, the remote provider updated`() = runTest(testDispatcher) {
        val usersDescription = DefaultUsersDescription(name = { throw Exception() })
        every { CollaborationUI.usersDescription } returns usersDescription
        every { CollaborationUI.collaboration } returns mockk {
            every { this@mockk.contacts } returns contactsMock
        }
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val remoteProvider = CachedRemoteContactDetailsProvider(contactsMock, testDispatcher)
        val userIds = arrayOf("userId1", "userId2")

        val result = provider.fetchContactsDetails(*userIds)
        val expected = remoteProvider.fetchContactsDetails(*userIds)
        assertEquals(expected, result)

        val uriMock = mockk<Uri>()
        val newContacts = RemoteContactDetailsProviderTestHelper.ContactsMock(
            hashMapOf(
                "userId1" to RemoteContactDetailsProviderTestHelper.ContactMock(
                    "userId1",
                    MutableStateFlow("newUsername1"),
                    MutableStateFlow(uriMock)
                )
            )
        )
        every { CollaborationUI.collaboration } returns mockk {
            every { this@mockk.contacts } returns newContacts
        }
        val newRemoteProvider = CachedRemoteContactDetailsProvider(newContacts, testDispatcher)

        val newResult = provider.fetchContactsDetails(*userIds)
        val newExpected = newRemoteProvider.fetchContactsDetails(*userIds)
        assertEquals(newExpected, newResult)
    }
}