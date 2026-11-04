package com.paras.valorantstate.rounds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// The real spike timer is 45 seconds. It's shorter here so you don't wait too long.
private const val SPIKE_SECONDS = 15

/**
 * ROUND 9: LaunchedEffect(key).
 *
 * Game version: planting the spike starts a countdown that runs by itself.
 * Defusing it stops the countdown.
 */
@Composable
fun Round09LaunchedEffect() {
    var planted by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(SPIKE_SECONDS) }
    var status by remember { mutableStateOf("Spike not planted") }

    // Starts a coroutine whenever `planted` changes. When the key changes,
    // the OLD coroutine is cancelled first. That's what stops the timer on defuse.
    LaunchedEffect(planted) {
        if (!planted) return@LaunchedEffect
        secondsLeft = SPIKE_SECONDS
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
        }
        status = "💥 Spike detonated. Attackers win."
        planted = false
    }

    RoundCard(
        round = 9,
        title = "Spike timer",
        code = "LaunchedEffect(planted) { while (secondsLeft > 0) { delay(1000); secondsLeft-- } }",
        lesson = "LaunchedEffect runs code that takes time (timers, network calls) without " +
            "freezing the UI. Its key decides when it restarts: changing `planted` cancels the " +
            "running countdown. It's also cancelled if this composable leaves the screen.",
    ) {
        if (planted) {
            Text("⏱ $secondsLeft s", style = MaterialTheme.typography.headlineSmall)
        }
        Text(status)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    planted = true
                    status = "Spike planted!"
                },
                enabled = !planted,
            ) {
                Text("Plant")
            }
            OutlinedButton(
                onClick = {
                    planted = false
                    status = "🛡 Spike defused with $secondsLeft s left. Defenders win."
                },
                enabled = planted,
            ) {
                Text("Defuse")
            }
        }
    }
}
