package com.kaleyra.collaboration_suite_phone_ui.recording

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
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
        fun make(
            view: View,
            title: CharSequence,
            subtitle: CharSequence,
            @Duration duration: Int
        ): KaleyraSnackbar {
            val parent = findSuitableParent(view)
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val content = inflater.inflate(
                R.layout.kaleyra_snackbar_layout,
                parent,
                false
            ) as KaleyraSnackbarLayout
            return KaleyraSnackbar(parent, content, content).also {
                it.duration = duration
                it.setTitle(title)
                it.setSubTitle(subtitle)
                it.setBackgroundColor(ContextCompat.getColor(context, R.color.kaleyra_color_on_surface_80))
            }
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

    private val accessibilityManager = parent.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    override fun getDuration(): Int {
        val userSetDuration = super.getDuration()
        if (userSetDuration == LENGTH_INDEFINITE)
            return LENGTH_INDEFINITE

        if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            return accessibilityManager.getRecommendedTimeoutMillis(
                userSetDuration, AccessibilityManager.FLAG_CONTENT_ICONS or AccessibilityManager.FLAG_CONTENT_TEXT
            )
        }

        return userSetDuration
    }

    fun setTitle(text: CharSequence): KaleyraSnackbar {
        val contentLayout = view.getChildAt(0) as KaleyraSnackbarLayout
        contentLayout.title?.text = text
        return this
    }

    fun setSubTitle(text: CharSequence?): KaleyraSnackbar {
        val contentLayout = view.getChildAt(0) as KaleyraSnackbarLayout
        contentLayout.subTitle?.text = text
        contentLayout.subTitle?.visibility = if (text == null) View.GONE else View.VISIBLE
        return this
    }

    fun setIcon(@DrawableRes resId: Int): KaleyraSnackbar {
        val contentLayout = view.getChildAt(0) as KaleyraSnackbarLayout
        contentLayout.icon?.setImageResource(resId)
        return this
    }

    fun setTextColor(@ColorInt color: Int) {
        val contentLayout = view.getChildAt(0) as KaleyraSnackbarLayout
        contentLayout.title?.setTextColor(color)
        contentLayout.subTitle?.setTextColor(color)
    }

    fun setBackgroundColor(@ColorInt color: Int) {
        if (view.background == null) return
        val wrappedBackground = DrawableCompat.wrap(view.background.mutate())
        DrawableCompat.setTintList(wrappedBackground, ColorStateList.valueOf(color))
        DrawableCompat.setTintMode(wrappedBackground, PorterDuff.Mode.SRC_IN)
        if (wrappedBackground === view.background) return
        ViewCompat.setBackground(view, wrappedBackground)
    }

}