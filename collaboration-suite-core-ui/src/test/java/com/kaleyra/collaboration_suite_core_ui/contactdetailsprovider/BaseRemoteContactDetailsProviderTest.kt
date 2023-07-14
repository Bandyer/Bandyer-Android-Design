package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.collaboration_suite.Contact
import com.kaleyra.collaboration_suite.Contacts
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher

abstract class BaseRemoteContactDetailsProviderTest {

    protected class ContactMock(
        override val userId: String,
        override val displayName: StateFlow<String?> = MutableStateFlow(null),
        override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
    ): Contact {
        override val restrictions: Contact.Restrictions = mockk()
    }

    protected class ContactsMock(private val contacts: Map<String, Contact>): Contacts {
        override val collection: Map<String, Contact> = HashMap()
        override suspend fun get(userId: String): Result<Contact> {
            return contacts[userId]?.let {
                Result.success(it)
            } ?: Result.failure(Exception("contact not found"))
        }
        override suspend fun me(): Contact.Me = mockk()
        override fun clear() = Unit
    }


    protected val uriContact1 = mockk<Uri>()

    protected val uriContact2 = mockk<Uri>()
}