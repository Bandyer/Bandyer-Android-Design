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

package com.kaleyra.demo_video_sdk

import android.content.Context
import com.kaleyra.app_utilities.MultiDexApplication
import com.kaleyra.app_utilities.storage.ConfigurationPrefsManager
import com.kaleyra.video.configuration.Configuration
import com.kaleyra.video.configuration.Environment
import com.kaleyra.video.configuration.Region
import com.kaleyra.video.utils.logger.CHAT_BOX
import com.kaleyra.video.utils.logger.COLLABORATION
import com.kaleyra.video.utils.logger.INPUTS
import com.kaleyra.video.utils.logger.PHONE_BOX
import com.kaleyra.video.utils.logger.PHONE_CALL
import com.kaleyra.video.utils.logger.STREAMS
import com.kaleyra.video_utils.logging.BaseLogger
import com.kaleyra.video_utils.logging.androidPrioryLogger
import kotlinx.coroutines.CompletableDeferred

class MyApplication : MultiDexApplication() {

    override fun create() {
        // init
    }

}

fun Context.configuration(): Configuration {
    val appConfiguration = ConfigurationPrefsManager.getConfiguration(this)
    return Configuration(
        appConfiguration.appId,
        Environment.create(appConfiguration.environment),
        Region.create(appConfiguration.region),
        httpStack = MultiDexApplication.okHttpClient,
        logger = androidPrioryLogger(BaseLogger.DEBUG, COLLABORATION or PHONE_CALL or PHONE_BOX or CHAT_BOX or STREAMS or INPUTS)
    )
}

suspend fun requestToken(userId: String): Result<String> = runCatching {
    val result = CompletableDeferred<Result<String>>()
    MultiDexApplication.restApi.getAccessToken(userId,
                                                   onSuccess = { result.complete(Result.success(it)) },
                                                   onError = { result.complete(Result.failure(it)) })
    result.await()
}.getOrDefault(Result.failure(IllegalStateException("Failed to get AccessToken")))