package com.bandyer.video_android_glass_ui.bottom_navigation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassBottomNavigationItemLayoutBinding

/**
 * Bottom action bar item view, it describes an action the user performs on a given gesture
 * It's made of:
 * - gesture icon
 * - gesture text
 * - action text
 *
 * @constructor
 */
internal class BottomNavigationItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = BandyerGlassBottomNavigationItemLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }
}