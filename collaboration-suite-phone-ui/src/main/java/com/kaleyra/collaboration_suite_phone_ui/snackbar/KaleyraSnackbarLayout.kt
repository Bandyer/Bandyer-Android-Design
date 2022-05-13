package com.kaleyra.collaboration_suite_phone_ui.snackbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.children
import com.google.android.material.snackbar.ContentViewCallback
import com.google.android.material.textview.MaterialTextView
import com.kaleyra.collaboration_suite_phone_ui.R

class KaleyraSnackbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes), ContentViewCallback {

    var title: MaterialTextView? = null
        private set
    var subTitle: MaterialTextView? = null
        private set
    var icon: AppCompatImageView? = null
        private set

    override fun onFinishInflate() {
        super.onFinishInflate()
        title = findViewById(R.id.kaleyra_title)
        subTitle = findViewById(R.id.kaleyra_subtitle)
        icon = findViewById(R.id.kaleyra_icon)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as View).setPadding(0, 0, 0, 0)
    }

    override fun animateContentIn(delay: Int, duration: Int) {
        children.forEach { child ->
            if (child.visibility != View.VISIBLE) return@forEach
            child.alpha = 0f
            ViewCompat
                .animate(child)
                .alpha(1f)
                .setDuration(duration.toLong())
                .setStartDelay(delay.toLong()).start()
        }
    }

    override fun animateContentOut(delay: Int, duration: Int) {
        children.forEach { child ->
            if (child.visibility != View.VISIBLE) return@forEach
            child.alpha = 1f
            ViewCompat
                .animate(child)
                .alpha(0f)
                .setDuration(duration.toLong())
                .setStartDelay(delay.toLong()).start()
        }
    }
}