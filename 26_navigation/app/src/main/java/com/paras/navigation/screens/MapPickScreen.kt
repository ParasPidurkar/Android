package com.paras.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun MapPickScreen(
    maps: List<String>,
    onMapPicked: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Lesson("Tap a map: it is written to the Lobby's SavedStateHandle, then this screen pops itself.") }
        items(maps) { map ->
            ValoRow(title = map, subtitle = "Standard map", onClick = { onMapPicked(map) }, trailing = "✓")
        }
    }
}
