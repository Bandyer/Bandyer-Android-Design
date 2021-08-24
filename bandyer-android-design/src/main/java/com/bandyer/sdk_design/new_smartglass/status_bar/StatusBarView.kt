package com.bandyer.sdk_design.new_smartglass.status_bar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.databinding.BandyerStatusBarLayoutBinding

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

    fun setTitleText(text: String?) {
        binding.bandyerTitle.text = text
    }

    fun showMicMutedIcon() {
        binding.bandyerMicMutedIcon.visibility = View.VISIBLE
    }

    fun hideMicMutedIcon() {
        binding.bandyerMicMutedIcon.visibility = View.GONE
    }

    fun setBatteryCharge(charge: Int) = binding.bandyerBattery.setCharge(charge)

    fun setBatteryChargingState(isCharging: Boolean) =
        binding.bandyerBattery.setCharging(isCharging)

    fun showCellSignalIcon() {
        binding.bandyerCellIcon.visibility = View.VISIBLE
    }

    fun hideCellSignalIcon() {
        binding.bandyerCellIcon.visibility = View.GONE
    }

    fun setCellSignalState(state: CellSignalState) {
        binding.bandyerCellIcon.state = when(state) {
            CellSignalState.MISSING -> CellImageView.State.MISSING
            CellSignalState.LOW -> CellImageView.State.LOW
            CellSignalState.MODERATE -> CellImageView.State.MODERATE
            CellSignalState.GOOD -> CellImageView.State.GOOD
            CellSignalState.FULL -> CellImageView.State.FULL
        }
    }

    fun showWiFiSignalIcon() {
        binding.bandyerWifiIcon.visibility = View.VISIBLE
    }

    fun hideWiFiSignalIcon() {
        binding.bandyerWifiIcon.visibility = View.GONE
    }

    fun setWiFiSignalState(state: WiFiSignalState) {
        binding.bandyerWifiIcon.state = when(state) {
            WiFiSignalState.DISABLED -> WifiImageView.State.DISABLED
            WiFiSignalState.LOW -> WifiImageView.State.LOW
            WiFiSignalState.MODERATE -> WifiImageView.State.MODERATE
            WiFiSignalState.FULL -> WifiImageView.State.FULL
        }
    }
}