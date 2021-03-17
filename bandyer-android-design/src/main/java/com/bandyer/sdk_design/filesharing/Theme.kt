package com.bandyer.sdk_design.filesharing

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.bandyer.sdk_design.filesharing.BandyerTheme.DarkColorPalette
import com.bandyer.sdk_design.filesharing.BandyerTheme.LightColorPalette

object BandyerTheme {

        var DarkColorPalette = darkColors(
            primary = purple200,
            primaryVariant = purple700,
            secondary = teal200
        )

        var LightColorPalette = lightColors(
            primary = purple500,
            primaryVariant = purple700,
            secondary = teal200
        )
}

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */


@Composable
fun FileShareComposeExperimentalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
