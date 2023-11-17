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

package com.kaleyra.video_common_ui.activityclazzprovider

internal abstract class ActivityClazzProvider(
    private val callActivityClassName: String,
    private val chatActivityClassName: String,
    private val termsAndConditionsActivityClassName: String,
    private val customChatNotificationClassName: String? = null
) {

    fun getActivityClazzConfiguration(): ActivityClazzConfiguration? {
        val callActivityClazz = getClassForName(callActivityClassName)
        val chatActivityClazz = getClassForName(chatActivityClassName)
        val termsAndConditionsActivityClazz = getClassForName(termsAndConditionsActivityClassName)
        if (callActivityClazz == null || chatActivityClazz == null || termsAndConditionsActivityClazz == null) return null
        return customChatNotificationClassName?.let {
            val chatNotificationActivityClazz = getClassForName(customChatNotificationClassName) ?: return null
            ActivityClazzConfiguration(callClazz = callActivityClazz, chatClazz = chatActivityClazz, termsAndConditionsClazz = termsAndConditionsActivityClazz, customChatNotificationClazz = chatNotificationActivityClazz)
        } ?: ActivityClazzConfiguration(callClazz = callActivityClazz, chatClazz = chatActivityClazz, termsAndConditionsClazz = termsAndConditionsActivityClazz)
    }

    private fun getClassForName(className: String): Class<*>? = kotlin.runCatching { Class.forName(className) }.getOrNull()
}

