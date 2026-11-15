package com.paras.deeplinks.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paras.deeplinks.Agent

/**
 * Reached through LOCK IN with popUpTo<Lobby>, so the back stack is [Lobby, Match].
 *
 * Both the system Back button and "Back to lobby" lead to the Lobby, but they work differently:
 *   - system Back      -> popBackStack(): removes one entry (Match).
 *   - "Back to lobby"  -> popBackStack<Lobby>(): removes everything above Lobby, however deep.
 * Open Career from here ([Lobby, Match, Career]), and the two would behave differently.
 */
@Composable
fun MatchScreen(
    agent: Agent,
    map: String,
    onBackToLobby: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Spacer(Modifier.weight(1f))
        Text("VICTORY", fontSize = 56.sp, fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.secondary)
        Text("13 – 7", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("${agent.name} on $map", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Lesson("Back stack is now just [Lobby, Match]. Press system Back or the button below.")
        ValoButton("Back to lobby", onClick = onBackToLobby)
    }
}
