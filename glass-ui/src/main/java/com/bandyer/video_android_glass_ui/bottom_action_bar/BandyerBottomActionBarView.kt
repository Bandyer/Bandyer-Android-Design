package com.bandyer.video_android_glass_ui.bottom_action_bar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bandyer.video_android_glass_ui.databinding.BandyerBottomActionBarLayoutBinding

/*
 * Bottom action bar view, it describes the actions the user performs
 * It is made of three inline [BottomActionBarItemView]
 */
class BandyerBottomActionBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerBottomActionBarLayoutBinding =
        BandyerBottomActionBarLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Set an on click listener on the swipe element. Needed for realwear glasses.
     *
     * @param callback function
     */
    fun setSwipeOnClickListener(callback: () -> Unit) =
        binding.bandyerSwipe.setOnClickListener {
            callback.invoke()
        }

    /**
     * Set an on click listener on the tap element. Needed for realwear glasses.
     *
     * @param callback function
     */
    fun setTapOnClickListener(callback: () -> Unit) =
        binding.bandyerTap.setOnClickListener {
            callback.invoke()
        }

    /**
     * Set an on click listener on the swipe down element. Needed for realwear glasses.
     *
     * @param callback function
     */
    fun setSwipeDownOnClickListener(callback: () -> Unit) =
        binding.bandyerSwipeDown.setOnClickListener {
            callback.invoke()
        }
}