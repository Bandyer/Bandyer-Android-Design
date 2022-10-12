package com.kaleyra.collaboration_suite_phone_ui.snackbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.accessibility.AccessibilityManager
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.utils.SwipeDismissBehavior2


/**
 * Kaleyra snackbar
 */
class KaleyraSnackbar private constructor(
    parent: ViewGroup,
    content: View,
    contentViewCallback: com.google.android.material.snackbar.ContentViewCallback
) : BaseTransientBottomBar<KaleyraSnackbar>(parent, content, contentViewCallback) {

    /**
     * Duration
     */
    @IntDef(LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG)
    @IntRange(from = 1)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Duration

    /**
     * Companion
     * @suppress
     */
    internal companion object {

        @JvmStatic
        @JvmSynthetic
        fun make(
            view: View,
            title: CharSequence,
            subtitle: CharSequence,
            @Duration duration: Int
        ): KaleyraSnackbar = makeInternal(view, title, subtitle, duration)

        @JvmStatic
        @JvmSynthetic
        fun make(
            view: View,
            @StringRes title: Int,
            @StringRes subtitle: Int,
            @Duration duration: Int
        ): KaleyraSnackbar = makeInternal(
            view,
            view.resources.getText(title),
            view.resources.getText(subtitle),
            duration
        )

        @JvmStatic
        @JvmSynthetic
        fun make(
            view: View,
            @Duration duration: Int
        ): KaleyraSnackbar = makeInternal(
            view,
            null,
            null,
            duration
        )

        private fun makeInternal(
            view: View,
            title: CharSequence?,
            subtitle: CharSequence?,
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

    private val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.kaleyra_snackbar_background)
    private val backgroundTint = ContextCompat.getColor(context, R.color.kaleyra_color_on_surface_80)
    private val accessibilityManager =
        parent.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    init {
        (view as View).background = backgroundDrawable
        setBackgroundColor(backgroundTint)
    }

    /**
     * @suppress
     */
    override fun show() {
        addCallback(object : BaseTransientBottomBar.BaseCallback<KaleyraSnackbar>() {
            override fun onShown(transientBottomBar: KaleyraSnackbar?) {
                super.onShown(transientBottomBar)
                removeCallback(this)
                val params: LayoutParams = getView().layoutParams
                if (params !is CoordinatorLayout.LayoutParams) return
                val behavior = SwipeDismissBehavior2<View>()
                behavior.setSwipeDirection(SwipeDismissBehavior2.SWIPE_DIRECTION_ANY)
                params.behavior = behavior
            }
        })
        super.show()
    }

    /**
     * @suppress
     */
    override fun getDuration(): Int {
        val userSetDuration = super.getDuration()
        if (userSetDuration == LENGTH_INDEFINITE)
            return LENGTH_INDEFINITE

        if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            return accessibilityManager.getRecommendedTimeoutMillis(
                userSetDuration,
                AccessibilityManager.FLAG_CONTENT_ICONS or AccessibilityManager.FLAG_CONTENT_TEXT
            )
        }

        return userSetDuration
    }

    @JvmSynthetic
    internal fun setTitle(text: CharSequence?): KaleyraSnackbar {
        val contentLayout = view.getChildAt(0) as KaleyraSnackbarLayout
        contentLayout.title?.text = text
        contentLayout.title?.visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
        return this
    }

    @JvmSynthetic
    internal fun setSubTitle(text: CharSequence?): KaleyraSnackbar {
        val contentLayout = view.getChildAt(0) as KaleyraSnackbarLayout
        contentLayout.subTitle?.text = text
        contentLayout.subTitle?.visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
        return this
    }

    @JvmSynthetic
    internal fun setIcon(@DrawableRes resId: Int): KaleyraSnackbar {
        val contentLayout = view.getChildAt(0) as KaleyraSnackbarLayout
        contentLayout.icon?.setImageResource(resId)
        return this
    }

    @JvmSynthetic
    internal fun setIconColor(@ColorInt color: Int): KaleyraSnackbar {
        val contentLayout = view.getChildAt(0) as KaleyraSnackbarLayout
        contentLayout.icon?.setColorFilter(color)
        return this
    }

    @JvmSynthetic
    internal fun setTextColor(@ColorInt color: Int): KaleyraSnackbar {
        val contentLayout = view.getChildAt(0) as KaleyraSnackbarLayout
        contentLayout.title?.setTextColor(color)
        contentLayout.subTitle?.setTextColor(color)
        return this
    }

    @JvmSynthetic
    internal fun setBackgroundColor(@ColorInt color: Int): KaleyraSnackbar {
        if (view.background != null) {
            val wrappedBackground = DrawableCompat.wrap(view.background.mutate())
            DrawableCompat.setTintList(wrappedBackground, ColorStateList.valueOf(color))
            DrawableCompat.setTintMode(wrappedBackground, PorterDuff.Mode.SRC_IN)
            if (wrappedBackground !== view.background) {
                ViewCompat.setBackground(view, wrappedBackground)
            }
        }
        return this
    }
}