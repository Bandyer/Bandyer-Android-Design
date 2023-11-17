/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.contactdetails.cachedprovider.CachedDefaultContactDetailsProvider
import com.kaleyra.video_common_ui.contactdetails.cachedprovider.CachedLocalContactDetailsProvider
import com.kaleyra.video_common_ui.contactdetails.cachedprovider.CachedRemoteContactDetailsProvider
import com.kaleyra.video_common_ui.contactdetails.provider.CollaborationContactDetailsProvider
import com.kaleyra.video_common_ui.contactdetailsprovider.ContactDetailsTestHelper.assertEqualsContactDetails
import com.kaleyra.video_common_ui.model.UserDetails
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
        mockkObject(KaleyraVideo)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `local provider is used as primary provider`() = runTest(testDispatcher) {
        val usersDescriptionProvider = LocalContactDetailsProviderTestHelper.usersDescriptionProviderMock(fetchDelay = 1500L)
        every { KaleyraVideo.userDetailsProvider } returns usersDescriptionProvider
        every { KaleyraVideo.collaboration } returns mockk(relaxed = true)
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val localProvider = CachedLocalContactDetailsProvider(usersDescriptionProvider, testDispatcher)

        val userIds = arrayOf("userId1", "userId2")
        val result = provider.fetchContactsDetails(*userIds)
        val expected = localProvider.fetchContactsDetails(*userIds)
        assertEqualsContactDetails(expected.toList(), result)
    }

    @Test
    fun `local provider is not available, the remote provider is used as fallback`() = runTest(testDispatcher) {
        every { KaleyraVideo.userDetailsProvider } returns null
        every { KaleyraVideo.collaboration } returns mockk {
            every { this@mockk.contacts } returns contactsMock
        }
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val remoteProvider = CachedRemoteContactDetailsProvider(contactsMock)

        val userIds = arrayOf("userId1", "userId2")
        val result = provider.fetchContactsDetails(*userIds)
        val expected = remoteProvider.fetchContactsDetails(*userIds)
        assertEqualsContactDetails(expected.toList(), result)
    }

    @Test
    fun `both local and remote providers are not available, the default provider is used as fallback`() = runTest(testDispatcher) {
        every { KaleyraVideo.userDetailsProvider } returns null
        every { KaleyraVideo.collaboration } returns null
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val defaultProvider = CachedDefaultContactDetailsProvider()

        val userIds = arrayOf("userId1", "userId2")
        val result = provider.fetchContactsDetails(*userIds)
        val expected = defaultProvider.fetchContactsDetails(*userIds)
        assertEqualsContactDetails(expected.toList(), result)
    }

    @Test
    fun `local provider does not provide all users results, the remote provider is used as fallback`() = runTest(testDispatcher) {
        val usersDescriptionProvider = { userIds: List<String> ->
            val result = userIds.mapNotNull { userId ->
                if (userId == "userId1") {
                    UserDetails(
                        userId = userId,
                        name = LocalContactDetailsProviderTestHelper.defaultUsers[userId]?.first ?: "null",
                        image =  LocalContactDetailsProviderTestHelper.defaultUsers[userId]?.second ?: Uri.EMPTY
                    )
                } else null
            }
            Result.success(result)
        }
        every { KaleyraVideo.userDetailsProvider } returns usersDescriptionProvider
        every { KaleyraVideo.collaboration } returns mockk {
            every { this@mockk.contacts } returns contactsMock
        }
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val localProvider = CachedLocalContactDetailsProvider(usersDescriptionProvider, testDispatcher)
        val remoteProvider = CachedRemoteContactDetailsProvider(contactsMock)

        val userIds = arrayOf("userId1", "userId2")
        val result = provider.fetchContactsDetails(*userIds)
        val localExpected = localProvider.fetchContactsDetails(*userIds)
        val remoteExpected = remoteProvider.fetchContactsDetails(*userIds).filter { it.userId == "userId2" }
        val expected = localExpected + remoteExpected
        assertEqualsContactDetails(expected.toList(), result)
    }

    @Test
    fun `both local and remote providers do not provide all users results, the default provider is used as fallback`() = runTest(testDispatcher) {
        val usersDescriptionProvider = { userIds: List<String> ->
            val result = userIds.mapNotNull { userId ->
                if (userId == "userId1") {
                    UserDetails(
                        userId = userId,
                        name = LocalContactDetailsProviderTestHelper.defaultUsers[userId]?.first ?: "null",
                        image =  LocalContactDetailsProviderTestHelper.defaultUsers[userId]?.second ?: Uri.EMPTY
                    )
                } else null
            }
            Result.success(result)
        }

        every { KaleyraVideo.userDetailsProvider } returns usersDescriptionProvider
        every { KaleyraVideo.collaboration } returns mockk {
            every { this@mockk.contacts } returns contactsMock
        }
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val localProvider = CachedLocalContactDetailsProvider(usersDescriptionProvider, testDispatcher)
        val remoteProvider = CachedRemoteContactDetailsProvider(contactsMock)
        val defaultProvider = CachedDefaultContactDetailsProvider()

        val userIds = arrayOf("userId1", "userId2", "userId3")
        val result = provider.fetchContactsDetails(*userIds)
        val localExpected = localProvider.fetchContactsDetails(*userIds)
        val remoteExpected = remoteProvider.fetchContactsDetails(*userIds).filter { it.userId == "userId2" }
        val defaultExpected = defaultProvider.fetchContactsDetails(*userIds).filter { it.userId == "userId3" }
        val expected = localExpected + remoteExpected + defaultExpected
        assertEqualsContactDetails(expected.toList(), result)
    }

    @Test
    fun `users description provider changes, the local provider is updated`() = runTest(testDispatcher) {
        val usersDescriptionProvider = LocalContactDetailsProviderTestHelper.usersDescriptionProviderMock()
        every { KaleyraVideo.userDetailsProvider } returns usersDescriptionProvider
        every { KaleyraVideo.collaboration } returns mockk(relaxed = true)
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val localProvider = CachedLocalContactDetailsProvider(usersDescriptionProvider, testDispatcher)
        val userIds = arrayOf("userId1", "userId2")

        val result = provider.fetchContactsDetails(*userIds)
        val expected = localProvider.fetchContactsDetails(*userIds)
        assertEqualsContactDetails(expected.toList(), result)

        val uriMock = mockk<Uri>()
        val newUsersDescription = { ids: List<String> ->
            Result.success(ids.map { UserDetails(it, it, uriMock) })
        }
        every { KaleyraVideo.userDetailsProvider } returns newUsersDescription
        val newLocalProvider = CachedLocalContactDetailsProvider(newUsersDescription, testDispatcher)

        val newResult = provider.fetchContactsDetails(*userIds)
        val newExpected = newLocalProvider.fetchContactsDetails(*userIds)
        assertEqualsContactDetails(newExpected.toList(), newResult)
    }

    @Test
    fun `collaboration changes, the remote provider updated`() = runTest(testDispatcher) {
        val usersDescriptionProvider = LocalContactDetailsProviderTestHelper.usersDescriptionProviderMock(Exception())
        every { KaleyraVideo.userDetailsProvider } returns usersDescriptionProvider
        every { KaleyraVideo.collaboration } returns mockk {
            every { this@mockk.contacts } returns contactsMock
        }
        val provider = CollaborationContactDetailsProvider(testDispatcher)
        val remoteProvider = CachedRemoteContactDetailsProvider(contactsMock)
        val userIds = arrayOf("userId1", "userId2")

        val result = provider.fetchContactsDetails(*userIds)
        val expected = remoteProvider.fetchContactsDetails(*userIds)
        assertEqualsContactDetails(expected.toList(), result)

        val uriMock = mockk<Uri>()
        val newContacts = RemoteContactDetailsProviderTestHelper.ContactsMock(
            hashMapOf(
                "userId1" to RemoteContactDetailsProviderTestHelper.ContactMock(
                    "userId1",
                    MutableStateFlow("newUsername1"),
                    MutableStateFlow(uriMock)
                ),
                "userId2" to RemoteContactDetailsProviderTestHelper.ContactMock(
                    "userId2",
                    MutableStateFlow("newUsername2"),
                    MutableStateFlow(uriMock)
                )
            )
        )
        every { KaleyraVideo.collaboration } returns mockk {
            every { this@mockk.contacts } returns newContacts
        }
        val newRemoteProvider = CachedRemoteContactDetailsProvider(newContacts)

        val newResult = provider.fetchContactsDetails(*userIds)
        val newExpected = newRemoteProvider.fetchContactsDetails(*userIds)
        assertEquals(newExpected, newResult)
    }

}