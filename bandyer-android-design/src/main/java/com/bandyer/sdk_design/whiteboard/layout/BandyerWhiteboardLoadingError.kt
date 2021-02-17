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


class BandyerWhiteboardLoadingError @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_rootLayoutStyle) : ConstraintLayout(context, attrs, defStyleAttr) {

    var reloadTitle: MaterialTextView? = null
        private set

    var reloadSubtitle: MaterialTextView? = null
        private set

    var reloadButton: MaterialButton? = null
        private set

    private val binding: BandyerWhiteboardLoadingErrorBinding by lazy { BandyerWhiteboardLoadingErrorBinding.inflate(LayoutInflater.from(context), this) }

    private val reloadAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.bandyer_reload_animation)

    init {
        reloadTitle = binding.bandyerReloadTitle
        reloadSubtitle = binding.bandyerReloadSubtitle
        reloadButton = binding.bandyerReloadButton
    }

    fun onReload(callback: () -> Unit) =
            reloadButton?.setOnClickListener {
                callback.invoke()
                it.startAnimation(reloadAnimation)
            }

}
