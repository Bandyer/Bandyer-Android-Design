package com.kaleyra.video_common_ui.contactdetailsprovider

import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import com.kaleyra.video_common_ui.contactdetails.provider.DefaultContactDetailsProvider
import com.kaleyra.video_common_ui.contactdetailsprovider.ContactDetailsTestHelper.assertEqualsContactDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultContactDetailsProviderTest {

    @Test
    fun `test contacts details empty user ids`() = runTest {
        val provider = DefaultContactDetailsProvider()
        val result = provider.fetchContactsDetails()
        Assert.assertEquals(setOf<ContactDetails>(), result)
    }

    @Test
    fun `test contacts details fetch`() = runTest {
        val provider = DefaultContactDetailsProvider()
        val result = provider.fetchContactsDetails("userId1", "userId2")
        val expected = listOf(
            ContactDetails("userId1", MutableStateFlow("userId1")),
            ContactDetails("userId2", MutableStateFlow("userId2"))
        )
        assertEqualsContactDetails(expected, result)
    }
}