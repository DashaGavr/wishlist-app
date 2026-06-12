package org.example.project.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Blue palette ---
val Blue900  = Color(0xFF0D2D7A)
val Blue700  = Color(0xFF1A50C8)
val Blue500  = Color(0xFF2563EB)
val Blue100  = Color(0xFFDBEAFE)
val Blue50   = Color(0xFFEFF6FF)

// --- Red palette ---
val Red700   = Color(0xFFB91C1C)
val Red500   = Color(0xFFEF4444)
val Red100   = Color(0xFFFFE4E6)

// --- Neutral ---
val White    = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFF8FAFC)
val Gray600  = Color(0xFF475569)
val Gray200  = Color(0xFFE2E8F0)

private val LightColorScheme = lightColorScheme(
    primary            = Blue500,
    onPrimary          = White,
    primaryContainer   = Blue100,
    onPrimaryContainer = Blue900,

    secondary            = Red500,
    onSecondary          = White,
    secondaryContainer   = Red100,
    onSecondaryContainer = Red700,

    background        = White,
    onBackground      = Blue900,

    surface           = OffWhite,
    onSurface         = Blue900,
    surfaceVariant    = Blue50,
    onSurfaceVariant  = Gray600,

    outline           = Gray200,
    error             = Red700,
    onError           = White,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
