package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.RemoteContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider.ContactDetailsTestHelper.assertEqualsContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider.RemoteContactDetailsProviderTestHelper.uriUser1
import com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider.RemoteContactDetailsProviderTestHelper.uriUser2
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteContactDetailsProviderTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `test contacts details empty user ids`() = runTest(testDispatcher) {
        val provider = RemoteContactDetailsProvider(contacts = RemoteContactDetailsProviderTestHelper.ContactsMock(HashMap()), ioDispatcher = testDispatcher)
        val result = provider.fetchContactsDetails()
        assertEquals(setOf<ContactDetails>(), result)
    }

    @Test
    fun `test contacts details fetch`() = runTest(testDispatcher) {
        val contacts = RemoteContactDetailsProviderTestHelper.ContactsMock(
            hashMapOf(
                "userId1" to RemoteContactDetailsProviderTestHelper.ContactMock(
                    "userId1",
                    MutableStateFlow("username1"),
                    MutableStateFlow(uriUser1)
                ),
                "userId2" to RemoteContactDetailsProviderTestHelper.ContactMock(
                    "userId2",
                    MutableStateFlow("username2"),
                    MutableStateFlow(uriUser2)
                )
            )
        )
        val provider = RemoteContactDetailsProvider(contacts = contacts, ioDispatcher = testDispatcher)
        val result = provider.fetchContactsDetails("userId1", "userId2")
        val expected = listOf(
            ContactDetails("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)),
            ContactDetails("userId2", MutableStateFlow("username2"), MutableStateFlow(uriUser2))
        )
        assertEqualsContactDetails(expected, result)
    }

}