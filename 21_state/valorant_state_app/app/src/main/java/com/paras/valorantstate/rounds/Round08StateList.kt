package com.paras.valorantstate.rounds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TEAM_SIZE = 5

private val AGENT_POOL = listOf(
    "Jett", "Sage", "Sova", "Omen",
    "Phoenix", "Killjoy", "Breach", "Viper",
)

/**
 * ROUND 8: mutableStateListOf.
 *
 * Game version: the team lobby. Players lock in and leave, and everyone's
 * screen updates every time the team list changes.
 */
@Composable
fun Round08StateList() {
    // An OBSERVABLE list: add() and remove() trigger a redraw.
    val team = remember { mutableStateListOf<String>() }

    RoundCard(
        round = 8,
        title = "Team lobby",
        code = "val team = remember { mutableStateListOf<String>() }",
        lesson = "team.add() and team.remove() redraw the screen right away. Watch out: " +
            "mutableStateOf(arrayListOf()) + list.add() would NOT redraw, because the list " +
            "object stays the same. Compose only sees the list change if the list is observable.",
    ) {
        Text("Your team (${team.size}/$TEAM_SIZE)", style = MaterialTheme.typography.titleMedium)

        if (team.isEmpty()) {
            Text("Nobody locked in yet", style = MaterialTheme.typography.bodySmall)
        }
        team.forEach { name ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔒 $name", modifier = Modifier.weight(1f))
                TextButton(onClick = { team.remove(name) }) { Text("Kick") }
            }
        }

        Text("Lock in an agent:", style = MaterialTheme.typography.bodySmall)
        AGENT_POOL.chunked(4).forEach { rowAgents ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                rowAgents.forEach { name ->
                    OutlinedButton(
                        onClick = { team.add(name) },
                        // Each agent can only be picked once, and the team holds 5.
                        enabled = name !in team && team.size < TEAM_SIZE,
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(name, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}
