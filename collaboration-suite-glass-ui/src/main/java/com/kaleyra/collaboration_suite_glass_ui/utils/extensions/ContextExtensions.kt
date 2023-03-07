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

package com.kaleyra.collaboration_suite_glass_ui.utils.extensions

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleableRes
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getThemeAttribute
import com.kaleyra.collaboration_suite_glass_ui.R

/**
 * Context utility class
 */
internal object ContextExtensions {

    /**
     * Retrieve a call theme attribute's style
     *
     * @receiver Context
     * @param styleAttribute the attribute for which you want to retrieve the style
     * @return Int the attribute's style
     */
    internal fun Context.getCallThemeAttribute(
        @StyleableRes styleAttribute: Int
    ): Int =
        this.getThemeAttribute(
            R.style.KaleyraCollaborationSuiteUI_Theme_GlassCall,
            R.styleable.KaleyraCollaborationSuiteUI_Theme_Glass_Call,
            styleAttribute
        )

    /**
     * Retrieve a chat theme attribute's style
     *
     * @receiver Context
     * @param styleAttribute the attribute for which you want to retrieve the style
     * @return Int the attribute's style
     */
    internal fun Context.getChatThemeAttribute(
        @StyleableRes styleAttribute: Int
    ): Int =
        this.getThemeAttribute(
            R.style.KaleyraCollaborationSuiteUI_Theme_GlassChat,
            R.styleable.KaleyraCollaborationSuiteUI_Theme_Glass_Chat,
            styleAttribute
        )

    /**
     * Retrieve a theme attribute value's resource id
     *
     * @receiver Resources.Theme
     * @param attr Int the theme attribute
     * @return Int the resource id
     */
    internal fun Resources.Theme.getAttributeResourceId(
        @AttrRes attr: Int
    ): Int =
        TypedValue()
            .also {
                resolveAttribute(attr, it, true)
            }.resourceId

    internal fun Context.tiltScrollFactor() = resources.displayMetrics.densityDpi / 8f
}