package com.bandyer.video_android_glass_ui.bottom_action_bar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bandyer.video_android_glass_ui.databinding.BandyerBottomActionBarItemLayoutBinding

/*
 * Bottom action bar item view, it describes an action the user performs on a given gesture
 * It's made of:
 * - gesture icon
 * - gesture text
 * - action text
 */
class BandyerBottomActionBarItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val binding = BandyerBottomActionBarItemLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }
}