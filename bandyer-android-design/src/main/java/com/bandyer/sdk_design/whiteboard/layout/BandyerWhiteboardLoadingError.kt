package com.bandyer.sdk_design.whiteboard.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerWhiteboardLoadingErrorBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

/**
 * A view showing an error occurred while loading the whiteboard web view
 * @property reloadTitle MaterialTextView?
 * @property reloadSubtitle MaterialTextView?
 * @property reloadButton MaterialButton?
 * @constructor
 */
class BandyerWhiteboardLoadingError @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_rootLayoutStyle) : ConstraintLayout(context, attrs, defStyleAttr) {

    /**
     * Title of the error layout
     */
    var reloadTitle: MaterialTextView? = null
        private set

    /**
     * Subtitle of the error layout
     */
    var reloadSubtitle: MaterialTextView? = null
        private set

    /**
     * The reload button of the error layout
     */
    var reloadButton: MaterialButton? = null
        private set

    private val binding: BandyerWhiteboardLoadingErrorBinding by lazy { BandyerWhiteboardLoadingErrorBinding.inflate(LayoutInflater.from(context), this) }

    /**
     * Animation applied to reload button
     */
    private val reloadAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.bandyer_reload_animation)

    init {
        reloadTitle = binding.bandyerReloadTitle
        reloadSubtitle = binding.bandyerReloadSubtitle
        reloadButton = binding.bandyerReloadButton
    }

    /**
     * Set a click listener on the reload button and animate it on click
     * @param callback the callback function to be executed when clicking the button
     */
    fun onReload(callback: () -> Unit) =
            reloadButton?.setOnClickListener {
                callback.invoke()
                reloadButton?.startAnimation(reloadAnimation)
            }

}
