package com.bandyer.sdk_design.new_smartglass.status_bar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.databinding.BandyerStatusBarLayoutBinding

/**
 * A custom status bar view
 *
 * @constructor
 */
class StatusBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    enum class CellSignalState {
        MISSING,
        LOW,
        MODERATE,
        GOOD,
        FULL
    }

    enum class WiFiSignalState {
        DISABLED,
        LOW,
        MODERATE,
        FULL
    }

    private var binding: BandyerStatusBarLayoutBinding =
        BandyerStatusBarLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Show the title icon
     */
    fun showTitleIcon() {
        binding.bandyerTitleIcon.visibility = View.VISIBLE
    }

    /**
     * Hide the title icon
     */
    fun hideTitleIcon() {
        binding.bandyerTitleIcon.visibility = View.GONE
    }

    /**
     * Set the title
     *
     * @param text The text title
     */
    fun setTitleText(text: String?) {
        binding.bandyerTitle.text = text
    }

    /**
     * Show the title
     */
    fun showTitle() {
        binding.bandyerTitle.visibility = View.VISIBLE
    }

    /**
     * Hide the title
     */
    fun hideTitle() {
        binding.bandyerTitle.visibility = View.GONE
    }

    /**
     * Set the centered title
     *
     * @param text The centered title text
     */
    fun setCenteredText(text: String) {
        binding.bandyerCenteredTitle.text = text
    }

    /**
     * Show the centered title
     */
    fun showCenteredTitle() {
        binding.bandyerCenteredTitle.visibility = View.VISIBLE
    }

    /**
     * Hide the centered title
     */
    fun hideCenteredTitle() {
        binding.bandyerCenteredTitle.visibility = View.GONE
    }

    /**
     * Show the mic muted icon
     */
    fun showMicMutedIcon() {
        binding.bandyerMicMutedIcon.visibility = View.VISIBLE
    }

    /**
     * Hide the mic muted icon
     */
    fun hideMicMutedIcon() {
        binding.bandyerMicMutedIcon.visibility = View.GONE
    }

    /**
     * Set the battery charge level
     *
     * @param charge The level of the charge
     */
    fun setBatteryCharge(charge: Int) = binding.bandyerBattery.setCharge(charge)

    /**
     * Set the
     *
     * @param isCharging Boolean
     */
    fun setBatteryChargingState(isCharging: Boolean) =
        binding.bandyerBattery.setCharging(isCharging)

    /**
     * Show the WiFi signal icon
     */
    fun showWiFiSignalIcon() {
        binding.bandyerWifiIcon.visibility = View.VISIBLE
    }

    /**
     * Hide the WiFi signal icon
     */
    fun hideWiFiSignalIcon() {
        binding.bandyerWifiIcon.visibility = View.GONE
    }

    /**
     * Set the WiFi signal icon state
     *
     * @param state WiFiSignalState
     */
    fun setWiFiSignalState(state: WiFiSignalState) {
        binding.bandyerWifiIcon.state = when(state) {
            WiFiSignalState.DISABLED -> WifiImageView.State.DISABLED
            WiFiSignalState.LOW -> WifiImageView.State.LOW
            WiFiSignalState.MODERATE -> WifiImageView.State.MODERATE
            WiFiSignalState.FULL -> WifiImageView.State.FULL
        }
    }
}