package com.kaleyra.collaboration_suite_core_ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

internal object KaleyraFontFamily {
    val fontFamily = FontFamily(
        Font(R.font.kaleyra_font_regular, FontWeight.W400, FontStyle.Normal),
        Font(R.font.kaleyra_font_italic, FontWeight.W400, FontStyle.Italic),
        Font(R.font.kaleyra_font_bold, FontWeight(450), FontStyle.Normal)
    )
}