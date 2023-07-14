package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider.CachedLocalContactDetailsProvider
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider.LocalContactDetailsProviderTestHelper.usersDescriptionMock
import com.kaleyra.collaboration_suite_core_ui.model.DefaultUsersDescription
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class CachedLocalContactDetailsProviderTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `test retrieve cached user contact details`() = runTest(testDispatcher) {
        val usersDescription = usersDescriptionMock()
        val name: suspend (List<String>) -> String = spyk(usersDescription.name)
        val image: suspend (List<String>) -> Uri = spyk(usersDescription.image)
        val provider = CachedLocalContactDetailsProvider(
            usersDescription = DefaultUsersDescription(name = name, image = image),
            ioDispatcher = testDispatcher
        )

        val result = provider.fetchContactsDetails("userId1")
        val expected = setOf(ContactDetails("userId1", "username1", LocalContactDetailsProviderTestHelper.uriUser1))
        Assert.assertEquals(expected, result)

        val newResult = provider.fetchContactsDetails("userId1", "userId2")
        val newExpected =  setOf(
            ContactDetails("userId1", "username1", LocalContactDetailsProviderTestHelper.uriUser1),
            ContactDetails("userId2", "username2", LocalContactDetailsProviderTestHelper.uriUser2),
        )
        Assert.assertEquals(newExpected, newResult)

        coVerify(exactly = 1) { name.invoke(listOf("userId1")) }
        coVerify(exactly = 1) { name.invoke(listOf("userId1")) }
        coVerify(exactly = 1) { name.invoke(listOf("userId2")) }
        coVerify(exactly = 1) { image.invoke(listOf("userId2")) }
    }

}