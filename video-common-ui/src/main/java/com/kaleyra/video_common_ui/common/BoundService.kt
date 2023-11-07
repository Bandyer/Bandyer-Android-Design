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

package com.kaleyra.video_common_ui.common

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import java.lang.ref.WeakReference

/**
 * Bound service
 */
abstract class BoundService : LifecycleService() {

    private var binder: BoundServiceBinder? = null

    /**
     * @suppress
     */
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder ?: BoundServiceBinder(this).also { binder = it }
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        binder = null
    }
}

/**
 * BoundService binder
 *
 * @property service A BoundService
 * @constructor
 */
internal class BoundServiceBinder(svc: BoundService): Binder() {
    private var service: WeakReference<BoundService> = WeakReference(svc)

    @Suppress("UNCHECKED_CAST")
    fun <T : BoundService> getService(): T = service.get() as T
}
