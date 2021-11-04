package com.bandyer.video_android_glass_ui.utils.observers.network

import android.Manifest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utility class which allows to observe the internet state. It tells if there is actually internet connection.
 */
internal class InternetObserver @RequiresPermission(Manifest.permission.INTERNET) constructor(private val intervalInMs: Long) {

    private val isConnectedFlow: MutableSharedFlow<Boolean> =
        MutableSharedFlow(onBufferOverflow = BufferOverflow.DROP_OLDEST, replay = 1)
    private var job: Job = CoroutineScope(Dispatchers.IO).launch {
        while (true) {
            isConnectedFlow.emit(isConnected())
            delay(intervalInMs)
        }
    }

    /**
     * Call to observe the internet state. It returns true if internet is reachable, false otherwise
     *
     * @return SharedFlow<Boolean>
     */
    fun observe(): Flow<Boolean> = isConnectedFlow.distinctUntilChanged()

    /**
     * Stop the observer
     */
    fun stop() = job.cancel()

    private fun isConnected(): Boolean {
        var urlConnection: HttpURLConnection? = null
        val result = kotlin.runCatching {
            urlConnection = initConnection()
            urlConnection!!.responseCode == 204
        }.getOrNull() ?: false
        urlConnection?.disconnect()
        return result
    }

    private fun initConnection() =
        (URL(HOST).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT
            readTimeout = READ_TIMEOUT
            instanceFollowRedirects = false
            useCaches = false
        }

    private companion object {
        const val HOST = "https://clients3.google.com/generate_204"
        const val CONNECT_TIMEOUT = 10000
        const val READ_TIMEOUT = 10000
    }
}