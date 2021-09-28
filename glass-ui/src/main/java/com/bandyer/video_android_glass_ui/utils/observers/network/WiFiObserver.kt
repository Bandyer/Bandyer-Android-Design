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

class WiFiObserver @RequiresPermission(ACCESS_WIFI_STATE) constructor(context: Context) {

    private val weakContext: WeakReference<Context> = WeakReference(context)
    private val wifiState: MutableSharedFlow<WiFiState> =
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

    fun observe(): SharedFlow<WiFiState> = wifiState.asSharedFlow()

    fun stop() = weakContext.get()?.unregisterReceiver(broadcastReceiver)

    inner class WiFiReceiver : BroadcastReceiver() {
        var state = WifiManager.WIFI_STATE_UNKNOWN

        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if(intent.action == WifiManager.WIFI_STATE_CHANGED_ACTION)
                state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)

            val rssi = wifiManager.connectionInfo.rssi
            wifiState.tryEmit(WiFiState(mapState(state), WiFiState.Level.getValue(rssi)))
        }

        private fun mapState(state: Int): WiFiState.State = when (state) {
            WifiManager.WIFI_STATE_ENABLING -> WiFiState.State.ENABLING
            WifiManager.WIFI_STATE_ENABLED -> WiFiState.State.ENABLED
            WifiManager.WIFI_STATE_DISABLING -> WiFiState.State.DISABLING
            WifiManager.WIFI_STATE_DISABLED -> WiFiState.State.DISABLED
            else -> WiFiState.State.UNKNOWN
        }
    }
}