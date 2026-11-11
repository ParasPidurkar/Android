package com.paras.scaffold.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** The switches on the Lab screen. Each one changes how AgentScaffold fills its slots. */
data class LabSettings(
    val showTopBar: Boolean = true,
    val showBottomBar: Boolean = true,
    val showFab: Boolean = true,
    val centerFab: Boolean = false,
    val ignoreInnerPadding: Boolean = false,
)

/** Saves LabSettings across rotation as a plain list of Booleans (see 22_parcelable_state). */
val LabSettingsSaver = listSaver<LabSettings, Boolean>(
    save = { listOf(it.showTopBar, it.showBottomBar, it.showFab, it.centerFab, it.ignoreInnerPadding) },
    restore = { LabSettings(it[0], it[1], it[2], it[3], it[4]) },
)

/**
 * A playground: flip a switch, watch the Scaffold rearrange itself, and read the
 * live innerPadding values to see where they come from.
 *
 * Normal (non-lazy) content takes Scaffold's padding through Modifier.padding.
 */
@Composable
fun LabScreen(
    settings: LabSettings,
    onSettingsChange: (LabSettings) -> Unit,
    innerPadding: PaddingValues,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "⬆ If you can read this line, the top padding is working.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        PaddingReadout(innerPadding)

        LabSwitch(
            title = "topBar",
            explain = "Hide it and innerPadding.top shrinks to just the status bar.",
            checked = settings.showTopBar,
            onChange = { onSettingsChange(settings.copy(showTopBar = it)) },
        )
        LabSwitch(
            title = "bottomBar",
            explain = "Hide it and innerPadding.bottom shrinks to just the navigation bar. " +
                "(You stay on this screen, so you can turn it back on.)",
            checked = settings.showBottomBar,
            onChange = { onSettingsChange(settings.copy(showBottomBar = it)) },
        )
        LabSwitch(
            title = "floatingActionButton",
            explain = "Shown on the Roster tab only. The FAB never changes innerPadding.",
            checked = settings.showFab,
            onChange = { onSettingsChange(settings.copy(showFab = it)) },
        )
        LabSwitch(
            title = "FabPosition.Center",
            explain = "floatingActionButtonPosition: End (default) or Center.",
            checked = settings.centerFab,
            onChange = { onSettingsChange(settings.copy(centerFab = it)) },
        )
        LabSwitch(
            title = "Ignore innerPadding  ⚠",
            explain = "The classic bug. Content starts at the very top of the screen and " +
                "hides under the bars. Check the first line of this screen and the first " +
                "agent on the Roster tab.",
            checked = settings.ignoreInnerPadding,
            onChange = { onSettingsChange(settings.copy(ignoreInnerPadding = it)) },
        )

        Text(
            "⬇ If you can read this line, the bottom padding is working.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun PaddingReadout(innerPadding: PaddingValues) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("innerPadding right now", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "top    = ${innerPadding.calculateTopPadding()}\n" +
                    "bottom = ${innerPadding.calculateBottomPadding()}",
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "top = status bar + topBar,  bottom = bottomBar (or the navigation bar)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LabSwitch(title: String, explain: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text(title, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text(
                    explain,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}
