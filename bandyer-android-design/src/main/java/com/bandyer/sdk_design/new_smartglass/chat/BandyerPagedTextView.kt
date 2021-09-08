package com.bandyer.sdk_design.new_smartglass.chat

import android.content.Context
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView

/**
 * An utility TextView to compute how much pages a text should be divided to show it on screen
 */
class BandyerPagedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    /**
     * Compute the text for each page
     *
     * @return List<CharSequence>
     */
    fun paginate(): List<CharSequence> {
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