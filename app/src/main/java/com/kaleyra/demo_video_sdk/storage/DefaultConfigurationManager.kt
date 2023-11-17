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

package com.kaleyra.demo_video_sdk.storage

import android.annotation.SuppressLint
import android.content.Context
import com.kaleyra.demo_video_sdk.ui.custom_views.CallConfiguration
import com.kaleyra.video_utils.ContextRetainer.Companion.context


object DefaultConfigurationManager {

    private const val preferenceKey = "DefaultConfigurationPrefs"
    private const val callConfigurationKey = "CALL_CONFIGURATION"

    private val prefs by lazy { context.getSharedPreferences(preferenceKey, Context.MODE_PRIVATE) }

    fun getDefaultCallConfiguration(): CallConfiguration = prefs.getString(callConfigurationKey, null)?.let { CallConfiguration.decode(it) } ?: CallConfiguration()

    @SuppressLint("ApplySharedPref")
    fun saveDefaultCallConfiguration(callConfiguration: CallConfiguration) = prefs.edit().apply {
        putString(callConfigurationKey, callConfiguration.encode())
        commit()
    }

    @SuppressLint("ApplySharedPref")
    fun clearAll() = prefs.edit().apply {
        clear()
        commit()
    }
}