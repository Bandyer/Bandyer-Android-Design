package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.graphics.Typeface
import android.text.TextUtils
import android.widget.TextView
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

enum class Ellipsize(val value: TextUtils.TruncateAt) {
    Start(TextUtils.TruncateAt.START),
    Middle(TextUtils.TruncateAt.MIDDLE),
    End(TextUtils.TruncateAt.END),
    Marquee(TextUtils.TruncateAt.MARQUEE)
}

// Replace this when compose Text will support overflow middle ellipsize
@Composable
internal fun EllipsizeText(
    text: String,
    color: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: TextUnit = 16.sp,
    ellipsize: Ellipsize,
    shadow: Shadow? = null,
    modifier: Modifier = Modifier
) {
    val fontFamily = LocalTextStyle.current.fontFamily

    AndroidView(
        factory = { context ->
            val tf = createFontFamilyResolver(context).resolve(
                fontFamily = fontFamily,
                fontWeight = fontWeight
            ).value as Typeface

            TextView(context).apply {
                maxLines = 1
                textSize = fontSize.value
                setTextColor(color.toArgb())
                typeface = tf
                this.ellipsize = ellipsize.value

                if (ellipsize == Ellipsize.Marquee) {
                    isSingleLine = true
                    isSelected = true
                    marqueeRepeatLimit = -1
                }

                if (shadow != null) {
                    setShadowLayer(shadow.blurRadius, shadow.offset.x, shadow.offset.y, shadow.color.toArgb())
                }
            }
        },
        update = { it.text = text },
        modifier = modifier.semantics { contentDescription = text }
    )
}