package com.kaleyra.collaboration_suite_phone_ui.chat.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.kaleyra.collaboration_suite_phone_ui.R

// TODO move in a common package for call and chat
private val fontFamily = FontFamily(
    Font(R.font.kaleyra_font_regular, FontWeight.W400, FontStyle.Normal),
    Font(R.font.kaleyra_font_italic, FontWeight.W400, FontStyle.Italic),
    Font(R.font.kaleyra_font_bold, FontWeight(450), FontStyle.Normal)
)

// Typography currently used only for previews.
internal val kaleyraTypography = Typography(defaultFontFamily = fontFamily)
