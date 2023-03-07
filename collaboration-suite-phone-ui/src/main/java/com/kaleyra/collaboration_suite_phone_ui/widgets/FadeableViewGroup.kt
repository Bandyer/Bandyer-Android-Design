/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *           
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.widgets

import android.animation.Animator
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import com.kaleyra.collaboration_suite_utils.FieldProperty
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.extensions.getViewIndex

/**
 * Defines a view group that is capable to fade in from black or fade out from black
 * @param T generic viewgroup type
 * @property fadingView View? the view that will fade in/out
 */
interface FadableViewGroup<T> where T : ViewGroup {

    /**
     * FabableViewGroup instance
     */
    companion object {
        /**
         * Fade in/out animation duration in millis
         */
        const val FADE_ANIMATION_DURATION = 300L
    }

    /**
     * Returns the view group's child id behind which the fade view will be placed
     * or View.NO_ID to let the fading view to be put above all view group's children
     * @return Int
     */
    fun getFadePivotViewId(): Int? = 0

    /**
     * Return the background drawable that will be used for the fading view
     * @return Drawable
     */
    fun getFadeBackground(): Drawable? = null

    /**
     * Fade out the fading view
     * @param endAnimationCallback Function0<Unit>? animation end callback
     */
    fun fadeOut(endAnimationCallback: (() -> Unit)? = null, durationMillis: Long? = FADE_ANIMATION_DURATION) =
            fade(this as ViewGroup, true, endAnimationCallback, durationMillis!!)

    /**
     * Fade in the fading view
     * @param endAnimationCallback Function0<Unit>? animation end callback
     */
    fun fadeIn(endAnimationCallback: (() -> Unit)? = null, durationMillis: Long? = FADE_ANIMATION_DURATION) =
            fade(this as ViewGroup, false, endAnimationCallback, durationMillis!!)

    private fun <T> fade(viewGroup: T, out: Boolean, endAnimationCallback: (() -> Unit)? = null, durationMillis: Long) where T : ViewGroup {
        if (fadingView == null)
            fadingView = View(viewGroup.context.applicationContext).apply {
                this.id = R.id.kaleyra_id_fading_view
                this.background = getFadeBackground()
                viewGroup.addView(
                        this,
                        viewGroup.getViewIndex(getFadePivotViewId()),
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }

        fadingView!!.clearAnimation()

        val finalAlpha = getFadeValue(out, false, viewGroup)

        if (fadingView!!.alpha == finalAlpha) {
            endAnimationCallback?.invoke()
            return
        }

        if (durationMillis == 0L) {
            fadingView!!.alpha = finalAlpha
            endAnimationCallback?.invoke()
            return
        } else
            fadingView!!.alpha = getFadeValue(out, true, viewGroup)

        fadingView!!.animate().alpha(finalAlpha).setDuration(durationMillis).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                endAnimationCallback?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
        }).start()
    }

    private fun getFadeValue(out: Boolean, startValue: Boolean, viewGroup: ViewGroup): Float {
        val isFadingViewGroup = fadingView!!.id == viewGroup.id

        when {
            out -> {
                return if (startValue) {
                    if (isFadingViewGroup) 1f
                    else 0f
                } else {
                    if (isFadingViewGroup) 0f
                    else 1f
                }
            }
            else -> {
                return if (startValue) {
                    if (isFadingViewGroup) 0f
                    else 1f
                } else {
                    if (isFadingViewGroup) 1f
                    else 0f
                }
            }
        }
    }
}

/**
 * Property specifying the fading view
 */
var <T> FadableViewGroup<T>.fadingView: View? where T : ViewGroup by FieldProperty { null }