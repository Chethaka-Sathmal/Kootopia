package com.ncs.kootopia.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val KotopiaDarkColorScheme = darkColorScheme(
    primary = KootopiaColors.accentBlue,
    onPrimary = KootopiaColors.textPrimary,
    secondary = KootopiaColors.textSecondary,
    onSecondary = KootopiaColors.textPrimary,
    background = KootopiaColors.primaryDark,
    onBackground = KootopiaColors.textPrimary,
    surface = KootopiaColors.surfaceDark,
    onSurface = KootopiaColors.textPrimary,
    error = KootopiaColors.errorRed,
    onError = KootopiaColors.textPrimary
)

private val KootopiaLightColorScheme = lightColorScheme(
    primary = KootopiaColors.accentBlue,
    onPrimary = KootopiaColors.textPrimary,
    secondary = KootopiaColors.textSecondary,
    background = KootopiaColors.primaryDark, // Keep dark theme even in light mode for code editor
    onBackground = KootopiaColors.textPrimary,
    surface = KootopiaColors.surfaceDark,
    onSurface = KootopiaColors.textPrimary,
    error = KootopiaColors.errorRed,
    onError = KootopiaColors.textPrimary
)

@Composable
fun KootopiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic colors to use your custom colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> KotopiaDarkColorScheme
        else -> KootopiaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}