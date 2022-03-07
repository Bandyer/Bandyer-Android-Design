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

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity

/**
 * Boundouble activity to service
 *
 * @suppress
 */
abstract class BoundServiceActivity<T : BoundService>(private val clazz: Class<T>) : AppCompatActivity() {

    interface Observer {
        fun onServiceBound()
    }

    private val observers: ArrayList<Observer> = arrayListOf()

    private var serviceConnection: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                val service = (binder as BoundService.ServiceBinder).getService<T>()
                onServiceBound(service)
                notifyServiceBound()
            }

            override fun onServiceDisconnected(componentName: ComponentName) = Unit
        }

        with(applicationContext) {
            val intent = Intent(this, clazz)
            bindService(intent, serviceConnection!!, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationContext.unbindService(serviceConnection!!)
        serviceConnection = null
    }

    abstract fun onServiceBound(service: T)

    /**
     * Add an observer
     *
     * @param observer Observer
     */
    fun addServiceBoundObserver(observer: Observer) {
        observers.add(observer)
    }

    /**
     * Remove an observer
     *
     * @param observer Observer
     */
    fun removeServiceBoundObserver(observer: Observer) {
        observers.remove(observer)
    }

    /**
     * Notify all observers
     */
    private fun notifyServiceBound() {
        observers.forEach { it.onServiceBound() }
    }
}