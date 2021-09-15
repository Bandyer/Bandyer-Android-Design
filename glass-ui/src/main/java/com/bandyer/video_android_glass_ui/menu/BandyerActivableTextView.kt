package com.bandyer.video_android_glass_ui.menu

import android.content.Context
import android.util.AttributeSet
import com.bandyer.video_android_glass_ui.R
import com.google.android.material.textview.MaterialTextView

/**
 * A textview which has two texts, one when is activated, the other when is not
 *
 * @constructor
 */
class BandyerActivableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    var defaultText: String? = null
    var activeText: String? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ActivableTextView).apply {
            activeText = getString(R.styleable.ActivableTextView_bandyer_activatedText)
            defaultText = getString(R.styleable.ActivableTextView_bandyer_inactivatedText)
            recycle()
        }
    }

    /**
     * Set the isActivated state. The text will update accordingly.
     *
     * @param activated True to set the activated state, false otherwise
     */
    override fun setActivated(activated: Boolean) {
        text = if(activated) activeText else defaultText
        super.setActivated(activated)
    }
}