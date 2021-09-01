package com.bandyer.sdk_design.new_smartglass.bottom_action_bar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerBottomActionBarLayoutBinding

/*
 * Bottom action bar view, it describes the actions the user performs
 * It is made of three inline [BottomActionBarItemView]
 */
class BottomActionBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerBottomActionBarLayoutBinding =
        BandyerBottomActionBarLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BottomActionBarView).apply {
            setSwipeText(getString(R.styleable.BottomActionBarView_bandyer_swipeText))
            setTapText(getString(R.styleable.BottomActionBarView_bandyer_tapText))
            setSwipeDownText(getString(R.styleable.BottomActionBarView_bandyer_swipeDownText))
            recycle()
        }
    }

    /**
     * Set the swipe action text. If it is set to null,
     * the parent [BottomActionBarItemView]'s visibility is set to View.GONE
     *
     * @param text String?
     */
    fun setSwipeText(text: String?) = binding.bandyerSwipe.setActionTextOrHide(text)

    /**
     * Set the tap action text. If it is set to null,
     * the parent [BottomActionBarItemView]'s visibility is set to View.GONE
     *
     * @param text String?
     */
    fun setTapText(text: String?) = binding.bandyerTap.setActionTextOrHide(text)

    /**
     * Set the swipe down action text. If it is set to null,
     * the parent [BottomActionBarItemView]'s visibility is set to View.GONE
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

    private fun BottomActionBarItemView.setActionTextOrHide(text: String?) =
        text?.let { setActionText(it) } ?: kotlin.run { visibility = View.GONE }
}