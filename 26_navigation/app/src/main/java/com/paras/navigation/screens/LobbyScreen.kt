package com.paras.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
 * FIRST SCREEN (start destination). It's always at the bottom of the back stack.
 *
 * This screen knows NOTHING about navigation. It doesn't know the NavController or MapPick.
 * It only gets:
 *   - selectedMap   : the map to show. ValoNavApp reads it from the result MapPick sent back.
 *   - onPickMap     : "the user wants to choose a map". ValoNavApp opens MapPick.
 *   - onSelectAgent : "the user wants to pick an agent". ValoNavApp opens AgentSelect(map).
 *
 * Data goes DOWN into the screen, events go UP out of it ("state hoisting").
 * Bonus: you can preview it with LobbyScreen("Ascent", {}, {}), no NavController needed.
 */
@Composable
fun LobbyScreen(
    selectedMap: String?,
    onPickMap: () -> Unit,
    onSelectAgent: (map: String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text("COMPETITIVE", style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp,
            color = MaterialTheme.colorScheme.secondary)
        Text(
            selectedMap?.uppercase() ?: "NO MAP SELECTED",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
        )
        Lesson("The map name is a RESULT sent back from the Choose Map screen.")

        Spacer(Modifier.weight(1f))

        ValoButton("Choose map", onClick = onPickMap)
        ValoButton(
            "Select agent",
            enabled = selectedMap != null,
            onClick = { selectedMap?.let(onSelectAgent) },
        )
    }
}
