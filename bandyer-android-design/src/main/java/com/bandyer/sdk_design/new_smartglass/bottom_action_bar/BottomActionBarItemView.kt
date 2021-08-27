package com.bandyer.sdk_design.new_smartglass.bottom_action_bar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerBottomActionBarItemLayoutBinding

/*
 * Bottom action bar item view, it describes an action the user performs on a given gesture
 * It's made of:
 * - gesture icon
 * - gesture text
 * - action text
 */
class BottomActionBarItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerBottomActionBarItemLayoutBinding =
        BandyerBottomActionBarItemLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BottomActionBarItemView).apply {
            binding.bandyerGestureIcon.setImageDrawable(getDrawable(R.styleable.BottomActionBarItemView_bandyer_gestureIcon))
            binding.bandyerGestureText.text = getString(R.styleable.BottomActionBarItemView_bandyer_gestureText)
            setActionText(getString(R.styleable.BottomActionBarItemView_bandyer_actionText))
            recycle()
        }
    }

    /**
     * Set the action's text
     *
     * @param text The text to set
     */
    fun setActionText(text: String?) {
        binding.bandyerActionText.text = text
    }
}