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

package com.kaleyra.collaboration_suite_glass_ui.utils

import android.content.Context
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import com.google.android.material.textview.MaterialTextView

/**
 * An utility TextView to compute how much pages a text need to be divided in
 */
internal class PaginatedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    /**
     * Compute the text for each page, the view needs to be laid out before executing this method
     *
     * @return List<CharSequence>
     */
    fun paginate(): List<CharSequence> {
        if (!ViewCompat.isLaidOut(this)) return listOf()
        val pageList = arrayListOf<CharSequence>()

        val layout = from(layout)
        val lines = layout.lineCount
        var startOffset = 0
        var height = height
        val heightWithoutPaddings = height - paddingTop - paddingBottom

        for (i in 0 until lines) {
            if (height < layout.getLineBottom(i)) {
                pageList.add(layout.text.subSequence(startOffset, layout.getLineStart(i)))
                startOffset = layout.getLineStart(i)
                height = layout.getLineTop(i) + heightWithoutPaddings
            }
            if (i == lines - 1)
                pageList.add(layout.text.subSequence(startOffset, layout.getLineEnd(i)))
        }
        return pageList
    }

    private fun from(layout: Layout): Layout =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                layout.width,
                layout.alignment,
                lineSpacingMultiplier,
                lineSpacingExtra,
                includeFontPadding
            )
        } else {
            StaticLayout.Builder
                .obtain(text, 0, text.length, paint, layout.width)
                .setAlignment(layout.alignment)
                .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                .setIncludePad(includeFontPadding)
                .setUseLineSpacingFromFallbacks()
                .setBreakStrategy(breakStrategy)
                .setHyphenationFrequency(hyphenationFrequency)
                .build()
        }

    private fun StaticLayout.Builder.setUseLineSpacingFromFallbacks(): StaticLayout.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.setUseLineSpacingFromFallbacks(isFallbackLineSpacing)
        }

        return this
    }

    private fun StaticLayout.Builder.setJustificationMode(): StaticLayout.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.setJustificationMode(justificationMode)
        }

        return this
    }
}