package com.paras.valorantstate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SpikeRed = Color(0xFFFF4655)
val NightNavy = Color(0xFF0F1923)
val PanelNavy = Color(0xFF1F2933)
val OffWhite = Color(0xFFECE8E1)
val ReadyTeal = Color(0xFF4CD6B0)

private val TacticalColors = darkColorScheme(
    primary = SpikeRed,
    onPrimary = Color.White,
    secondary = ReadyTeal,
    onSecondary = NightNavy,
    background = NightNavy,
    onBackground = OffWhite,
    surface = PanelNavy,
    onSurface = OffWhite,
    onSurfaceVariant = Color(0xFFB0B8C1),
)

@Composable
fun ValorantTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TacticalColors, content = content)
}
