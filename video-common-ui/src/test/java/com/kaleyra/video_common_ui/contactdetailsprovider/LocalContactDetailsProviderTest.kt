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

import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import com.kaleyra.video_common_ui.contactdetails.provider.LocalContactDetailsProvider
import com.kaleyra.video_common_ui.contactdetailsprovider.ContactDetailsTestHelper.assertEqualsContactDetails
import com.kaleyra.video_common_ui.contactdetailsprovider.LocalContactDetailsProviderTestHelper.uriUser1
import com.kaleyra.video_common_ui.contactdetailsprovider.LocalContactDetailsProviderTestHelper.uriUser2
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal open class LocalContactDetailsProviderTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `test contacts details empty user ids`() = runTest(testDispatcher) {
        val provider = LocalContactDetailsProvider(
            userDetailsProvider = LocalContactDetailsProviderTestHelper.usersDescriptionProviderMock(),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails()
        assertEquals(setOf<ContactDetails>(), result)
    }

    @Test
    fun `test contacts details immediate fetch`() = runTest(testDispatcher) {
        val provider = LocalContactDetailsProvider(
            userDetailsProvider = LocalContactDetailsProviderTestHelper.usersDescriptionProviderMock(fetchDelay = 0L),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2")
        val expected = listOf(
            ContactDetails("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)),
            ContactDetails("userId2", MutableStateFlow("username2"), MutableStateFlow(uriUser2))
        )
        assertEqualsContactDetails(expected, result)
    }

    @Test
    fun `test contacts details delayed fetch`() = runTest(testDispatcher) {
        val provider = LocalContactDetailsProvider(
            userDetailsProvider = LocalContactDetailsProviderTestHelper.usersDescriptionProviderMock(fetchDelay = 1500L),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2")
        val expected = listOf(
            ContactDetails("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)),
            ContactDetails("userId2", MutableStateFlow("username2"), MutableStateFlow(uriUser2))
        )
        assertEqualsContactDetails(expected, result)
    }

    @Test
    fun `test contacts details fetch timed out`() = runTest(testDispatcher) {
        val timeout = 1000L
        val provider = LocalContactDetailsProvider(
            userDetailsProvider = LocalContactDetailsProviderTestHelper.usersDescriptionProviderMock(fetchDelay = timeout),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2", timeout = timeout)
        assertEquals(setOf<ContactDetails>(), result)
    }

    @Test
    fun `test contacts details fetch timeout limit`() = runTest(testDispatcher) {
        val timeout = 1000L
        val provider = LocalContactDetailsProvider(
            userDetailsProvider = LocalContactDetailsProviderTestHelper.usersDescriptionProviderMock(fetchDelay = timeout - 1),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2", timeout = timeout)
        val expected = listOf(
            ContactDetails("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)),
            ContactDetails("userId2", MutableStateFlow("username2"), MutableStateFlow(uriUser2))
        )
        assertEqualsContactDetails(expected, result)
    }

    @Test
    fun `test contacts details fetch exception occurrence`() = runTest(testDispatcher) {
        val provider = LocalContactDetailsProvider(
            userDetailsProvider = LocalContactDetailsProviderTestHelper.usersDescriptionProviderMock(Exception()),
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2")
        assertEquals(setOf<ContactDetails>(), result)
    }

    @Test
    fun `test contacts details fetch result failure`() = runTest(testDispatcher) {
        val provider = LocalContactDetailsProvider(
            userDetailsProvider = { Result.failure(Exception()) },
            ioDispatcher = testDispatcher
        )
        val result = provider.fetchContactsDetails("userId1", "userId2")
        assertEquals(setOf<ContactDetails>(), result)
    }



}