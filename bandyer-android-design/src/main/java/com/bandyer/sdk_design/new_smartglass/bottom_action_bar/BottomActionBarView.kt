package com.bandyer.sdk_design.new_smartglass.bottom_action_bar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerBottomActionBarLayoutBinding

class BottomActionBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerBottomActionBarLayoutBinding =
        BandyerBottomActionBarLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BottomActionBarView).apply {
            setSwipeText(getString(R.styleable.BottomActionBarView_swipeText))
            setTapText(getString(R.styleable.BottomActionBarView_tapText))
            setSwipeDownText(getString(R.styleable.BottomActionBarView_swipeDownText))
            recycle()
        }
    }

    fun setSwipeText(text: String?) = binding.swipe.setActionTextOrHide(text)

    fun setTapText(text: String?) = binding.tap.setActionTextOrHide(text)

    fun setSwipeDownText(text: String?) = binding.swipeDown.setActionTextOrHide(text)

    private fun BottomActionBarItemView.setActionTextOrHide(text: String?) =
        text?.let { setActionText(it) } ?: kotlin.run { visibility = View.GONE }
}