package com.paras.scaffold.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paras.scaffold.Agent

/**
 * A scrolling list inside the Scaffold's content slot.
 *
 * For a LazyColumn, pass Scaffold's padding as `contentPadding` instead of
 * `Modifier.padding`. The list then still scrolls *behind* the bars (looks nice
 * with a translucent bar), but its first and last items can scroll fully into view.
 */
@Composable
fun RosterScreen(
    roster: List<Agent>,
    contentPadding: PaddingValues,
    onRemove: (Agent) -> Unit,
) {
    val direction = LocalLayoutDirection.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Scaffold's padding + 16dp of our own on each side. The extra 88dp at the
        // bottom stops the FAB from covering the last row: Scaffold does NOT include
        // the FAB in innerPadding.
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(direction) + 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            end = contentPadding.calculateEndPadding(direction) + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(roster, key = { it.name }) { agent ->
            AgentRow(agent, onRemove = { onRemove(agent) })
        }
    }
}

@Composable
private fun AgentRow(agent: Agent, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(agent.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    agent.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove ${agent.name}")
            }
        }
    }
}
