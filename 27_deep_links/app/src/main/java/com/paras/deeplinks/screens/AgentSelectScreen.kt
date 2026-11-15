package com.paras.deeplinks.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.paras.deeplinks.Agent

/**
 * Receives a navigation ARGUMENT: `map`.
 *
 * ValoNavApp got it with entry.toRoute<AgentSelect>().map and passes it in as a normal
 * parameter. The screen doesn't care that it came from navigation.
 *
 * onAgentClick hands back the whole Agent for convenience. ValoNavApp then navigates with only
 * agent.id, because arguments should be small and bundle-friendly.
 *
 * Try it: scroll this list, open an agent, press Back. The scroll position is still there.
 * LazyColumn's state uses rememberSaveable, and NavHost keeps saveable state per back stack
 * entry while the entry is covered.
 */
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
