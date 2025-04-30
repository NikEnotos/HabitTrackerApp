package com.example.tracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext



private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),            // Bright green for buttons, active elements
    onPrimary = Color.White,                      // White text on top of the primary color

    primaryContainer = Color(0xFFE7E7E7),
    onPrimaryContainer = Color.Black,
    onSecondaryContainer = Color.Gray,

    secondary = Color(0xFFD3D3D3),
    onSecondary = Color.Black,
    onTertiary = Color.White,

    background = Color.White, // Main background color
    onBackground = Color.Black, // Text color on background

    surface = Color.White, // Surface color for cards and dialogs
    onSurface = Color.Black, // Text color on surface

    error = Color(0xFFB00020), // Error color
    onError = Color.White, // Text color on error
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A),           // Softer green for buttons, active elements
    onPrimary = Color(0xFF444444),

    primaryContainer = Color(0xFF626262),
    onPrimaryContainer = Color.White,            // White text/icons on primaryContainer
    onSecondaryContainer = Color.LightGray,

    secondary = Color(0xFF565656), // Darker gray for background and secondary elements
    onSecondary = Color.White, // Text color on secondary elements
    onTertiary = Color(0xFFEFEFEF),

    background = Color(0xFF121212), // Dark background color
    onBackground = Color.White, // Text color on background

    surface = Color(0xFF1E1E1E), // Darker surface color
    onSurface = Color.White, // Text color on surface

    error = Color(0xFFCF6679), // Error color
    onError = Color.Black, // Text color on error
)

@Composable
fun TrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}