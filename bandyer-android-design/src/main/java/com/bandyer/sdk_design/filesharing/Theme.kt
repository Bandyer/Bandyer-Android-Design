package com.bandyer.sdk_design.filesharing

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.bandyer.sdk_design.filesharing.BandyerTheme.DarkColorPalette
import com.bandyer.sdk_design.filesharing.BandyerTheme.LightColorPalette

object BandyerTheme {

    var LightColorPalette = Colors(
        primary = colorPrimary,
        primaryVariant = colorPrimaryVariant,
        secondary = colorSecondary,
        secondaryVariant = colorSecondaryVariant,
        background = colorBackground,
        surface = colorSurface,
        error = colorError,
        onPrimary = colorOnPrimary,
        onSecondary = colorOnSecondary,
        onBackground = colorOnBackground,
        onSurface = colorOnSurface,
        onError = colorOnError,
        isLight = true
    )

    var DarkColorPalette = Colors(
        primary = colorPrimaryNight,
        primaryVariant = colorPrimaryVariantNight,
        secondary = colorSecondary,
        secondaryVariant = colorSecondaryVariantNight,
        background = colorBackgroundNight,
        surface = colorSurfaceNight,
        error = colorError,
        onPrimary = colorOnPrimaryNight,
        onSecondary = colorOnSecondary,
        onBackground = colorOnBackgroundNight,
        onSurface = colorOnSurfaceNight,
        onError = colorOnError,
        isLight = false
    )

}

@Composable
fun BandyerSdkDesignComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        typography = typography,
        content = content
    )
}

