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

import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.contactdetails.cachedprovider.CachedDefaultContactDetailsProvider
import com.kaleyra.video_common_ui.contactdetails.cachedprovider.CachedLocalContactDetailsProvider
import com.kaleyra.video_common_ui.contactdetails.cachedprovider.CachedRemoteContactDetailsProvider
import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class CollaborationContactDetailsProvider(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ContactDetailsProvider {

    private val primaryProvider: ContactDetailsProvider
        get() = localContactDetailsProvider ?: remoteContactDetailsProvider ?: defaultProvider

    private val fallbackProvider: ContactDetailsProvider
        get() = when (primaryProvider) {
            localContactDetailsProvider -> remoteContactDetailsProvider ?: defaultProvider
            else -> defaultProvider
        }

    private val defaultProvider: ContactDetailsProvider by lazy { CachedDefaultContactDetailsProvider() }

    private var localContactDetailsProvider: CachedLocalContactDetailsProvider? = null
        get() = with(KaleyraVideo.userDetailsProvider) {
            field?.takeIf { it.userDetailsProvider == this } ?: this?.let { CachedLocalContactDetailsProvider(it, ioDispatcher) }.apply { field = this }
        }

    private var remoteContactDetailsProvider: CachedRemoteContactDetailsProvider? = null
        get() = with(KaleyraVideo.collaboration?.contacts) {
            field?.takeIf { it.contacts == this } ?: this?.let { CachedRemoteContactDetailsProvider(it) }.apply { field = this }
        }

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> = coroutineScope {
        val primaryContactDetails = async(ioDispatcher) { primaryProvider.fetchContactsDetails(userIds = userIds) }
        val fallbackContactDetails = async(ioDispatcher) { fallbackProvider.fetchContactsDetails(userIds = userIds) }
        val defaultProviderContacts = async(ioDispatcher) { defaultProvider.fetchContactsDetails(userIds = userIds) }
        val (primaryResult, fallbackResult, defaultResult) = listOf(primaryContactDetails, fallbackContactDetails, defaultProviderContacts).awaitAll()
        val result = userIds.mapNotNull { userId ->
            primaryResult.find { it.userId == userId } ?: fallbackResult.find { it.userId == userId } ?: defaultResult.find { it.userId == userId }
        }
        result.toSet()
    }
}