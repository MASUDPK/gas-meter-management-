package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TealPrimary = Color(0xFF0F766E)
val TealDark = Color(0xFF115E59)
val TealLight = Color(0xFF14B8A6)
val TealContainer = Color(0xFFCCFBF1)
val OnTealContainer = Color(0xFF134E4A)

val FlameOrange = Color(0xFFD97706)
val FlameLight = Color(0xFFFEF3C7)

val StatusPaid = Color(0xFF16A34A)
val StatusDue = Color(0xFFDC2626)
val StatusPartial = Color(0xFFEA580C)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = TealLight,
    onSecondary = Color.White,
    tertiary = FlameOrange,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = TealDark,
    onPrimaryContainer = TealContainer,
    secondary = TealLight,
    onSecondary = Color(0xFF042F2E),
    tertiary = FlameOrange,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569)
)

@Composable
fun JamilaBhavanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
