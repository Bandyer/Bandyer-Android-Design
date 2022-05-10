package com.kaleyra.collaboration_suite_phone_ui.recording

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.kaleyra.collaboration_suite_phone_ui.R

class KaleyraSnackbar private constructor(
    parent: ViewGroup,
    content: View,
    contentViewCallback: com.google.android.material.snackbar.ContentViewCallback
) : BaseTransientBottomBar<KaleyraSnackbar>(parent, content, contentViewCallback) {

    @IntDef(LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG)
    @IntRange(from = 1)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Duration

    companion object {
        fun make(view: View, text: CharSequence, @Duration duration: Int): KaleyraSnackbar {
            val parent = findSuitableParent(view)
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val content = inflater.inflate(
                R.layout.kaleyra_snackbar_layout,
                parent,
                false
            ) as KaleyraSnackbarLayout
            val snackbar = KaleyraSnackbar(parent, content, content)
            snackbar.duration = duration
            return snackbar
        }

        private fun findSuitableParent(providedView: View): ViewGroup {
            var view: View? = providedView
            var fallback: ViewGroup? = null
            do {
                if (view is CoordinatorLayout) {
                    // Use the CoordinatorLayout found
                    return view
                } else if (view is FrameLayout) {
                    if (view.id == android.R.id.content) {
                        // Use the content view since CoordinatorLayout not found
                        return view
                    } else {
                        // Non-content view, but use it as fallback
                        fallback = view
                    }
                }
                if (view != null) {
                    // Loop and crawl up the view hierarchy and try to find a parent
                    val parent = view.parent
                    view = if (parent is View) parent else null
                }
            } while (view != null)
            // Use fallback since CoordinatorLayout and other alternative not found
            fallback?.run { return this }
                ?: throw IllegalArgumentException("No suitable parent found from the given view. " + "Please provide a valid view.")
        }
    }
}