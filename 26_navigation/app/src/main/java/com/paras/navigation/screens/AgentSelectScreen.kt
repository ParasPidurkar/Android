package com.paras.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.paras.navigation.Agent

/**
 * Gets one navigation ARGUMENT: `map`.
 *
 * ValoNavApp reads it with entry.toRoute<AgentSelect>().map and passes it in like a normal
 * parameter. The screen doesn't know it came from navigation.
 *
 * onAgentClick gives back the whole Agent (easy to use). ValoNavApp then sends only agent.id
 * to the next screen, because navigation data should stay small.
 *
 * Try it: scroll the list, open an agent, press Back. You're at the same scroll position.
 * The NavHost remembers the saved state of each screen while it's hidden below another one.
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
