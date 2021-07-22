package com.bandyer.sdk_design.new_smartglass.bottom_action_bar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerBottomActionBarItemLayoutBinding

class BottomActionBarItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerBottomActionBarItemLayoutBinding =
        BandyerBottomActionBarItemLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BottomActionBarItemView).apply {
            binding.kaleyraGestureIcon.setImageDrawable(getDrawable(R.styleable.BottomActionBarItemView_kaleyra_gestureIcon))
            binding.kaleyraGestureText.text = getString(R.styleable.BottomActionBarItemView_kaleyra_gestureText)
            setActionText(getString(R.styleable.BottomActionBarItemView_kaleyra_actionText))
            recycle()
        }
    }

    fun setActionText(text: String?) {
        binding.kaleyraActionText.text = text
    }
}