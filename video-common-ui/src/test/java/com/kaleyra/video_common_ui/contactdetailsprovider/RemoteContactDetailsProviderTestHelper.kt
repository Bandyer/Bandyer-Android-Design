package com.kaleyra.video_common_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.video.Contact
import com.kaleyra.video.Contacts
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal object RemoteContactDetailsProviderTestHelper {

    class ContactMock(
        override val userId: String,
        override val displayName: StateFlow<String?> = MutableStateFlow(null),
        override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
    ): Contact {
        override val restrictions: Contact.Restrictions = mockk()
    }

    class ContactsMock(private val contacts: Map<String, Contact>): Contacts {
        override val collection: Map<String, Contact> = HashMap()
        override suspend fun get(userId: String): Result<Contact> {
            return contacts[userId]?.let {
                Result.success(it)
            } ?: Result.failure(Exception("contact not found"))
        }
        override suspend fun me(): Contact.Me = mockk()
        override fun clear() = Unit
    }

    val uriUser1 = mockk<Uri>()

    val uriUser2 = mockk<Uri>()
}