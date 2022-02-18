package com.bandyer.video_android_glass_ui

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.suspendCancellableCoroutine

internal object Extensions {

    inline fun <T : CallService> FragmentActivity.bindCallService(
        clazz: Class<T>,
        crossinline onConnected: (service: T) -> Unit,
        crossinline onDisconnected: () -> Unit
    ) {
        var serviceConnection: ServiceConnection?

        onLifecycleEvent(
            onCreate = {
                serviceConnection = serviceConnection(
                    onServiceConnected = { _, binder -> (binder as CallService.ServiceBinder).getService<T>().also { onConnected(it) } },
                    onServiceDisconnected = { onDisconnected() }
                )
                bindService(serviceConnection!!, clazz, 0)
            },
            onDestroy = {
                serviceConnection = null
            },
        )
    }

    inline fun LifecycleOwner.onLifecycleEvent(
        crossinline onCreate: (() -> Unit) = { },
        crossinline onStart: (() -> Unit) = { },
        crossinline onResume: (() -> Unit) = { },
        crossinline onPause: (() -> Unit) = { },
        crossinline onStop: (() -> Unit) = { },
        crossinline onDestroy: (() -> Unit) = { },
        crossinline onAny: (() -> Unit) = { },
    ) {
        var observer: LifecycleEventObserver? = null

        val scope = MainScope() + Dispatchers.Main.immediate
        scope.launch {
            try {
                suspendCancellableCoroutine<Unit> { cont ->
                    observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_CREATE -> onCreate()
                            Lifecycle.Event.ON_START -> onStart()
                            Lifecycle.Event.ON_RESUME -> onResume()
                            Lifecycle.Event.ON_PAUSE -> onPause()
                            Lifecycle.Event.ON_STOP -> onStop()
                            Lifecycle.Event.ON_DESTROY -> {
                                onDestroy()
                                cont.resumeWith(Result.success(Unit))
                            }
                            else -> Unit
                        }
                        onAny()
                    }

                    lifecycle.addObserver(observer!!)
                }
            } finally {
                lifecycle.removeObserver(observer!!)
                observer = null
            }
        }
    }

    private inline fun serviceConnection(
        crossinline onServiceConnected: (className: ComponentName, binder: IBinder) -> Unit,
        crossinline onServiceDisconnected: (className: ComponentName) -> Unit
    ): ServiceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, binder: IBinder) =
                onServiceConnected(className, binder)

            override fun onServiceDisconnected(componentName: ComponentName) =
                onServiceDisconnected(componentName)
        }

    private fun <T : Service> Context.bindService(
        serviceConnection: ServiceConnection,
        clazz: Class<T>,
        flags: Int
    ) {
        Intent(applicationContext, clazz).also { intent ->
            applicationContext.bindService(intent, serviceConnection, flags)
        }
    }
}