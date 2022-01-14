package com.bandyer.video_android_glass_ui.status_bar_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassStatusBarLayoutBinding

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

    private var binding: BandyerGlassStatusBarLayoutBinding = BandyerGlassStatusBarLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        hideCamMutedIcon()
        hideMicMutedIcon()
        hideChatIcon()
    }

    /**
     * Hide the state bar
     */
    fun hide() {
        visibility = View.GONE
    }

    /**
     * Show the state bar
     */
    fun show() {
        visibility = View.VISIBLE
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
     * Show the recording icon and text
     */
    fun showRec() {
        binding.bandyerRec.visibility = View.VISIBLE
    }

    /**
     * Hide the recording icon and text
     */
    fun hideRec() {
        binding.bandyerRec.visibility = View.GONE
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
     * Show the cam muted icon
     */
    fun showCamMutedIcon(isBlocked: Boolean = false) = with(binding.bandyerCamMutedIcon) {
        visibility = View.VISIBLE
        isActivated = isBlocked
    }

    /**
     * Hide the cam muted icon
     */
    fun hideCamMutedIcon() {
        binding.bandyerCamMutedIcon.visibility = View.GONE
    }

    /**
     * Show the mic muted icon
     */
    fun showMicMutedIcon(isBlocked: Boolean = false) = with(binding.bandyerMicMutedIcon) {
        visibility = View.VISIBLE
        isActivated = isBlocked
    }

    /**
     * Hide the mic muted icon
     */
    fun hideMicMutedIcon() {
        binding.bandyerMicMutedIcon.visibility = View.GONE
    }

    /**
     * Show the chat icon
     */
    fun showChatIcon() {
        binding.bandyerChatIcon.visibility = View.VISIBLE
    }

    /**
     * Hide the chat icon
     */
    fun hideChatIcon() {
        binding.bandyerChatIcon.visibility = View.GONE
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
            WiFiSignalState.LOW      -> WifiImageView.State.LOW
            WiFiSignalState.MODERATE -> WifiImageView.State.MODERATE
            WiFiSignalState.FULL     -> WifiImageView.State.FULL
        }
    }
}