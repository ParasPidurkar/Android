package com.paras.valorantstate.rounds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

/**
 * ROUND 3: the fix. remember + mutableStateOf together.
 *
 * Game version: a real scoreboard. It's kept for the whole match (remember),
 * and it refreshes the HUD every time you get a kill (mutableStateOf).
 */
@Composable
fun Round03RememberState() {
    // remember       -> keep the SAME State object across recompositions
    // mutableStateOf -> tell Compose to redraw when the value changes
    // (For Int you can also use mutableIntStateOf(0), which skips boxing. Later rounds use it.)
    var kills by remember { mutableStateOf(0) }

    RoundCard(
        round = 3,
        title = "A kill counter that works",
        code = "var kills by remember { mutableStateOf(0) }",
        lesson = "Kills now count and show up right away. Next, get a few kills and ROTATE the " +
            "phone: it goes back to 0. Rotation rebuilds the Activity, and remember is wiped " +
            "with it. Round 4 fixes that.",
    ) {
        Text("Kills: $kills", style = MaterialTheme.typography.headlineSmall)
        if (kills >= 5) {
            Text("🔥 ACE!", color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { kills++ }) { Text("Get a kill") }
            OutlinedButton(onClick = { kills = 0 }) { Text("Reset") }
        }
    }
}
