package com.kaleyra.collaboration_suite_phone_ui.chat.compose.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.kaleyra.collaboration_suite_phone_ui.R

private val fontFamily = FontFamily(
    fonts = listOf(
        Font(
            resId = R.font.kaleyra_font_regular,
            weight = FontWeight.W400,
            style = FontStyle.Normal
        ),
        Font(
            resId = R.font.kaleyra_font_italic,
            weight = FontWeight.W400,
            style = FontStyle.Italic
        ),
        Font(
            resId = R.font.kaleyra_font_bold,
            weight = FontWeight(450),
            style = FontStyle.Normal
        )
    )
)

// Typography currently used only for previews.
val kaleyraTypography = Typography(defaultFontFamily = fontFamily)