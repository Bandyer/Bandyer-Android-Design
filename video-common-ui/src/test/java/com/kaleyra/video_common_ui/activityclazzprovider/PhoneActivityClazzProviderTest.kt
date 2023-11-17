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

import android.app.Activity
import io.mockk.every
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneActivityClazzProviderTest {

    private class CallActivity: Activity()
    private class ChatActivity: Activity()
    private class TermsActivity: Activity()

    private val callActivity = "com.kaleyra.video_sdk.call.PhoneCallActivity"

    private val chatActivity = "com.kaleyra.video_sdk.chat.PhoneChatActivity"

    private val termsActivity = "com.kaleyra.video_sdk.termsandconditions.PhoneTermsAndConditionsActivity"

    private val activityClazzProvider = spyk(PhoneActivityClazzProvider, recordPrivateCalls = true)

    @Test
    fun allActivityClassNameAvailable_getActivityClazzConfiguration_activityClazzConfiguration() {
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(callActivity) } returns CallActivity::class.java
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(chatActivity) } returns ChatActivity::class.java
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(termsActivity) } returns TermsActivity::class.java
        val result = activityClazzProvider.getActivityClazzConfiguration()
        val expected = ActivityClazzConfiguration(CallActivity::class.java, ChatActivity::class.java, TermsActivity::class.java)
        assertEquals(expected, result)
    }

    @Test
    fun callActivityClassNameNotAvailable_getActivityClazzConfiguration_null() {
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(callActivity) } returns null
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(chatActivity) } returns ChatActivity::class.java
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(termsActivity) } returns TermsActivity::class.java
        val result = activityClazzProvider.getActivityClazzConfiguration()
        assertEquals(null, result)
    }

    @Test
    fun chatActivityClassNameNotAvailable_getActivityClazzConfiguration_null() {
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(callActivity) } returns CallActivity::class.java
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(chatActivity) } returns null
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(termsActivity) } returns TermsActivity::class.java
        val result = activityClazzProvider.getActivityClazzConfiguration()
        assertEquals(null, result)
    }

    @Test
    fun termsActivityClassNameNotAvailable_getActivityClazzConfiguration_null() {
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(callActivity) } returns CallActivity::class.java
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(chatActivity) } returns ChatActivity::class.java
        every { activityClazzProvider invoke "getClassForName" withArguments listOf(termsActivity) } returns null
        val result = activityClazzProvider.getActivityClazzConfiguration()
        assertEquals(null, result)
    }
}