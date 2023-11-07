package com.kaleyra.video_common_ui.contactdetails.provider

import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import com.kaleyra.video_common_ui.model.UserDetailsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

internal class LocalContactDetailsProvider(
    private val userDetailsProvider: UserDetailsProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContactDetailsProvider {

    override suspend fun fetchContactsDetails(
        vararg userIds: String,
        timeout: Long
    ): Set<ContactDetails> = coroutineScope {
        runCatching {
            withTimeout(timeout) {
                withContext(ioDispatcher) {
                    val userDetails = userDetailsProvider(userIds.toList()).getOrNull()
                    val contactsDetails = userDetails?.map { (userId, name, image) ->
                        ContactDetails(
                            userId = userId,
                            name = MutableStateFlow(name),
                            image = MutableStateFlow(image)
                        )
                    }
                    contactsDetails
                }
            }
        }.getOrNull()?.toSet() ?: setOf()
    }
}