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

package com.kaleyra.video_common_ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.IBinder
import android.util.Log
import com.kaleyra.video_common_ui.common.BoundService
import com.kaleyra.video_common_ui.common.BoundServiceBinder
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Collaboration service
 */
abstract class KaleyraVideoService : BoundService() {

    /**
     * Companion
     **/
    companion object {

        /**
         *  A service that is used to configure collaboration.
         **/
        suspend fun get(): KaleyraVideoService? = getKaleyraVideoService()
    }

    /**
     * On request new collaboration set up
     */
    abstract suspend fun onRequestKaleyraVideoConfigure()
}

@SuppressLint("QueryPermissionsNeeded")
private fun getService(context: Context): ResolveInfo? {
    val serviceIntent = Intent().setAction("kaleyra_collaboration_configure").setPackage(context.packageName)
    val resolveInfo = context.packageManager.queryIntentServices(serviceIntent, PackageManager.GET_RESOLVED_FILTER)
    if (resolveInfo.size < 1) return null
    return resolveInfo[0]
}

private suspend fun getKaleyraVideoService(): KaleyraVideoService? = with(ContextRetainer.context) {
    val name = getService(this)?.serviceInfo?.name ?: return null
    val intent = Intent(this, Class.forName(name))
    startService(intent)
    return withTimeoutOrNull(1000L) {
        suspendCancellableCoroutine<KaleyraVideoService> { continuation ->
            bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val kaleyraVideoService = (service as BoundServiceBinder).getService<KaleyraVideoService>()
                    continuation.resumeWith(Result.success(kaleyraVideoService))
                }

                override fun onServiceDisconnected(name: ComponentName?) = Unit
            }, 0)
            continuation.invokeOnCancellation {
                Log.e("Collaboration", "Collaboration was not set up, you had 1s to do it. And you didn't do it. reason = $it")
            }
        }
    }
}
