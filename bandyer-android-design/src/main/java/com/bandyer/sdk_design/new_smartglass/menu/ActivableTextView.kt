package com.bandyer.sdk_design.new_smartglass.menu

import android.content.Context
import android.util.AttributeSet
import com.bandyer.sdk_design.R
import com.google.android.material.textview.MaterialTextView

class ActivableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    var inactivatedText: String? = null
    var activatedText: String? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ActivableTextView).apply {
            activatedText = getString(R.styleable.ActivableTextView_bandyer_activatedText)
            inactivatedText = getString(R.styleable.ActivableTextView_bandyer_inactivatedText)
            recycle()
        }
    }

    override fun setActivated(activated: Boolean) {
        text = if(activated) activatedText else inactivatedText
        super.setActivated(activated)
    }
}