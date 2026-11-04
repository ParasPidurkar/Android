package com.paras.valorantstate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paras.valorantstate.rounds.Round01NoRemember
import com.paras.valorantstate.rounds.Round02NoState
import com.paras.valorantstate.rounds.Round03RememberState
import com.paras.valorantstate.rounds.Round04RememberSaveable
import com.paras.valorantstate.rounds.Round05Hoisting
import com.paras.valorantstate.rounds.Round06DerivedState
import com.paras.valorantstate.rounds.Round07RememberKey
import com.paras.valorantstate.rounds.Round08StateList
import com.paras.valorantstate.rounds.Round09LaunchedEffect
import com.paras.valorantstate.rounds.Round10ViewModel
import com.paras.valorantstate.ui.theme.ValorantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ValorantTheme {
                StateTrainingRange()
            }
        }
    }
}

/** One long scrolling screen. Each card is one "round" that teaches one state concept. */
@Composable
fun StateTrainingRange() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "STATE TRAINING RANGE",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "10 rounds, one Compose state concept per round. Play them in order, " +
                    "and rotate the phone when a round tells you to.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Round01NoRemember()
            Round02NoState()
            Round03RememberState()
            Round04RememberSaveable()
            Round05Hoisting()
            Round06DerivedState()
            Round07RememberKey()
            Round08StateList()
            Round09LaunchedEffect()
            Round10ViewModel()
        }
    }
}
