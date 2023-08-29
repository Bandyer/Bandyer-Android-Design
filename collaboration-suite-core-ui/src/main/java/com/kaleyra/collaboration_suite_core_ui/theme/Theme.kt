package com.kaleyra.collaboration_suite_core_ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import com.google.android.material.color.MaterialColors
import com.kaleyra.collaboration_suite_core_ui.KaleyraFontFamily
import com.kaleyra.collaboration_suite_core_ui.Theme

private val KaleyraLightColorTheme = lightColors(
    primary = kaleyra_theme_light_primary,
    primaryVariant = kaleyra_theme_light_primaryVariant,
    onPrimary = kaleyra_theme_light_onPrimary,
    secondary = kaleyra_theme_light_secondary,
    secondaryVariant = kaleyra_theme_light_secondaryVariant,
    onSecondary = kaleyra_theme_light_onSecondary,
    error = kaleyra_theme_light_error,
    onError = kaleyra_theme_light_onError,
    background = kaleyra_theme_light_background,
    onBackground = kaleyra_theme_light_onBackground,
    surface = kaleyra_theme_light_surface,
    onSurface = kaleyra_theme_light_onSurface
)

private val KaleyraDarkColorTheme = darkColors(
    primary = kaleyra_theme_dark_primary,
    primaryVariant = kaleyra_theme_dark_primaryVariant,
    onPrimary = kaleyra_theme_dark_onPrimary,
    secondary = kaleyra_theme_dark_secondary,
    secondaryVariant = kaleyra_theme_dark_secondaryVariant,
    onSecondary = kaleyra_theme_dark_onSecondary,
    error = kaleyra_theme_dark_error,
    onError = kaleyra_theme_dark_onError,
    background = kaleyra_theme_dark_background,
    onBackground = kaleyra_theme_dark_onBackground,
    surface = kaleyra_theme_dark_surface,
    onSurface = kaleyra_theme_dark_onSurface
)

