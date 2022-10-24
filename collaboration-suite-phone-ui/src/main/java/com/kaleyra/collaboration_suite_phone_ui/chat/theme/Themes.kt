package com.kaleyra.collaboration_suite_phone_ui.chat.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KaleyraDarkColorTheme = darkColors(
    primary = Color(0xFF303030),
    primaryVariant = Color(0xFF1E1E1E),
    secondary = Color(0xFF9E000A),
    secondaryVariant = Color.White,
    background = Color(0xFF0E0E0E),
    surface = Color(0xFF242424),
    error = Color(0xFFC70000),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

// Compiler unhappy because onBackground is not Color.Black, like onPrimary and onSurface.
// The compiler want it Color.Black because primary, surface and background colors are all Color.White.
@SuppressLint("ConflictingOnColor")
private val KaleyraLightColorTheme = lightColors(
    primary = Color.White,
    primaryVariant = Color(0xFFEFEFEF),
    secondary = Color(0xFFD80D30),
    secondaryVariant = Color(0xFF6C6C6C),
    background = Color.White,
    surface = Color.White,
    error = Color(0xFFC70000),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFF0E0E0E),
    onSurface = Color.Black,
    onError = Color.White
)

// Theme currently used only for previews.
@Composable
internal fun KaleyraTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when {
        isDarkTheme -> KaleyraDarkColorTheme
        else -> KaleyraLightColorTheme
    }

    MaterialTheme(
        colors = colors,
        typography = kaleyraTypography,
        content = content
    )
}