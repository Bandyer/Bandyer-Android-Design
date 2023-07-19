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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactDetailsManagerTest {

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager, recordPrivateCalls = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test contact name and image are updated after flow subscription`() = runTest {
        val userId = "userId2"
        val username = "username2"
        val uri = mockk<Uri>()
        val provider = mockkClass(CollaborationContactDetailsProvider::class)
        val contact = object : Contact {
            override val userId: String = userId
            override val restrictions: Contact.Restrictions = mockk()
            override val displayName: StateFlow<String?> = MutableStateFlow(null)
            override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
        }
        every { ContactDetailsManager getProperty "collaborationContactDetailsProvider" } answers { provider }
        coEvery { provider.fetchContactsDetails(any()) } returns setOf(ContactDetails(userId, username, uri))

        val names = mutableListOf<String?>()
        val images = mutableListOf<Uri?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            contact.combinedDisplayName.toList(names)
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            contact.combinedDisplayImage.toList(images)
        }
        assertEquals(null, names[0])
        assertEquals(null, images[0])

        ContactDetailsManager.refreshContactDetails(userId)

        assertEquals(username, names[1])
        assertEquals(uri, images[1])
    }

    @Test
    fun testRefreshContactDetails() = runTest {
        val userId = "userId"
        val username = "username"
        val uri = mockk<Uri>()
        val provider = mockkClass(CollaborationContactDetailsProvider::class)
        val contact = object : Contact {
            override val userId: String = userId
            override val restrictions: Contact.Restrictions = mockk()
            override val displayName: StateFlow<String?> = MutableStateFlow(null)
            override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
        }
        every { ContactDetailsManager getProperty "collaborationContactDetailsProvider" } answers { provider }
        coEvery { provider.fetchContactsDetails(any()) } returns setOf(ContactDetails(userId, username, uri))

        ContactDetailsManager.refreshContactDetails(userId)

        coVerify { provider.fetchContactsDetails(userId) }
        assertEquals(username, contact.combinedDisplayName.first())
        assertEquals(uri, contact.combinedDisplayImage.first())
    }
}