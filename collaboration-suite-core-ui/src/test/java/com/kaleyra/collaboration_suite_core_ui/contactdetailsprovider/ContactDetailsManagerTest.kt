package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.collaboration_suite.Contact
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.contactdetails.model.ContactDetails
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.CollaborationContactDetailsProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactDetailsManagerTest {

    @Before
    fun setUp() {
        mockkConstructor(CollaborationContactDetailsProvider::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testRefreshContactDetails() = runTest {
        val uri = mockk<Uri>()
        coEvery { anyConstructed<CollaborationContactDetailsProvider>().fetchContactsDetails("userId") } returns setOf(ContactDetails("userId", "username", uri))
        val contact = object : Contact {
            override val userId: String = "userId"
            override val restrictions: Contact.Restrictions = mockk()
            override val displayName: StateFlow<String?> = MutableStateFlow(null)
            override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
        }
        ContactDetailsManager.refreshContactDetails("userId")
        coVerify { anyConstructed<CollaborationContactDetailsProvider>().fetchContactsDetails("userId") }
        assertEquals("username", contact.combinedDisplayName.first())
        assertEquals(uri, contact.combinedDisplayImage.first())
    }
}