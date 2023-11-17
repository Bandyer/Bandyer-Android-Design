/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui.contactdetails

import android.net.Uri
import com.kaleyra.video.Contact
import com.kaleyra.video_common_ui.contactdetails.provider.CollaborationContactDetailsProvider
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
