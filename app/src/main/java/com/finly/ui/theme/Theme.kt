package com.finly.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ==================== Pastel Colors ====================

// Primary colors - Pastel Blue/Teal
val md_theme_light_primary = Color(0xFF5C9EAD)          // Pastel teal
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFD0ECF0) // Very light teal
val md_theme_light_onPrimaryContainer = Color(0xFF2C6975)

val md_theme_light_secondary = Color(0xFF7AB8BF)        // Light teal
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFE8F6F8)
val md_theme_light_onSecondaryContainer = Color(0xFF3D8B94)

val md_theme_light_background = Color(0xFFF8FAFB)       // Very light blue-gray
val md_theme_light_onBackground = Color(0xFF2D3436)
val md_theme_light_surface = Color(0xFFFFFFFF)
val md_theme_light_onSurface = Color(0xFF2D3436)
val md_theme_light_surfaceVariant = Color(0xFFF0F4F5)
val md_theme_light_onSurfaceVariant = Color(0xFF636E72)

val md_theme_light_error = Color(0xFFE17055)            // Pastel coral for error
val md_theme_light_onError = Color(0xFFFFFFFF)

// Dark theme colors - Pastel dark
val md_theme_dark_primary = Color(0xFF8ECFD6)           // Light pastel teal
val md_theme_dark_onPrimary = Color(0xFF1A4A52)
val md_theme_dark_primaryContainer = Color(0xFF2C6975)
val md_theme_dark_onPrimaryContainer = Color(0xFFD0ECF0)

val md_theme_dark_secondary = Color(0xFFA8D8DC)
val md_theme_dark_onSecondary = Color(0xFF1A4A52)
val md_theme_dark_secondaryContainer = Color(0xFF3D8B94)
val md_theme_dark_onSecondaryContainer = Color(0xFFE8F6F8)

val md_theme_dark_background = Color(0xFF1A2327)        // Dark blue-gray
val md_theme_dark_onBackground = Color(0xFFE0E6E8)
val md_theme_dark_surface = Color(0xFF222D32)
val md_theme_dark_onSurface = Color(0xFFE0E6E8)
val md_theme_dark_surfaceVariant = Color(0xFF2D3A40)
val md_theme_dark_onSurfaceVariant = Color(0xFFB0BEC5)

val md_theme_dark_error = Color(0xFFFF8A65)
val md_theme_dark_onError = Color(0xFF000000)

// Special colors for income/expense - Pastel versions
val IncomeGreen = Color(0xFF81C784)                     // Pastel green
val ExpenseRed = Color(0xFFE57373)                      // Pastel red
val IncomeGreenDark = Color(0xFFA5D6A7)
val ExpenseRedDark = Color(0xFFEF9A9A)

// ==================== Color Schemes ====================

private val LightColorScheme = lightColorScheme(
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
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    error = md_theme_light_error,
    onError = md_theme_light_onError
)

private val DarkColorScheme = darkColorScheme(
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
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError
)

// ==================== Theme ====================

@Composable
fun FinlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Tắt dynamic color để giữ brand identity
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// ==================== Typography ====================

val Typography = Typography()
