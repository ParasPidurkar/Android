package com.paras.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paras.navigation.Agent

@Composable
fun AgentDetailScreen(
    agent: Agent,
    map: String,
    onLockIn: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text(agent.role.uppercase(), letterSpacing = 3.sp, color = MaterialTheme.colorScheme.secondary)
        Text(agent.name.uppercase(), fontSize = 48.sp, fontWeight = FontWeight.Black)
        HorizontalDivider(color = MaterialTheme.colorScheme.primary)
        Stat("Origin", agent.origin)
        Stat("Ultimate", agent.ultimate)
        Stat("Map", map)
        Lesson("Route: AgentDetail(agentId = \"${agent.id}\", map = \"$map\"). Only the id is passed, not the whole Agent.")

        Spacer(Modifier.weight(1f))

        Lesson("LOCK IN uses popUpTo<Lobby>, so Back from the match skips agent select.")
        ValoButton("Lock in", onClick = onLockIn)
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
