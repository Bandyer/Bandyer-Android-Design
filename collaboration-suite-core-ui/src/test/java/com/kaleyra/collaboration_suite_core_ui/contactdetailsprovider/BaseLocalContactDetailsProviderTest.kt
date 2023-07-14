package com.kaleyra.collaboration_suite_core_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.model.DefaultUsersDescription
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import io.mockk.mockk
import kotlinx.coroutines.delay

abstract class BaseLocalContactDetailsProviderTest {

    protected val uriUser1 = mockk<Uri>()

    protected val uriUser2 = mockk<Uri>()

    private val defaultUsers = hashMapOf(
        "userId1" to Pair("username1", uriUser1),
        "userId2" to Pair("username2", uriUser2),
    )

    protected fun usersDescriptionMock(fetchDelay: Long = 0L, users: Map<String, Pair<String, Uri>> = defaultUsers): UsersDescription {
        return DefaultUsersDescription(
            name = { userIds ->
                delay(fetchDelay)
                val userId = userIds.getOrNull(0)
                users[userId]?.first ?: "null"
            },
            image = { userIds ->
                delay(fetchDelay)
                val userId = userIds.getOrNull(0)
                users[userId]?.second ?: Uri.EMPTY
            }
        )
    }
}