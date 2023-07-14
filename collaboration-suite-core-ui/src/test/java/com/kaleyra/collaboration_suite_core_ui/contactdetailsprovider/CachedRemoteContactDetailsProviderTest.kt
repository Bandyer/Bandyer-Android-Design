package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedLocalContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedRemoteContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.RemoteContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.model.DefaultUsersDescription
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CachedRemoteContactDetailsProviderTest: BaseRemoteContactDetailsProviderTest() {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `test retrieve cached user contact details`() = runTest(testDispatcher) {
        val contact1 = spyk(ContactMock("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)))
        val contact2 = spyk(ContactMock("userId2", MutableStateFlow("username2"), MutableStateFlow(uriUser2)))
        val contacts = ContactsMock(hashMapOf("userId1" to contact1, "userId2" to contact2))
        val provider = CachedRemoteContactDetailsProvider(contacts = contacts, ioDispatcher = testDispatcher)
        val result = provider.fetchContactsDetails("userId1")
        val expected = setOf(ContactDetails("userId1", "username1", uriUser1))
        Assert.assertEquals(expected, result)

        val newResult = provider.fetchContactsDetails("userId1", "userId2")
        val newExpected =  setOf(
            ContactDetails("userId1", "username1", uriUser1),
            ContactDetails("userId2", "username2", uriUser2),
        )
        Assert.assertEquals(newExpected, newResult)

        coVerify(exactly = 1) { contact1.displayName }
        coVerify(exactly = 1) { contact1.displayImage }
        coVerify(exactly = 1) { contact2.displayName }
        coVerify(exactly = 1) { contact2.displayImage }
    }
}