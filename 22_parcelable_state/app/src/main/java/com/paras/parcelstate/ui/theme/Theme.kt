package com.paras.parcelstate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TacticalColors = darkColorScheme(
    primary = Color(0xFFFF4655),
    onPrimary = Color.White,
    secondary = Color(0xFF4CD6B0),
    onSecondary = Color(0xFF0F1923),
    background = Color(0xFF0F1923),
    onBackground = Color(0xFFECE8E1),
    surface = Color(0xFF1F2933),
    onSurface = Color(0xFFECE8E1),
    onSurfaceVariant = Color(0xFFB0B8C1),
)

@Composable
fun ParcelStateTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TacticalColors, content = content)
}
