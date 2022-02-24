package com.bandyer.video_android_glass_ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity

abstract class BoundServiceActivity<T : BoundService>(private val clazz: Class<T>) :
    AppCompatActivity() {

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
            startService(intent)
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