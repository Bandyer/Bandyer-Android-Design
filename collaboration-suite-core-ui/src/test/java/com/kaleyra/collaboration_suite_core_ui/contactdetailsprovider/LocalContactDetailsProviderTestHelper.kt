package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.model.UserDetails
import com.kaleyra.collaboration_suite_core_ui.model.UserDetailsProvider
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

internal object LocalContactDetailsProviderTestHelper {

    val uriUser1 = mockk<Uri>()

    val uriUser2 = mockk<Uri>()

    val defaultUsers = hashMapOf(
        "userId1" to Pair("username1", uriUser1),
        "userId2" to Pair("username2", uriUser2),
    )

    fun usersDescriptionProviderMock(fetchDelay: Long = 0L, users: Map<String, Pair<String, Uri>> = defaultUsers) =
        object : UserDetailsProvider {
            override suspend fun userDetailsRequested(userIds: List<String>) = coroutineScope {
                val result = userIds.map { userId ->
                    async {
                        val name = async {
                            delay(fetchDelay)
                            users[userId]?.first ?: "null"
                        }
                        val image = async {
                            delay(fetchDelay)
                            users[userId]?.second ?: Uri.EMPTY
                        }
                        UserDetails(userId = userId, name = name.await(), image = image.await())
                    }
                }.awaitAll()
               return@coroutineScope Result.success(result)
            }
        }

    fun usersDescriptionProviderMock(exceptionToThrow: Exception) = object : UserDetailsProvider {
        override suspend fun userDetailsRequested(userIds: List<String>): Result<List<UserDetails>> {
            throw exceptionToThrow
        }
    }

}