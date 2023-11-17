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

package com.kaleyra.app_utilities

import android.content.Context
import com.bandyer.flipper_socket_io_plugin.FlipperOKHttpClient
import com.bandyer.flipper_socket_io_plugin.SIONetworkFlipperPlugin
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.leakcanary2.LeakCanary2FlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin.SharedPreferencesDescriptor
import com.facebook.soloader.SoLoader
import com.kaleyra.app_utilities.storage.ConfigurationPrefsManager
import com.kaleyra.app_utilities.storage.LoginManager

object FlipperManager {

    fun enable(context: Context) {
        SoLoader.init(context, false)
        if (!FlipperUtils.shouldEnableFlipper(context)) return
        val client = AndroidFlipperClient.getInstance(context)
        with(client) {
            addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
            addPlugin(DatabasesFlipperPlugin(context))
            addPlugin(
                SharedPreferencesFlipperPlugin(
                    context, listOf(
                        SharedPreferencesDescriptor(ConfigurationPrefsManager.CONFIGURATION_PREFS, Context.MODE_PRIVATE),
                        SharedPreferencesDescriptor(LoginManager.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    )
                )
            )
            addPlugin(CrashReporterPlugin.getInstance())
            addPlugin(LeakCanary2FlipperPlugin())
            val socketIOFlipperPlugin = SIONetworkFlipperPlugin(context)
            MultiDexApplication.okHttpClient = FlipperOKHttpClient(socketIOFlipperPlugin)
            client.addPlugin(socketIOFlipperPlugin)
            start()
        }
    }
}