package com.bandyer.video_android_glass_ui.bottom_action_bar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
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
     * Set the swipe action text. If it is set to null,
     * the parent [BandyerBottomActionBarItemView]'s visibility is set to View.GONE
     *
     * @param text String?
     */
    fun setSwipeText(text: String?) = binding.bandyerSwipe.setActionTextOrHide(text)

    /**
     * Set the tap action text. If it is set to null,
     * the parent [BandyerBottomActionBarItemView]'s visibility is set to View.GONE
     *
     * @param text String?
     */
    fun setTapText(text: String?) = binding.bandyerTap.setActionTextOrHide(text)

    /**
     * Set the swipe down action text. If it is set to null,
     * the parent [BandyerBottomActionBarItemView]'s visibility is set to View.GONE
     *
     * @param text String?
     */
    fun setSwipeDownText(text: String?) = binding.bandyerSwipeDown.setActionTextOrHide(text)

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

    private fun BandyerBottomActionBarItemView.setActionTextOrHide(text: String?) =
        text?.let { setActionText(it) } ?: kotlin.run { visibility = View.GONE }
}