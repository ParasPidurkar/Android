package com.paras.parcelstate.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A plain class that is NOT Parcelable. Think of it as a class from a library
 * that you cannot add @Parcelize to. rememberSaveable cannot store it by itself.
 */
data class MatchScore(val attackers: Int, val defenders: Int)

private const val ATTACKERS_KEY = "attackers"
private const val DEFENDERS_KEY = "defenders"

/** Teaches Compose how to turn a MatchScore into a Map of saveable values and back. */
val MatchScoreSaver: Saver<MatchScore, Any> = mapSaver(
    save = { score ->
        mapOf(ATTACKERS_KEY to score.attackers, DEFENDERS_KEY to score.defenders)
    },
    restore = { saved ->
        MatchScore(
            attackers = saved[ATTACKERS_KEY] as Int,
            defenders = saved[DEFENDERS_KEY] as Int,
        )
    },
)

@Composable
fun MapSaverExample() {
    // The value is wrapped in mutableStateOf, so the saver goes in stateSaver.
    // Using saver = here would try to save the state holder instead of the MatchScore.
    var score by rememberSaveable(stateSaver = MatchScoreSaver) {
        mutableStateOf(MatchScore(attackers = 0, defenders = 0))
    }

    ExampleCard(
        title = "mapSaver",
        code = "rememberSaveable(stateSaver = MatchScoreSaver) { ... }",
        lesson = "Score a few rounds, then rotate. MatchScore is not Parcelable, " +
            "so mapSaver stores its fields by name and rebuilds it afterwards.",
    ) {
        Text(
            "${score.attackers} : ${score.defenders}",
            style = MaterialTheme.typography.headlineMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { score = score.copy(attackers = score.attackers + 1) },
            ) {
                Text("Attack +1")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = { score = score.copy(defenders = score.defenders + 1) },
            ) {
                Text("Defend +1")
            }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { score = MatchScore(attackers = 0, defenders = 0) },
        ) {
            Text("Reset")
        }
    }
}
