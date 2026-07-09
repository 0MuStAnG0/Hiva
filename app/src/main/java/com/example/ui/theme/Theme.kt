package com.example.ui.theme

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
import androidx.compose.ui.text.font.FontFamily
import com.example.data.AppFontSize
import com.example.data.AppThemeMode

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
)

private val TypographyDefault = Typography

private fun getTypographyByScale(scale: Float): androidx.compose.material3.Typography {
    return androidx.compose.material3.Typography().copy(
        displayLarge = TypographyDefault.displayLarge.copy(fontSize = TypographyDefault.displayLarge.fontSize * scale),
        displayMedium = TypographyDefault.displayMedium.copy(fontSize = TypographyDefault.displayMedium.fontSize * scale),
        displaySmall = TypographyDefault.displaySmall.copy(fontSize = TypographyDefault.displaySmall.fontSize * scale),
        headlineLarge = TypographyDefault.headlineLarge.copy(fontSize = TypographyDefault.headlineLarge.fontSize * scale),
        headlineMedium = TypographyDefault.headlineMedium.copy(fontSize = TypographyDefault.headlineMedium.fontSize * scale),
        headlineSmall = TypographyDefault.headlineSmall.copy(fontSize = TypographyDefault.headlineSmall.fontSize * scale),
        titleLarge = TypographyDefault.titleLarge.copy(fontSize = TypographyDefault.titleLarge.fontSize * scale),
        titleMedium = TypographyDefault.titleMedium.copy(fontSize = TypographyDefault.titleMedium.fontSize * scale),
        titleSmall = TypographyDefault.titleSmall.copy(fontSize = TypographyDefault.titleSmall.fontSize * scale),
        bodyLarge = TypographyDefault.bodyLarge.copy(fontSize = TypographyDefault.bodyLarge.fontSize * scale),
        bodyMedium = TypographyDefault.bodyMedium.copy(fontSize = TypographyDefault.bodyMedium.fontSize * scale),
        bodySmall = TypographyDefault.bodySmall.copy(fontSize = TypographyDefault.bodySmall.fontSize * scale),
        labelLarge = TypographyDefault.labelLarge.copy(fontSize = TypographyDefault.labelLarge.fontSize * scale),
        labelMedium = TypographyDefault.labelMedium.copy(fontSize = TypographyDefault.labelMedium.fontSize * scale),
        labelSmall = TypographyDefault.labelSmall.copy(fontSize = TypographyDefault.labelSmall.fontSize * scale),
    )
}

@Composable
fun MyApplicationTheme(
    appThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    appFontSize: AppFontSize = AppFontSize.MEDIUM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appThemeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    
    val currentTypography = when (appFontSize) {
        AppFontSize.SMALL -> getTypographyByScale(0.85f)
        AppFontSize.MEDIUM -> TypographyDefault
        AppFontSize.LARGE -> getTypographyByScale(1.15f)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = currentTypography,
        content = content
    )
}
