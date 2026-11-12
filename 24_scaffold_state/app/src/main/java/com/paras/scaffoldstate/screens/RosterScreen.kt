package com.paras.scaffoldstate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import com.paras.scaffoldstate.Agent

/**
 * Doesn't own any state. The roster and the scroll position (listState) are passed in
 * from SquadApp, so they outlive this screen when you switch to the Inspector.
 */
@Composable
fun RosterScreen(
    roster: List<Agent>,
    listState: LazyListState,
    innerPadding: PaddingValues,
    onRemove: (Agent) -> Unit,
) {
    val dir = LocalLayoutDirection.current
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(dir) + 16.dp,
            top = innerPadding.calculateTopPadding() + 16.dp,
            end = innerPadding.calculateEndPadding(dir) + 16.dp,
            bottom = innerPadding.calculateBottomPadding() + 88.dp, // room for the FAB
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(roster, key = { it.name }) { agent ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(agent.name, fontWeight = FontWeight.Bold)
                        Text(
                            agent.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    IconButton(onClick = { onRemove(agent) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove ${agent.name}")
                    }
                }
            }
        }
    }
}
