package com.kaleyra.collaboration_suite_phone_ui.recording

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
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
        val parentView = parent as View
        parentView.setPadding(0, 0, 0, 0)
    }

    override fun animateContentIn(delay: Int, duration: Int) {
        setOf(title, subTitle, icon).forEach { viewToAnimate ->
            viewToAnimate ?: return@forEach
            if (viewToAnimate.visibility == View.VISIBLE) {
                viewToAnimate.alpha = 0f
                ViewCompat.animate(viewToAnimate).alpha(1f).setDuration(duration.toLong())
                    .setStartDelay(delay.toLong()).start()
            }
        }
    }

    override fun animateContentOut(delay: Int, duration: Int) {
        setOf(title, subTitle, icon).forEach { viewToAnimate ->
            viewToAnimate ?: return@forEach
            if (viewToAnimate.visibility == View.VISIBLE) {
                viewToAnimate.alpha = 1f
                ViewCompat.animate(viewToAnimate).alpha(0f).setDuration(duration.toLong())
                    .setStartDelay(delay.toLong()).start()
            }
        }
    }
}