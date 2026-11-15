package com.paras.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Opened from the 👤 icon in the top bar, which is visible on EVERY screen.
 * It uses launchSingleTop = true, so tapping 👤 again doesn't open a second Career.
 *
 * No arguments and no lambdas. You leave it with Back or the ← arrow.
 */
@Composable
fun CareerScreen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text("RANK", letterSpacing = 3.sp, color = MaterialTheme.colorScheme.secondary)
        Text("IMMORTAL 2", fontSize = 40.sp, fontWeight = FontWeight.Black)
        Text("Win rate 58%  ·  K/D 1.31  ·  HS 27%", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Lesson(
            "Opened from the 👤 icon with launchSingleTop = true. Tap the icon again here: " +
                "nothing is added, and one Back still leaves Career."
        )
    }
}
