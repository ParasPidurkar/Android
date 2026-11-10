package com.paras.parcelstate.examples

import android.os.Parcelable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.parcelize.Parcelize

/**
 * @Parcelize writes the Parcelable code for us. Every property must itself be
 * saveable, so Weapon is @Parcelize too (a Parcelable can contain a Parcelable).
 */
@Parcelize
data class Weapon(val name: String, val price: Int) : Parcelable

@Parcelize
data class Loadout(
    val agent: String,
    val weapon: Weapon,
    val credits: Int,
) : Parcelable {
    /** Credits available if the current weapon is sold back. */
    val balance: Int get() = credits + weapon.price

    fun buy(newWeapon: Weapon) = copy(weapon = newWeapon, credits = balance - newWeapon.price)
}

private val Agents = listOf("Jett", "Sage", "Omen", "Killjoy")
private val Arsenal = listOf(
    Weapon("Classic", 0),
    Weapon("Spectre", 1600),
    Weapon("Vandal", 2900),
    Weapon("Operator", 4700),
)
private val StartingLoadout = Loadout(agent = Agents.first(), weapon = Arsenal.first(), credits = 3900)

@Composable
fun ParcelizeExample() {
    // Loadout is Parcelable, so it can go straight into the saved Bundle.
    // No custom Saver is needed.
    var loadout by rememberSaveable { mutableStateOf(StartingLoadout) }

    ExampleCard(
        title = "@Parcelize",
        code = "rememberSaveable { mutableStateOf(parcelableLoadout) }",
        lesson = "Change the agent and buy a weapon, then rotate. The whole object comes back " +
            "because Android can write a Parcelable into the saved instance state.",
    ) {
        Text("Agent: ${loadout.agent}")
        Text("Weapon: ${loadout.weapon.name}")
        Text("Credits: ${loadout.credits}")

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val next = (Agents.indexOf(loadout.agent) + 1) % Agents.size
                loadout = loadout.copy(agent = Agents[next])
            },
        ) {
            Text("Next agent")
        }

        Arsenal.forEach { weapon ->
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = weapon != loadout.weapon && weapon.price <= loadout.balance,
                onClick = { loadout = loadout.buy(weapon) },
            ) {
                Text("${weapon.name} (${weapon.price})")
            }
        }
    }
}
