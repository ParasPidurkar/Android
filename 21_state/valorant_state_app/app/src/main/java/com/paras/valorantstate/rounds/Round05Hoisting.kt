package com.paras.valorantstate.rounds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class Weapon(val name: String, val price: Int)

private val ARMORY = listOf(
    Weapon("Sheriff", 800),
    Weapon("Spectre", 1600),
    Weapon("Vandal", 2900),
    Weapon("Operator", 4700),
)

private const val ROUND_CREDITS = 3900

/**
 * ROUND 5: state hoisting.
 *
 * Game version: the shop screen doesn't hold your money. It shows the prices,
 * and when you click a gun it tells the game "the player wants this".
 * The game (the parent) owns the credits and decides what happens.
 */
@Composable
fun Round05Hoisting() {
    // The PARENT owns the state.
    var credits by rememberSaveable { mutableIntStateOf(ROUND_CREDITS) }
    var equipped by rememberSaveable { mutableStateOf("Classic") }

    RoundCard(
        round = 5,
        title = "The buy menu",
        code = "BuyMenu(credits = credits, onBuy = { ... })",
        lesson = "State goes DOWN (credits are passed into BuyMenu), events go UP (onBuy reports " +
            "the click). BuyMenu has no remember inside, so it's 'stateless': you can reuse it " +
            "anywhere and it's easy to test.",
    ) {
        Text("Credits: $credits   •   Equipped: $equipped", style = MaterialTheme.typography.titleMedium)

        BuyMenu(
            credits = credits,
            onBuy = { weapon ->
                credits -= weapon.price
                equipped = weapon.name
            },
        )

        TextButton(onClick = {
            credits = ROUND_CREDITS
            equipped = "Classic"
        }) {
            Text("New round")
        }
    }
}

/** STATELESS: it only shows what it's given and reports clicks up to the parent. */
@Composable
private fun BuyMenu(credits: Int, onBuy: (Weapon) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ARMORY.forEach { weapon ->
            OutlinedButton(
                onClick = { onBuy(weapon) },
                enabled = credits >= weapon.price,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(weapon.name, modifier = Modifier.weight(1f))
                Text("${weapon.price}")
            }
        }
    }
}
