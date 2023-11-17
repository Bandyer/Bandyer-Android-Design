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

package com.kaleyra.video_common_ui.contactdetailsprovider

import android.net.Uri
import com.kaleyra.video_common_ui.model.UserDetails
import com.kaleyra.video_common_ui.model.UserDetailsProvider
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

    fun usersDescriptionProviderMock(
        fetchDelay: Long = 0L,
        users: Map<String, Pair<String, Uri>> = defaultUsers
    ): UserDetailsProvider {
        val lambda: UserDetailsProvider = { userIds: List<String> ->
            coroutineScope {
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
                Result.success(result)
            }
        }
        return lambda
    }

    fun usersDescriptionProviderMock(exceptionToThrow: Exception) = { _: List<String> ->
        throw exceptionToThrow
    }

}