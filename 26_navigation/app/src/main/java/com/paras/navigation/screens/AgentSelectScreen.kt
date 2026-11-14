package com.paras.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.paras.navigation.Agent

@Composable
fun AgentSelectScreen(
    map: String,
    agents: List<Agent>,
    onAgentClick: (Agent) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Lesson("Route: AgentSelect(map = \"$map\"). The map arrived as a navigation ARGUMENT.") }
        items(agents, key = { it.id }) { agent ->
            ValoRow(title = agent.name, subtitle = agent.role, onClick = { onAgentClick(agent) })
        }
    }
}
