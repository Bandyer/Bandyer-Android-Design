package com.kaleyra.video_common_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.video.Contact
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import com.kaleyra.video_common_ui.contactdetails.provider.CollaborationContactDetailsProvider
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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
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
        coEvery { provider.fetchContactsDetails(any()) } returns setOf(ContactDetails(userId, MutableStateFlow(username), MutableStateFlow(uri)))

        ContactDetailsManager.refreshContactDetails(userId)

        coVerify { provider.fetchContactsDetails(userId) }
        assertEquals(username, contact.combinedDisplayName.first())
        assertEquals(uri, contact.combinedDisplayImage.first())
    }

    @Test
    fun `test contact name and image are updated when flow value is updated`() = runTest {
        val userId = "userId2"
        val username = "username2"
        val uri = mockk<Uri>()
        val usernameFlow = MutableStateFlow(username)
        val uriFlow = MutableStateFlow(uri)
        val provider = mockkClass(CollaborationContactDetailsProvider::class)
        val contact = object : Contact {
            override val userId: String = userId
            override val restrictions: Contact.Restrictions = mockk()
            override val displayName: StateFlow<String?> = MutableStateFlow(null)
            override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
        }
        every { ContactDetailsManager getProperty "collaborationContactDetailsProvider" } answers { provider }
        coEvery { provider.fetchContactsDetails(any()) } returns setOf(ContactDetails(userId, usernameFlow, uriFlow))

        ContactDetailsManager.refreshContactDetails(userId)

        assertEquals(username, contact.combinedDisplayName.first())
        assertEquals(uri, contact.combinedDisplayImage.first())

        val newUsername = "newUsername2"
        val newUri = mockk<Uri>()
        usernameFlow.value = newUsername
        uriFlow.value = newUri

        assertEquals(newUsername, contact.combinedDisplayName.first())
        assertEquals(newUri, contact.combinedDisplayImage.first())
    }

    @Test
    fun `test contact name and image are updated when flow changes`() = runTest {
        val userId = "userId3"
        val username = "username3"
        val uri = mockk<Uri>()
        val provider = mockkClass(CollaborationContactDetailsProvider::class)
        val contact = object : Contact {
            override val userId: String = userId
            override val restrictions: Contact.Restrictions = mockk()
            override val displayName: StateFlow<String?> = MutableStateFlow(null)
            override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
        }
        every { ContactDetailsManager getProperty "collaborationContactDetailsProvider" } answers { provider }
        coEvery { provider.fetchContactsDetails(any()) } returns setOf(ContactDetails(userId, MutableStateFlow(username), MutableStateFlow(uri)))

        val combinedDisplayName = withTimeoutOrNull(100) { contact.combinedDisplayName.first() }
        val combinedDisplayImage = withTimeoutOrNull(100) { contact.combinedDisplayImage.first() }
        assertEquals(null, combinedDisplayName)
        assertEquals(null, combinedDisplayImage)

        ContactDetailsManager.refreshContactDetails(userId)

        assertEquals(username, contact.combinedDisplayName.first())
        assertEquals(uri, contact.combinedDisplayImage.first())
    }
}