package com.paras.valorantstate.rounds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

/** A normal Kotlin class. Compose has no way to know when `kills` changes. */
private class PaperScoreboard {
    var kills = 0
}

/**
 * ROUND 2: the other bug. remember WITHOUT mutableStateOf.
 *
 * Game version: your kills ARE being counted on a paper scoreboard,
 * but nobody tells the HUD to refresh, so the screen keeps showing old numbers.
 */
@Composable
fun Round02NoState() {
    // ✅ remember keeps the same scoreboard object across recompositions...
    // ❌ ...but `kills` is a plain var, not State, so changing it triggers no redraw.
    val board = remember { PaperScoreboard() }

    // Real State, used only to force a redraw so you can see the hidden kills.
    var hudRefreshes by remember { mutableIntStateOf(0) }

    RoundCard(
        round = 2,
        title = "The HUD that doesn't refresh",
        code = "val board = remember { PaperScoreboard() }   // no mutableStateOf",
        lesson = "Tap 'Get a kill' a few times: nothing changes. Now tap 'Refresh HUD' and all " +
            "those kills appear at once. remember kept the value, but only State tells " +
            "Compose WHEN to redraw.",
    ) {
        Text("Kills: ${board.kills}", style = MaterialTheme.typography.headlineSmall)
        Text("HUD refreshed $hudRefreshes times", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { board.kills++ }) { Text("Get a kill") }
            OutlinedButton(onClick = { hudRefreshes++ }) { Text("Refresh HUD") }
        }
    }
}
