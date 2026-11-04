package com.paras.valorantstate.rounds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class Agent(val role: String) {
    Jett("Duelist"),
    Sage("Sentinel"),
    Sova("Initiator"),
    Omen("Controller"),
}

/** Pretend this is expensive, like loading a big file. We only want it to run when needed. */
private fun buildGamePlan(agent: Agent): String = when (agent.role) {
    "Duelist" -> "Entry first and take space for the team."
    "Sentinel" -> "Lock down a site, watch the flank, keep the team alive."
    "Initiator" -> "Gather info and set up your team's push."
    else -> "Block sightlines with smokes so the team can move."
}

/** Plain counter, only used to show how many times the plan was built. */
private class BuildCounter {
    var count = 0
}

/**
 * ROUND 7: remember(key).
 *
 * Game version: when you switch agent in agent select, you need a new game plan.
 * But getting a kill shouldn't make you rethink the whole plan.
 */
@Composable
fun Round07RememberKey() {
    var agent by rememberSaveable { mutableStateOf(Agent.Jett) }
    var kills by remember { mutableIntStateOf(0) }
    val builds = remember { BuildCounter() }

    // Re-runs the block ONLY when `agent` changes. Any other redraw reuses the cached plan.
    val gamePlan = remember(agent) {
        builds.count++
        buildGamePlan(agent)
    }

    RoundCard(
        round = 7,
        title = "Agent select",
        code = "val gamePlan = remember(agent) { buildGamePlan(agent) }",
        lesson = "Getting a kill redraws this card, but the counter below doesn't go up, so the " +
            "plan was NOT rebuilt. Switching agent changes the key, so it is rebuilt. With plain " +
            "remember { } (no key), the plan would be stuck on Jett forever.",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Agent.entries.forEach { option ->
                val pick = { agent = option }
                val padding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                if (option == agent) {
                    Button(onClick = pick, contentPadding = padding, modifier = Modifier.weight(1f)) {
                        Text(option.name, fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(onClick = pick, contentPadding = padding, modifier = Modifier.weight(1f)) {
                        Text(option.name, fontSize = 12.sp)
                    }
                }
            }
        }
        Text("${agent.name} (${agent.role}): $gamePlan", style = MaterialTheme.typography.bodyLarge)
        Text(
            "Kills: $kills   •   Game plan built ${builds.count} times",
            style = MaterialTheme.typography.bodySmall,
        )
        TextButton(onClick = { kills++ }) { Text("Get a kill (redraws, keeps the plan)") }
    }
}