// DE-COMMENT THIS WHEN THE MATERIAL 3 DEPENDENCY WILL BE ADDED
//private val KaleyraM3LightColorTheme = lightColorScheme(
//    primary = kaleyra_m3_theme_light_primary,
//    onPrimary = kaleyra_m3_theme_light_onPrimary,
//    primaryContainer = kaleyra_m3_theme_light_primaryContainer,
//    onPrimaryContainer = kaleyra_m3_theme_light_onPrimaryContainer,
//    secondary = kaleyra_m3_theme_light_secondary,
//    onSecondary = kaleyra_m3_theme_light_onSecondary,
//    secondaryContainer = kaleyra_m3_theme_light_secondaryContainer,
//    onSecondaryContainer = kaleyra_m3_theme_light_onSecondaryContainer,
//    tertiary = kaleyra_m3_theme_light_tertiary,
//    onTertiary = kaleyra_m3_theme_light_onTertiary,
//    tertiaryContainer = kaleyra_m3_theme_light_tertiaryContainer,
//    onTertiaryContainer = kaleyra_m3_theme_light_onTertiaryContainer,
//    error = kaleyra_m3_theme_light_error,
//    errorContainer = kaleyra_m3_theme_light_errorContainer,
//    onError = kaleyra_m3_theme_light_onError,
//    onErrorContainer = kaleyra_m3_theme_light_onErrorContainer,
//    background = kaleyra_m3_theme_light_background,
//    onBackground = kaleyra_m3_theme_light_onBackground,
//    surface = kaleyra_m3_theme_light_surface,
//    onSurface = kaleyra_m3_theme_light_onSurface,
//    surfaceVariant = kaleyra_m3_theme_light_surfaceVariant,
//    onSurfaceVariant = kaleyra_m3_theme_light_onSurfaceVariant,
//    outline = kaleyra_m3_theme_light_outline,
//    inverseOnSurface = kaleyra_m3_theme_light_inverseOnSurface,
//    inverseSurface = kaleyra_m3_theme_light_inverseSurface,
//    inversePrimary = kaleyra_m3_theme_light_inversePrimary,
//    surfaceTint = kaleyra_m3_theme_light_surfaceTint,
//    outlineVariant = kaleyra_m3_theme_light_outlineVariant,
//    scrim = kaleyra_m3_theme_light_scrim,
//)
//
//
//private val KaleyraM3DarkColorTheme = darkColorScheme(
//    primary = kaleyra_m3_theme_dark_primary,
//    onPrimary = kaleyra_m3_theme_dark_onPrimary,
//    primaryContainer = kaleyra_m3_theme_dark_primaryContainer,
//    onPrimaryContainer = kaleyra_m3_theme_dark_onPrimaryContainer,
//    secondary = kaleyra_m3_theme_dark_secondary,
//    onSecondary = kaleyra_m3_theme_dark_onSecondary,
//    secondaryContainer = kaleyra_m3_theme_dark_secondaryContainer,
//    onSecondaryContainer = kaleyra_m3_theme_dark_onSecondaryContainer,
//    tertiary = kaleyra_m3_theme_dark_tertiary,
//    onTertiary = kaleyra_m3_theme_dark_onTertiary,
//    tertiaryContainer = kaleyra_m3_theme_dark_tertiaryContainer,
//    onTertiaryContainer = kaleyra_m3_theme_dark_onTertiaryContainer,
//    error = kaleyra_m3_theme_dark_error,
//    errorContainer = kaleyra_m3_theme_dark_errorContainer,
//    onError = kaleyra_m3_theme_dark_onError,
//    onErrorContainer = kaleyra_m3_theme_dark_onErrorContainer,
//    background = kaleyra_m3_theme_dark_background,
//    onBackground = kaleyra_m3_theme_dark_onBackground,
//    surface = kaleyra_m3_theme_dark_surface,
//    onSurface = kaleyra_m3_theme_dark_onSurface,
//    surfaceVariant = kaleyra_m3_theme_dark_surfaceVariant,
//    onSurfaceVariant = kaleyra_m3_theme_dark_onSurfaceVariant,
//    outline = kaleyra_m3_theme_dark_outline,
//    inverseOnSurface = kaleyra_m3_theme_dark_inverseOnSurface,
//    inverseSurface = kaleyra_m3_theme_dark_inverseSurface,
//    inversePrimary = kaleyra_m3_theme_dark_inversePrimary,
//    surfaceTint = kaleyra_m3_theme_dark_surfaceTint,
//    outlineVariant = kaleyra_m3_theme_dark_outlineVariant,
//    scrim = kaleyra_m3_theme_dark_scrim,
//)

@Composable
fun KaleyraTheme(
    lightColors: Theme.Colors = Theme.Colors(
        primary = kaleyra_theme_light_primary,
        secondary = kaleyra_theme_light_secondary
    ),
    darkColors: Theme.Colors = Theme.Colors(
        primary = kaleyra_theme_dark_primary,
        secondary = kaleyra_theme_dark_secondary
    ),
    fontFamily: FontFamily = KaleyraFontFamily.fontFamily,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when {
        isDarkTheme -> KaleyraDarkColorTheme.copy(
            primary = darkColors.primary,
            onPrimary = onColorFor(darkColors.primary),
            secondary = darkColors.secondary,
            onSecondary = onColorFor(darkColors.secondary)
        )
        else -> KaleyraLightColorTheme.copy(
            primary = lightColors.primary,
            onPrimary = onColorFor(lightColors.primary),
            secondary = lightColors.secondary,
            onSecondary = onColorFor(lightColors.secondary)
        )
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(defaultFontFamily = fontFamily),
        content = content
    )
}

private fun onColorFor(color: Color): Color {
    val argb = color.toArgb()
    return if (MaterialColors.isColorLight(argb)) Color.Black else Color.White
}

private val TermsDarkColorTheme = darkColors(
    primary = Color.White,
    primaryVariant = Color.White,
    surface = Color(0xFF0E0E0E),
    onPrimary = Color.Black,
    onSurface = Color.White
)

private val TermsLightColorTheme = lightColors(
    primary = Color.Black,
    primaryVariant = Color.Black,
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

@Composable
fun TermsAndConditionsTheme(
    fontFamily: FontFamily = KaleyraFontFamily.fontFamily,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when {
        isDarkTheme -> TermsDarkColorTheme
        else -> TermsLightColorTheme
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(defaultFontFamily = fontFamily),
        content = content
    )
}