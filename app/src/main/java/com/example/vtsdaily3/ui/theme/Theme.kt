package com.example.vtsdaily3.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
val VtsLightColorScheme: ColorScheme = lightColorScheme(
    primary = VtsGreen,
    onPrimary = Color.White,

    secondary = VtsGreen,
    onSecondary = Color.White,

    tertiary = VtsGreen,
    onTertiary = Color.White,

    background = AppBackground,
    onBackground = VtsTextPrimary_Light,

    surface = Color.White,
    onSurface = VtsTextPrimary_Light,

    surfaceVariant = Color.White,
    onSurfaceVariant = VtsTextSecondary_Light,

    primaryContainer = VtsGreen,
    onPrimaryContainer = Color.White,

    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = VtsTextPrimary_Light,

    tertiaryContainer = Color(0xFFE8F5E9),
    onTertiaryContainer = VtsTextPrimary_Light,

    outline = VtsOutline_Light,

    error = VtsError,
    onError = Color.White
)
val VtsDarkColorScheme: ColorScheme = darkColorScheme(
    primary = VtsGreen,
    onPrimary = Color.Black,

    secondary = VtsGreen,
    onSecondary = Color.Black,

    tertiary = VtsGreen,
    onTertiary = Color.Black,

    background = VtsBackground_Dark,
    onBackground = VtsText_OnDark,

    surface = VtsSurface_Dark,
    onSurface = VtsText_OnDark,

    surfaceVariant = VtsBackground_Dark,
    onSurfaceVariant = VtsText_OnDark,

    primaryContainer = VtsGreen,
    onPrimaryContainer = Color.Black,

    secondaryContainer = VtsSurface_Dark,
    onSecondaryContainer = VtsText_OnDark,

    tertiaryContainer = VtsSurface_Dark,
    onTertiaryContainer = VtsText_OnDark,

    outline = VtsOutline_Dark,

    error = VtsError,
    onError = Color.Black
)

@Composable
fun Vts3DailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // ✅ MUST be false to match VTSDaily colors exactly
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> VtsDarkColorScheme
        else -> com.example.vtsdaily3.ui.theme.VtsLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = androidx.compose.material3.Shapes(),
        content = content
    )
}