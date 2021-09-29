package com.bandyer.video_android_glass_ui.utils.observers.network

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Utility class which allows to observe the cell signal state
 */
class CellSignalObserver @RequiresApi(Build.VERSION_CODES.M) constructor(context: Context) {

    private val signalState: MutableSharedFlow<SignalState> =
        MutableSharedFlow(onBufferOverflow = BufferOverflow.DROP_OLDEST, replay = 1)
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val phoneStateListener = object : PhoneStateListener() {
        @SuppressLint("NewApi")
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            signalState.tryEmit(
                when (signalStrength?.level) {
                    1 -> SignalState.POOR
                    2 -> SignalState.MODERATE
                    3 -> SignalState.GOOD
                    4 -> SignalState.GREAT
                    else -> SignalState.NONE
                }
            )

            super.onSignalStrengthsChanged(signalStrength)
        }
    }

    init {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    /**
     * Call to observe the cell signal events
     *
     * @return SharedFlow<SignalState>
     */
    fun observe(): SharedFlow<SignalState> = signalState.asSharedFlow()

    /**
     * Stop the observer
     */
    fun stop() = telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
}