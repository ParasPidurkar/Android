package com.paras.valorantstate.rounds

import android.annotation.SuppressLint
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * ROUND 1: the bug. mutableStateOf WITHOUT remember.
 *
 * Game version: every time the HUD redraws, someone throws your scoreboard
 * in the bin and hands you a fresh one that says 0 kills.
 */
@SuppressLint("UnrememberedMutableState") // We WANT this bug here, so we can see it.
@Composable
fun Round01NoRemember() {
    // ❌ This line runs on EVERY recomposition and creates a brand-new State starting at 0.
    var kills by mutableStateOf(0)

    // We read `kills` here, in this function, so this function is what Compose re-runs.
    val scoreboard = "Kills: $kills"

    RoundCard(
        round = 1,
        title = "Kills that never count",
        code = "var kills by mutableStateOf(0)   // no remember",
        lesson = "Tap 'Get a kill' and it stays at 0. The tap writes 1 into the state, which " +
            "makes Compose redraw. Redrawing runs this function again, which creates a NEW " +
            "state at 0, so the kill is lost. State has to be remembered.",
    ) {
        Text(scoreboard, style = MaterialTheme.typography.headlineSmall)
        Button(onClick = { kills++ }) { Text("Get a kill") }
    }
}
