package com.paras.deeplinks.screens

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
 * START DESTINATION. The first screen, and the bottom of the back stack.
 *
 * Notice what this screen does NOT know: NavController, routes, or that MapPick exists.
 * It receives:
 *   - selectedMap   : plain data. ValoNavApp reads it from the Lobby entry's SavedStateHandle,
 *                     where MapPick left it as a RESULT.
 *   - onPickMap     : "the user wants to choose a map". ValoNavApp decides that means
 *                     navigate(MapPick).
 *   - onSelectAgent : "the user wants to pick an agent for this map". ValoNavApp turns it into
 *                     navigate(AgentSelect(map)).
 *
 * This is "state down, events up" (state hoisting), applied to navigation. Because of it you
 * could @Preview this screen with LobbyScreen("Ascent", {}, {}) and no NavController.
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
