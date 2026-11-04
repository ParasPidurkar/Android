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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

private const val START_CREDITS = 800
private const val MAX_CREDITS = 9000
private const val ROUND_WIN_BONUS = 3000
private const val VANDAL_PRICE = 2900

/**
 * ROUND 4: rememberSaveable.
 *
 * Game version: your credits are saved on your account, so they're still there
 * after you alt-tab, the game minimizes, or the screen rotates.
 */
@Composable
fun Round04RememberSaveable() {
    // Like remember, but the value is also written into the saved-instance-state Bundle,
    // so it survives rotation AND Android killing the app in the background.
    var credits by rememberSaveable { mutableIntStateOf(START_CREDITS) }

    RoundCard(
        round = 4,
        title = "Credits that survive rotation",
        code = "var credits by rememberSaveable { mutableIntStateOf(800) }",
        lesson = "Win a round or two, then rotate the phone: your credits are still there. " +
            "Bundle space is small, so save small things here (numbers, text, IDs), not big lists.",
    ) {
        Text("Credits: $credits", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { credits = (credits + ROUND_WIN_BONUS).coerceAtMost(MAX_CREDITS) }) {
                Text("Win round +$ROUND_WIN_BONUS")
            }
            OutlinedButton(
                onClick = { credits -= VANDAL_PRICE },
                enabled = credits >= VANDAL_PRICE,
            ) {
                Text("Buy Vandal")
            }
        }
    }
}
