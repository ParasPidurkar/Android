package com.paras.scaffoldstate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paras.scaffoldstate.SquadScaffoldState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shows the live values inside each state object, plus buttons that call their
 * functions. Every value here is backed by Compose state, so the text updates by itself.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InspectorScreen(
    state: SquadScaffoldState,
    gesturesEnabled: Boolean,
    onGesturesEnabledChange: (Boolean) -> Unit,
    innerPadding: PaddingValues,
) {
    val drawer = state.drawerState
    val snackbars = state.snackbarHostState
    val topBar = state.topBarState

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ---- DrawerState ---------------------------------------------------------
        StateCard("DrawerState") {
            ValueRow("currentValue", drawer.currentValue.name)
            ValueRow("targetValue", drawer.targetValue.name)
            ValueRow("isOpen", drawer.isOpen.toString())
            ValueRow("isAnimationRunning", drawer.isAnimationRunning.toString())
            Hint("Drag the drawer in from the left edge and stop halfway: currentValue stays Closed until you let go.")

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // open() animates the drawer open.
                FilledTonalButton(onClick = state::openDrawer) { Text("open()") }
                // snapTo() jumps straight there, with no animation.
                FilledTonalButton(onClick = {
                    state.scope.launch { drawer.snapTo(DrawerValue.Open) }
                }) { Text("snapTo(Open)") }
                // open() and close() are suspend functions: each line waits for the
                // animation to finish before the next one runs.
                FilledTonalButton(onClick = {
                    state.scope.launch {
                        drawer.open()
                        delay(1500)
                        drawer.close()
                        snackbars.showSnackbar("open() and close() each waited for their animation")
                    }
                }) { Text("Open 1.5 s, then report") }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("gesturesEnabled")
                    Hint("Off: swiping from the edge won't open the drawer. The menu button still works.")
                }
                Switch(checked = gesturesEnabled, onCheckedChange = onGesturesEnabledChange)
            }
        }

        // ---- SnackbarHostState ---------------------------------------------------
        StateCard("SnackbarHostState") {
            ValueRow("currentSnackbarData", snackbars.currentSnackbarData?.visuals?.message ?: "null")
            Hint("Queue three and watch them show one at a time.")
            FilledTonalButton(onClick = {
                listOf("Spike planted", "Spike defused", "Round won").forEach(state::showMessage)
            }) { Text("Queue 3 snackbars") }
        }

        // ---- TopAppBarState ------------------------------------------------------
        StateCard("TopAppBarState") {
            ValueRow("heightOffset", "%.0f px".format(topBar.heightOffset))
            ValueRow("heightOffsetLimit", "%.0f px".format(topBar.heightOffsetLimit))
            ValueRow("collapsedFraction", "%.2f".format(topBar.collapsedFraction))
            Hint("Scroll this screen down and up to move the top bar.")
        }

        // ---- What survives rotation ----------------------------------------------
        StateCard("After rotation") {
            ValueRow("drawerState", "kept (saveable)")
            ValueRow("topBarState", "kept (saveable)")
            ValueRow("snackbarHostState", "lost (not saveable)")
            ValueRow("pending UNDO", "kept (we save it)")
            ValueRow("roster", "kept (custom Saver)")
            ValueRow("selected screen", "kept (rememberSaveable)")
            Hint("Open the drawer and rotate: it stays open.")
        }
    }
}

@Composable
private fun StateCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}

@Composable
private fun ValueRow(label: String, value: String) {
    Row {
        Text(label, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
        Text(value, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
