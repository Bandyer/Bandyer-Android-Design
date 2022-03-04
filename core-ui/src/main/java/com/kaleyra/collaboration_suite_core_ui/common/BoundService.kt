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

package com.kaleyra.collaboration_suite_core_ui.common

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService

abstract class BoundService : LifecycleService() {
    @Suppress("UNCHECKED_CAST")
    inner class ServiceBinder : Binder() {
        fun <T : BoundService> getService(): T = this@BoundService as T
    }

    private var binder: ServiceBinder? = null

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return ServiceBinder().also { binder = it }
    }

    override fun onDestroy() {
        super.onDestroy()
        binder = null
    }
}