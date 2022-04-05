/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes


import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getThemeAttribute
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassToastLayoutBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getCallThemeAttribute
import java.util.*

internal class ToastContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val theme = context.getThemeAttribute(
        context.getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Glass_Call_kaleyra_toastContainerStyle),
        R.styleable.KaleyraCollaborationSuiteUI_Theme_GlassCall_ToastContainerStyle,
        R.styleable.KaleyraCollaborationSuiteUI_Theme_GlassCall_ToastContainerStyle_kaleyra_toastStyle
    )

    /**
     * It shows a toast. The default toast duration is 3000 ms, if it is set to 0 the toast is showed until manually cancelled.
     *
     * @param id The toast id
     * @param text The toast's text
     * @param icon The toast's icon
     * @param duration The toast duration
     * @return String The id of the toast
     */
    fun show(
        id: String = UUID.randomUUID().toString(),
        text: String,
        @DrawableRes icon: Int? = null,
        duration: Long = 3000L
    ): String {
        cancel(id)
        Toast(ContextThemeWrapper(context, theme)).apply {
            tag = id
            setText(text)
            setIcon(icon)
            this@ToastContainer.addView(this, 0)

            if (duration == 0L) return@apply
            postDelayed({ this@ToastContainer.removeView(this) }, duration)
        }
        return id
    }

    /**
     * Cancel the toast with the given id
     *
     * @param id String
     */
    fun cancel(id: String) { findViewWithTag<Toast>(id)?.also { removeView(it) } }

    /**
     * Clear all the toasts
     */
    fun clear() = removeAllViews()

    private class Toast @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : LinearLayout(context, attrs, defStyleAttr)  {

        private val binding = KaleyraGlassToastLayoutBinding.inflate(LayoutInflater.from(context), this, true)

        fun setIcon(@DrawableRes resId: Int? = null) = with(binding.kaleyraIcon) {
            resId?.also { setImageResource(it) } ?: kotlin.run { visibility = View.GONE }
        }

        fun setText(text: String) { binding.kaleyraText.text = text }
    }
}