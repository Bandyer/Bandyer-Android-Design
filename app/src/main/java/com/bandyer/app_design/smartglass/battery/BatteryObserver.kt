package com.bandyer.app_design.smartglass.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

class BatteryObserver(context: Context) {

    private val weakContext: WeakReference<Context> = WeakReference(context)
    private val batteryState: MutableSharedFlow<BatteryState> =
        MutableSharedFlow(onBufferOverflow = BufferOverflow.DROP_OLDEST, replay = 1)
    private val broadcastReceiver: BroadcastReceiver = BatteryReceiver()

    init {
        context.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    fun observe(): SharedFlow<BatteryState> = batteryState.asSharedFlow()

    fun stop() = weakContext.get()?.unregisterReceiver(broadcastReceiver)

    inner class BatteryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            val defaultValue = -1
            val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, defaultValue)
            val plugged: Int = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, defaultValue)
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, defaultValue)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, defaultValue)

            batteryState.tryEmit(BatteryState(mapStatus(status), mapPlugged(plugged), computePercentage(level, scale)))
        }

        private fun mapStatus(status: Int): BatteryState.Status = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryState.Status.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryState.Status.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> BatteryState.Status.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryState.Status.NOT_CHARGING
            else -> BatteryState.Status.UNKNOWN
        }

        private fun mapPlugged(plugged: Int): BatteryState.Plugged = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> BatteryState.Plugged.AC
            BatteryManager.BATTERY_PLUGGED_USB -> BatteryState.Plugged.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> BatteryState.Plugged.WIRELESS
            else -> BatteryState.Plugged.UNKNOWN
        }

        private fun computePercentage(level: Int, scale: Int): Int =
            if (level > 0 && scale > 0) (level * 100 / scale.toFloat()).roundToInt()
            else 0
    }
}

