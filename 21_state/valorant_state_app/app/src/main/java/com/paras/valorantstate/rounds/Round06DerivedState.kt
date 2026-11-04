package com.paras.valorantstate.rounds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val ULT_COST = 7

/**
 * ROUND 6: derivedStateOf.
 *
 * Game version: ult points go up on every kill, but the "ULT READY" banner only
 * cares about one thing: are there enough points yet? Yes or no.
 */
@Composable
fun Round06DerivedState() {
    var ultPoints by remember { mutableIntStateOf(0) }

    // A value CALCULATED from other state. It only reports a change when the
    // RESULT changes (false -> true), not on every single point.
    val ultReady by remember { derivedStateOf { ultPoints >= ULT_COST } }

    RoundCard(
        round = 6,
        title = "Ult ready?",
        code = "val ultReady by remember { derivedStateOf { ultPoints >= 7 } }",
        lesson = "ultPoints changes 7 times, but ultReady changes only once. Use derivedStateOf " +
            "when the input changes often but the answer changes rarely, like 'is the list " +
            "scrolled?' or 'is the form valid?'. Code that only reads ultReady then redraws less.",
    ) {
        Text("Ult points: $ultPoints / $ULT_COST", style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = { ultPoints / ULT_COST.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
        if (ultReady) {
            Text(
                "⚡ ULTIMATE READY",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { ultPoints = (ultPoints + 1).coerceAtMost(ULT_COST) },
                enabled = !ultReady,
            ) {
                Text("Get a kill (+1)")
            }
            OutlinedButton(onClick = { ultPoints = 0 }, enabled = ultReady) {
                Text("Use ult")
            }
        }
    }
}
