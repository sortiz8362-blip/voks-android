package com.voks.social.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VoksBlue,
    background = VoksBlack,
    surface = VoksBlack,
    surfaceVariant = VoksDarkGray,
    onPrimary = VoksWhite,
    onBackground = VoksWhite,
    onSurface = VoksWhite,
    onSurfaceVariant = VoksDarkText,
    error = VoksError
)

private val LightColorScheme = lightColorScheme(
    primary = VoksBlue,
    background = VoksWhite,
    surface = VoksWhite,
    surfaceVariant = VoksLightGray,
    onPrimary = VoksWhite,
    onBackground = VoksBlack,
    onSurface = VoksBlack,
    onSurfaceVariant = VoksLightText,
    error = VoksError
)

@Composable
fun VoksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos dynamicColor por defecto para mantener nuestra identidad de marca intacta
    dynamicColor: Boolean = false,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Hacemos que la barra de estado superior coincida con nuestro fondo
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}