package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.DefaultContactDetailsProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultContactDetailsProviderTest {

    private val successResult = setOf(
        ContactDetails("userId1", "userId1"),
        ContactDetails("userId2", "userId2")
    )

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
        Assert.assertEquals(successResult, result)
    }
}