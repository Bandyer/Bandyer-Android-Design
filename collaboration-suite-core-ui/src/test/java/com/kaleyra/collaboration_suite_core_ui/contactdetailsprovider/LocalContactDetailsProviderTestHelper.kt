package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.model.UserDescription
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescriptionProvider
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
        object : UsersDescriptionProvider {
            override suspend fun fetchUserDescription(userId: String): UserDescription = coroutineScope {
                val name = async {
                    delay(fetchDelay)
                    users[userId]?.first ?: "null"
                }
                val image = async {
                    delay(fetchDelay)
                    users[userId]?.second ?: Uri.EMPTY
                }
                UserDescription(name = name.await(), image = image.await())
            }
        }

    fun usersDescriptionProviderMock(exceptionToThrow: Exception) = object : UsersDescriptionProvider {
        override suspend fun fetchUserDescription(userId: String): UserDescription {
            throw exceptionToThrow
        }
    }

}