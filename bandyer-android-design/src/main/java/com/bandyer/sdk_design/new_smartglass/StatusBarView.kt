package com.bandyer.sdk_design.new_smartglass

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
}