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

package com.kaleyra.video_common_ui.contactdetails.cachedprovider

import com.kaleyra.video_common_ui.Cache
import com.kaleyra.video_common_ui.PerpetualCache
import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import com.kaleyra.video_common_ui.contactdetails.provider.ContactDetailsProvider

internal abstract class CachedContactDetailsProvider(private val provider: ContactDetailsProvider) :
    ContactDetailsProvider {

    private val cache: com.kaleyra.video_common_ui.Cache<String, ContactDetails> = com.kaleyra.video_common_ui.PerpetualCache()

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> {
        val cached: List<ContactDetails> = userIds.mapNotNull { cache[it] }
        val contactsDetails: MutableList<ContactDetails> = cached.toMutableList()

        val cachedIds: List<String> = cached.map { it.userId }
        val fetchIds: Set<String> = userIds.toSet() - cachedIds.toSet()
        if (fetchIds.isNotEmpty()) {
            val fetched =
                provider.fetchContactsDetails(userIds = fetchIds.toTypedArray(), timeout = timeout)
            contactsDetails += fetched
            fetched.forEach { cache[it.userId] = it }
        }
        return contactsDetails.toSet()
    }
}