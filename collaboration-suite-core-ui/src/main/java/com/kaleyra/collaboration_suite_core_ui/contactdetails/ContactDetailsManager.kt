package com.kaleyra.collaboration_suite_core_ui.contactdetails

import android.net.Uri
import com.kaleyra.collaboration_suite.Contact
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.CollaborationContactDetailsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

// TODO update the remote details when they change
// TODO use updateDisplayDetails?, invoke when connected on call module
// TODO check if it is right also in the call link
object ContactDetailsManager {

    const val FETCH_TIMEOUT = 2500L

    val Contact.combinedDisplayName: StateFlow<String?>
        get() = MutableStateFlow(contactNames[userId])

    val Contact.combinedDisplayImage: StateFlow<Uri?>
        get() = MutableStateFlow(contactImages[userId])

    private val collaborationContactDetailsProvider by lazy { CollaborationContactDetailsProvider() }

    private val contactNames = ConcurrentHashMap<String, String?>()

    private val contactImages = ConcurrentHashMap<String, Uri?>()

    private val mutex = Mutex()

    suspend fun refreshContactDetails(vararg userIds: String, timeout: Long = FETCH_TIMEOUT) = mutex.withLock {
        val fetchedContactDetails = collaborationContactDetailsProvider.fetchContactsDetails(userIds = userIds, timeout = timeout)
        fetchedContactDetails.forEach {
            contactNames[it.userId] = it.name
            contactImages[it.userId] = it.image
        }
    }

}
