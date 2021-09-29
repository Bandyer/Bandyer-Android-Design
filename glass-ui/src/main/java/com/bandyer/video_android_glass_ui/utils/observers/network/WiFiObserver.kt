package com.bandyer.video_android_glass_ui.utils.observers.network

import android.Manifest.permission.ACCESS_WIFI_STATE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.lang.ref.WeakReference

/**
 * Utility class which allows to observe the WiFi info events
 */
class WiFiObserver @RequiresPermission(ACCESS_WIFI_STATE) constructor(context: Context) {

    private val weakContext: WeakReference<Context> = WeakReference(context)
    private val wifiInfo: MutableSharedFlow<WiFiInfo> =
        MutableSharedFlow(onBufferOverflow = BufferOverflow.DROP_OLDEST, replay = 1)
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val intentFilter = IntentFilter().apply {
        addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        addAction(WifiManager.RSSI_CHANGED_ACTION)
    }
    private val broadcastReceiver: BroadcastReceiver = WiFiReceiver()

    init {
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    /**
     * Call to observe the wifi info events
     *
     * @return SharedFlow<WiFiInfo>
     */
    fun observe(): SharedFlow<WiFiInfo> = wifiInfo.asSharedFlow()

    /**
     * Stop the observer
     */
    fun stop() = weakContext.get()?.unregisterReceiver(broadcastReceiver)

    /**
     * A broadcast receiver which handle the WiFi events
     */
    inner class WiFiReceiver : BroadcastReceiver() {
        private var state = WifiManager.WIFI_STATE_UNKNOWN

        /**
         * @suppress
         */
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if(intent.action == WifiManager.WIFI_STATE_CHANGED_ACTION)
                state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)

            val rssi = wifiManager.connectionInfo.rssi
            wifiInfo.tryEmit(WiFiInfo(mapState(state), WiFiInfo.Level.getValue(rssi)))
        }

        private fun mapState(state: Int): WiFiInfo.State = when (state) {
            WifiManager.WIFI_STATE_ENABLING -> WiFiInfo.State.ENABLING
            WifiManager.WIFI_STATE_ENABLED -> WiFiInfo.State.ENABLED
            WifiManager.WIFI_STATE_DISABLING -> WiFiInfo.State.DISABLING
            WifiManager.WIFI_STATE_DISABLED -> WiFiInfo.State.DISABLED
            else -> WiFiInfo.State.UNKNOWN
        }
    }
}