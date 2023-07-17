package com.kaleyra.collaboration_suite_core_ui.contactdetails

import android.net.Uri
import com.kaleyra.collaboration_suite.Contact
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.CollaborationContactDetailsProvider
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

// TODO update the remote details when they change
// TODO use updateDisplayDetails?, invoke when connected on call module
// TODO check if it is right also in the call link
object ContactDetailsManager {

    const val FETCH_TIMEOUT = 2500L

    private val collaborationContactDetailsProvider by lazy { CollaborationContactDetailsProvider() }

    private val contactNames = MutableStateFlow(HashMap<String, String?>())

    private val contactImages = MutableStateFlow(HashMap<String, Uri?>())

    private val mutex = Mutex()

    val Contact.combinedDisplayName: Flow<String?>
        get() = contactNames.map { it[userId] }

    val Contact.combinedDisplayImage: Flow<Uri?>
        get() = contactImages.map { it[userId] }

    suspend fun refreshContactDetails(vararg userIds: String, timeout: Long = FETCH_TIMEOUT) = mutex.withLock {
        val fetchedContactDetails = collaborationContactDetailsProvider.fetchContactsDetails(userIds = userIds, timeout = timeout)
        fetchedContactDetails.forEach { contactDetails ->
            contactNames.update { it.apply { this[contactDetails.userId] = contactDetails.name } }
            contactImages.update { it.apply { this[contactDetails.userId] = contactDetails.image } }
        }
    }

}
