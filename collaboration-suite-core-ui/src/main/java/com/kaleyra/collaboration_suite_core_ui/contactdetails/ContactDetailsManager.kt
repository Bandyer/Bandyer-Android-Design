package com.kaleyra.collaboration_suite_core_ui.contactdetails

import android.net.Uri
import com.kaleyra.collaboration_suite.Contact
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.CollaborationContactDetailsProvider
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// TODO use updateDisplayDetails?, invoke when connected on call module
// TODO check if it is right also in the call link
object ContactDetailsManager {

    const val FETCH_TIMEOUT = 2500L

    private val collaborationContactDetailsProvider by lazy { CollaborationContactDetailsProvider() }

    private val contactNames = HashMap<String, StateFlow<String?>>()

    private val contactImages = HashMap<String, StateFlow<Uri?>>()

    private val contactNamesFlow = MutableSharedFlow<Map<String, StateFlow<String?>>>(1, 1, BufferOverflow.DROP_OLDEST)

    private val contactImagesFlow = MutableSharedFlow<Map<String, StateFlow<Uri?>>>(1, 1, BufferOverflow.DROP_OLDEST)

    private val mutex = Mutex()

    val Contact.combinedDisplayName: Flow<String?>
        get() = contactNamesFlow.mapToUserFlow(userId)

    val Contact.combinedDisplayImage: Flow<Uri?>
        get() = contactImagesFlow.mapToUserFlow(userId)

    init {
        contactNamesFlow.tryEmit(contactNames)
        contactImagesFlow.tryEmit(contactImages)
    }

    suspend fun refreshContactDetails(vararg userIds: String, timeout: Long = FETCH_TIMEOUT) = mutex.withLock {
        val fetchedContactDetails = collaborationContactDetailsProvider.fetchContactsDetails(userIds = userIds, timeout = timeout)
        fetchedContactDetails.forEach { (userId, name, image) ->
            contactNames[userId] = name
            contactImages[userId] = image
        }
        contactNamesFlow.emit(contactNames)
        contactImagesFlow.emit(contactImages)
    }

    private fun <T> Flow<Map<String, StateFlow<T>>>.mapToUserFlow(userId: String) = flatMapLatest { it[userId] ?: emptyFlow() }.distinctUntilChanged()

}
