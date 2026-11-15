package com.paras.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Sends a RESULT back to the screen that opened it.
 *
 * The screen only reports "this map was picked" through onMapPicked. The actual "send result
 * and close" logic (write to previousBackStackEntry.savedStateHandle, then popBackStack) is in
 * ValoNavApp. So this screen could be reused anywhere a map needs picking.
 */
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
