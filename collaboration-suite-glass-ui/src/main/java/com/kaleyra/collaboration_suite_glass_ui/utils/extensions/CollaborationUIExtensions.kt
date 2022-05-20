/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.utils.extensions

import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_glass_ui.call.GlassCallActivity
import com.kaleyra.collaboration_suite_glass_ui.chat.ChatNotificationActivity
import com.kaleyra.collaboration_suite_glass_ui.chat.GlassChatActivity

/**
 * Set up with glass u i
 *
 * @param credentials to use when Collaboration tools need to be connected
 * @param configuration representing a set of info necessary to instantiate the communication
 */
fun CollaborationUI.setUpWithGlassUI(
    credentials: Collaboration.Credentials,
    configuration: Collaboration.Configuration
) = setUp(credentials, configuration, GlassCallActivity::class.java, GlassChatActivity::class.java, ChatNotificationActivity::class.java)

//fun CollaborationUI.setUpCustomGlassChatNotification(context: Context) {
//    listenToChats(context, chatBox, usersDescription ?: UsersDescription())
//}
//
//private fun listenToChats(context: Context, chatBox: ChatBox, usersDescription: UsersDescription): Job {
//        val hashMap = hashMapOf<String, String>()
//        val jobs = mutableListOf<Job>()
//        val chatNotificationManager2 = ChatNotificationManager2(ChatNotificationActivity::class.java)
//        return chatBox.channels.onEach { chats ->
//            jobs.forEach {
//                it.cancel()
//                it.join()
//            }
//            jobs.clear()
//            chats.forEach { chat ->
//                jobs += chat.messages
//                    .onEach onEachMessages@{ msgs ->
//                        msgs.other.firstOrNull { it.state.value is Message.State.Received }?.also {
//                            if (hashMap[chat.id] == it.id) return@onEachMessages
//                            hashMap[chat.id] = it.id
////                            _newMessages.emit(Pair(chat, it))
//
//                            val userId = it.creator.userId
//                            val username = usersDescription.name(listOf(userId))
//                            val message =
//                                (chat.messages.value.list.firstOrNull()?.content as? Message.Content.Text)?.message
//                                    ?: ""
//                            val imageUri = usersDescription.image(listOf(userId))
//
//                            chatNotificationManager2.notify(
//                                ChatNotification(
//                                    username,
//                                    userId,
//                                    message,
//                                    imageUri
//                                )
//                            )
//                        }
//                    }.launchIn(MainScope())
//            }
//        }.launchIn(MainScope())
//    }