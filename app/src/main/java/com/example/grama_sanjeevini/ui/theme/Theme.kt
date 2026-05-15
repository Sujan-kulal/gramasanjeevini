package com.example.grama_sanjeevini.ui.theme

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
    primary = MedicalGreenPrimary,
    onPrimary = MedicalGreenOnPrimary,
    primaryContainer = MedicalGreenContainer,
    onPrimaryContainer = MedicalGreenOnContainer,
    secondary = MedicalSecondary,
    onSecondary = MedicalOnSecondary,
    secondaryContainer = MedicalSecondaryContainer,
    onSecondaryContainer = MedicalOnSecondaryContainer,
    tertiary = MedicalTertiary,
    onTertiary = MedicalOnTertiary,
    tertiaryContainer = MedicalTertiaryContainer,
    onTertiaryContainer = MedicalOnTertiaryContainer,
    error = MedicalError,
    onError = MedicalOnError,
    background = MedicalBackground,
    onBackground = MedicalOnBackground,
    surface = MedicalSurface,
    onSurface = MedicalOnSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalGreenPrimary,
    onPrimary = MedicalGreenOnPrimary,
    primaryContainer = MedicalGreenContainer,
    onPrimaryContainer = MedicalGreenOnContainer,
    secondary = MedicalSecondary,
    onSecondary = MedicalOnSecondary,
    secondaryContainer = MedicalSecondaryContainer,
    onSecondaryContainer = MedicalOnSecondaryContainer,
    tertiary = MedicalTertiary,
    onTertiary = MedicalOnTertiary,
    tertiaryContainer = MedicalTertiaryContainer,
    onTertiaryContainer = MedicalOnTertiaryContainer,
    error = MedicalError,
    onError = MedicalOnError,
    background = MedicalBackground,
    onBackground = MedicalOnBackground,
    surface = MedicalSurface,
    onSurface = MedicalOnSurface,
)

@Composable
fun GramaSanjeeviniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to enforce the Green Medical theme
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
