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

package com.kaleyra.collaboration_suite_glass_ui.status_bar_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.kaleyra.collaboration_suite_core_ui.utils.TimerParser
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.blink
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassStatusBarLayoutBinding


/**
 * A custom state bar view
 *
 * @constructor
 */
internal class StatusBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    /**
     * Cell signal's states
     */
    enum class CellSignalState {
        /**
         * m i s s i n g
         */
        MISSING,

        /**
         * l o w
         */
        LOW,

        /**
         * m o d e r a t e
         */
        MODERATE,

        /**
         * g o o d
         */
        GOOD,

        /**
         * f u l l
         */
        FULL
    }

    /**
     * Wifi signal's states
     */
    enum class WiFiSignalState {
        /**
         * d i s a b l e d
         */
        DISABLED,

        /**
         * l o w
         */
        LOW,

        /**
         * m o d e r a t e
         */
        MODERATE,

        /**
         * f u l l
         */
        FULL
    }

    private var binding: KaleyraGlassStatusBarLayoutBinding =
        KaleyraGlassStatusBarLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        hideCamMutedIcon()
        hideMicMutedIcon()
        hideChatIcon()
    }

    /**
     * Show the recording icon and text
     */
    fun showRec() {
        binding.kaleyraRec.visibility = View.VISIBLE
    }

    /**
     * Hide the recording icon and text
     */
    fun hideRec() {
        binding.kaleyraRec.visibility = View.GONE
    }

    /**
     * Set the centered title
     *
     * @param text The centered title text
     */
    fun setCenteredText(text: String) {
        binding.kaleyraCenteredTitle.text = text
    }

    /**
     * Show the centered title
     */
    fun showCenteredTitle() {
        binding.kaleyraCenteredTitle.visibility = View.VISIBLE
    }

    /**
     * Hide the centered title
     */
    fun hideCenteredTitle() {
        binding.kaleyraCenteredTitle.visibility = View.GONE
    }

    /**
     * Show the cam muted icon
     */
    fun showCamMutedIcon(isBlocked: Boolean = false) = with(binding.kaleyraCamMutedIcon) {
        visibility = View.VISIBLE
        isActivated = isBlocked
    }

    /**
     * Hide the cam muted icon
     */
    fun hideCamMutedIcon() {
        binding.kaleyraCamMutedIcon.visibility = View.GONE
    }

    /**
     * Show the mic muted icon
     */
    fun showMicMutedIcon(isBlocked: Boolean = false) = with(binding.kaleyraMicMutedIcon) {
        visibility = View.VISIBLE
        isActivated = isBlocked
    }

    /**
     * Hide the mic muted icon
     */
    fun hideMicMutedIcon() {
        binding.kaleyraMicMutedIcon.visibility = View.GONE
    }

    /**
     * Show the chat icon
     */
    fun showChatIcon() {
        binding.kaleyraChatIcon.visibility = View.VISIBLE
    }

    /**
     * Hide the chat icon
     */
    fun hideChatIcon() {
        binding.kaleyraChatIcon.visibility = View.GONE
    }


    /**
     * Set the battery charge level
     *
     * @param charge The level of the charge
     */
    fun setBatteryCharge(charge: Int) = binding.kaleyraBattery.setCharge(charge)

    /**
     * Set the
     *
     * @param isCharging Boolean
     */
    fun setBatteryChargingState(isCharging: Boolean) =
        binding.kaleyraBattery.setCharging(isCharging)

    /**
     * Show the WiFi signal icon
     */
    fun showWiFiSignalIcon() {
        binding.kaleyraWifiIcon.visibility = View.VISIBLE
    }

    /**
     * Hide the WiFi signal icon
     */
    fun hideWiFiSignalIcon() {
        binding.kaleyraWifiIcon.visibility = View.GONE
    }

    /**
     * Set the WiFi signal icon state
     *
     * @param state WiFiSignalState
     */
    fun setWiFiSignalState(state: WiFiSignalState) {
        binding.kaleyraWifiIcon.state = when (state) {
            WiFiSignalState.DISABLED -> WifiImageView.State.DISABLED
            WiFiSignalState.LOW -> WifiImageView.State.LOW
            WiFiSignalState.MODERATE -> WifiImageView.State.MODERATE
            WiFiSignalState.FULL -> WifiImageView.State.FULL
        }
    }

    /**
     * Set the timer text
     *
     * @param timestamp Timestamp expressed in seconds
     */
    fun setTimer(timestamp: Long) {
        binding.kaleyraTimer.text = TimerParser.parseTimestamp(timestamp)
    }

    /**
     * Show the timer text
     */
    fun showTimer() {
        binding.kaleyraTimer.visibility = View.VISIBLE
    }

    /**
     * Hide the timer text
     */
    fun hideTimer() {
        binding.kaleyraTimer.visibility = View.GONE
    }

    /**
     * Blink the timer a certain amount of times
     *
     * @param repeatCount The number of times to blink the timer. If set to -1, the timer blinks indefinitely
     */
    fun blinkTimer(repeatCount: Int) {
        binding.kaleyraTimer.blink(BLINK_DURATION, repeatCount)
    }

    private companion object {
        const val BLINK_DURATION = 500L // millis
    }
}