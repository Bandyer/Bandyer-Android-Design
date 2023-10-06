package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedLocalContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider.ContactDetailsTestHelper.assertEqualsContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider.LocalContactDetailsProviderTestHelper.usersDescriptionProviderMock
import com.kaleyra.collaboration_suite_core_ui.model.UserDetailsProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class CachedLocalContactDetailsProviderTest {

    private val testDispatcher = StandardTestDispatcher()

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `test retrieve cached user contact details`() = runTest(testDispatcher) {
        // cannot use spyk(usersDescriptionProviderMock()) because of this issue https://github.com/mockk/mockk/issues/1033
        val usersDescriptionProvider: UserDetailsProvider = spyk {
            coEvery { this@spyk.invoke(any()) } coAnswers { call ->
                usersDescriptionProviderMock().invoke(call.invocation.args.first() as List<String>)
            }
        }
        val provider = CachedLocalContactDetailsProvider(userDetailsProvider = usersDescriptionProvider, ioDispatcher = testDispatcher)

        val result = provider.fetchContactsDetails("userId1")
        val expected = listOf(ContactDetails("userId1", MutableStateFlow("username1"), MutableStateFlow(LocalContactDetailsProviderTestHelper.uriUser1)))
        assertEqualsContactDetails(expected, result)

        val newResult = provider.fetchContactsDetails("userId1", "userId2")
        val newExpected =  listOf(
            ContactDetails("userId1", MutableStateFlow("username1"), MutableStateFlow(LocalContactDetailsProviderTestHelper.uriUser1)),
            ContactDetails("userId2", MutableStateFlow("username2"), MutableStateFlow(LocalContactDetailsProviderTestHelper.uriUser2)),
        )
        assertEqualsContactDetails(newExpected, newResult)

        coVerify(exactly = 1) { usersDescriptionProvider(listOf("userId1")) }
        coVerify(exactly = 1) { usersDescriptionProvider(listOf("userId2")) }
    }

}