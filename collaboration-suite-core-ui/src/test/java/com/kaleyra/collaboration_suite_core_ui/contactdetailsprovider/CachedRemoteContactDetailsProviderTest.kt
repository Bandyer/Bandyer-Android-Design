package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedRemoteContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider.ContactDetailsTestHelper.assertEqualsContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider.RemoteContactDetailsProviderTestHelper.uriUser1
import com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider.RemoteContactDetailsProviderTestHelper.uriUser2
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CachedRemoteContactDetailsProviderTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `test retrieve cached user contact details`() = runTest(testDispatcher) {
        val contact1 = spyk(RemoteContactDetailsProviderTestHelper.ContactMock("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)))
        val contact2 = spyk(RemoteContactDetailsProviderTestHelper.ContactMock("userId2", MutableStateFlow("username2"), MutableStateFlow(uriUser2)))
        val contacts = RemoteContactDetailsProviderTestHelper.ContactsMock(hashMapOf("userId1" to contact1, "userId2" to contact2))
        val provider = CachedRemoteContactDetailsProvider(contacts = contacts)
        val result = provider.fetchContactsDetails("userId1")
        val expected = listOf(ContactDetails("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)))
        assertEqualsContactDetails(expected, result)

        val newResult = provider.fetchContactsDetails("userId1", "userId2")
        val newExpected = listOf(
            ContactDetails("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)),
            ContactDetails("userId2", MutableStateFlow("username2"), MutableStateFlow(uriUser2)),
        )
        assertEqualsContactDetails(newExpected, newResult)

        coVerify(exactly = 1) { contact1.displayName }
        coVerify(exactly = 1) { contact1.displayImage }
        coVerify(exactly = 1) { contact2.displayName }
        coVerify(exactly = 1) { contact2.displayImage }
    }
}