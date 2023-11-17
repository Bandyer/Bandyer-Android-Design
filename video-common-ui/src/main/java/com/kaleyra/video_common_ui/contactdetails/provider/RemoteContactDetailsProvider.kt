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

package com.kaleyra.video_common_ui.contactdetails.provider

import com.kaleyra.video.Contacts
import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import kotlinx.coroutines.coroutineScope

internal class RemoteContactDetailsProvider(val contacts: Contacts) : ContactDetailsProvider {

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> = coroutineScope {
        runCatching {
            val userContacts = userIds.mapNotNull { contacts.get(it).getOrNull() }
            userContacts.map { contact -> ContactDetails(userId = contact.userId, name = contact.displayName, image = contact.displayImage) }
        }.getOrNull()?.toSet() ?: setOf()
    }

}