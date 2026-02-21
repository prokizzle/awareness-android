package com.feelingmindful.awareness.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple500,
    onPrimary = SurfaceLight,
    primaryContainer = Purple700,
    onPrimaryContainer = SurfaceLight,
    secondary = Gray400,
    onSecondary = Gray900,
    background = BackgroundDark,
    onBackground = SurfaceLight,
    surface = SurfaceDark,
    onSurface = SurfaceLight,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    error = Error,
    onError = SurfaceLight
)

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = SurfaceLight,
    primaryContainer = Purple600,
    onPrimaryContainer = SurfaceLight,
    secondary = Gray600,
    onSecondary = SurfaceLight,
    background = BackgroundLight,
    onBackground = Gray900,
    surface = SurfaceLight,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    error = Error,
    onError = SurfaceLight
)

@Composable
fun AwarenessByChrisShanksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
