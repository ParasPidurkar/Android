package com.paras.scaffoldstate

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paras.scaffoldstate.screens.InspectorScreen
import com.paras.scaffoldstate.screens.RosterScreen
import kotlinx.parcelize.Parcelize

/** The destinations listed in the drawer. */
enum class Screen(val label: String, val icon: ImageVector) {
    Roster("Roster", Icons.Filled.Person),
    Inspector("State Inspector", Icons.Filled.Info),
}

@Parcelize
data class Agent(val name: String, val role: String) : Parcelable

/** A deleted agent and where it was, so UNDO can put it back in the same place. */
@Parcelize
data class RemovedAgent(val agent: Agent, val index: Int) : Parcelable

private val agentPool = listOf(
    Agent("Jett", "Duelist"), Agent("Sova", "Initiator"), Agent("Sage", "Sentinel"),
    Agent("Omen", "Controller"), Agent("Phoenix", "Duelist"), Agent("Killjoy", "Sentinel"),
    Agent("Breach", "Initiator"), Agent("Viper", "Controller"), Agent("Reyna", "Duelist"),
    Agent("Cypher", "Sentinel"), Agent("Skye", "Initiator"), Agent("Brimstone", "Controller"),
)

/** Same Saver as 23_scaffold: SnapshotStateList <-> ArrayList for the Bundle. */
private val RosterSaver = Saver<SnapshotStateList<Agent>, ArrayList<Agent>>(
    save = { roster -> ArrayList(roster) },
    restore = { saved -> saved.toMutableStateList() },
)

/**
 * Layering, from the outside in:
 *
 *   ModalNavigationDrawer        <- owns the drawer, slides over EVERYTHING
 *   └── Scaffold                 <- topBar, FAB, snackbarHost, content
 *       └── RosterScreen / InspectorScreen
 *
 * In Material 3 the drawer is not a Scaffold slot. It wraps the Scaffold, which is
 * why it covers the top bar too.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadApp() {
    // ---- All the state, hoisted to the top so every part can share it ----------
    val state = rememberSquadScaffoldState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state.topBarState)

    var screen by rememberSaveable { mutableStateOf(Screen.Roster) }
    var gesturesEnabled by rememberSaveable { mutableStateOf(true) }
    val roster = rememberSaveable(saver = RosterSaver) { agentPool.take(6).toMutableStateList() }

    // Hoisted here, not inside RosterScreen. RosterScreen leaves the composition when
    // you switch to the Inspector, and any state it remembered would be thrown away.
    // Up here, the scroll position is still there when you come back.
    val listState = rememberLazyListState()

    // ---- UNDO that survives rotation -------------------------------------------
    // In 23_scaffold, the coroutine waiting for UNDO lived only in memory. Rotating
    // killed it, so the snackbar vanished and the delete could no longer be undone.
    //
    // Fix: save *what* can be undone (a Parcelable) instead of relying on a running
    // coroutine. After rotation, pendingUndo comes back from the Bundle and the
    // LaunchedEffect below starts again and shows the snackbar again.
    var pendingUndo by rememberSaveable { mutableStateOf<RemovedAgent?>(null) }

    LaunchedEffect(pendingUndo) {
        val removed = pendingUndo ?: return@LaunchedEffect
        val result = state.snackbarHostState.showSnackbar(
            message = "${removed.agent.name} removed",
            actionLabel = "UNDO",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) {
            roster.add(removed.index.coerceAtMost(roster.size), removed.agent)
        }
        pendingUndo = null
    }

    fun addAgent() {
        val next = agentPool.firstOrNull { it !in roster } ?: return
        roster.add(next)
        state.showMessage("${next.name} joined the team")
    }

    fun removeAgent(agent: Agent) {
        val index = roster.indexOf(agent)
        roster.removeAt(index)
        // Changing the key restarts the LaunchedEffect. Only the latest delete can be undone.
        pendingUndo = RemovedAgent(agent, index)
    }

    fun select(target: Screen) {
        screen = target
        state.closeDrawer()
    }

    // The system Back button should close an open drawer instead of leaving the app.
    // Only enabled while the drawer is open, so normally Back works as usual.
    BackHandler(enabled = state.drawerState.isOpen) {
        state.closeDrawer()
    }

    ModalNavigationDrawer(
        drawerState = state.drawerState,
        // With gestures off you can't swipe the drawer open from the edge. Once it IS
        // open, keep gestures on so swiping or tapping the dark scrim can still close it.
        gesturesEnabled = gesturesEnabled || state.drawerState.isOpen,
        drawerContent = {
            SquadDrawerSheet(
                current = screen,
                agentCount = roster.size,
                onSelect = ::select,
            )
        },
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = { Text(screen.label.uppercase(), fontWeight = FontWeight.Bold) },
                    // The usual way to open a drawer: a menu ("hamburger") icon.
                    navigationIcon = {
                        IconButton(onClick = state::toggleDrawer) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open drawer")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                if (screen == Screen.Roster) {
                    ExtendedFloatingActionButton(
                        onClick = ::addAgent,
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text("Add agent") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            },
            snackbarHost = { SnackbarHost(state.snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            when (screen) {
                Screen.Roster -> RosterScreen(
                    roster = roster,
                    listState = listState,
                    innerPadding = innerPadding,
                    onRemove = ::removeAgent,
                )
                Screen.Inspector -> InspectorScreen(
                    state = state,
                    gesturesEnabled = gesturesEnabled,
                    onGesturesEnabledChange = { gesturesEnabled = it },
                    innerPadding = innerPadding,
                )
            }
        }
    }
}

/** What slides in from the left. ModalDrawerSheet gives it the right width, shape and colour. */
@Composable
private fun SquadDrawerSheet(
    current: Screen,
    agentCount: Int,
    onSelect: (Screen) -> Unit,
) {
    ModalDrawerSheet {
        Text(
            "SQUAD",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 28.dp, top = 24.dp, end = 28.dp),
        )
        Text(
            "$agentCount agents on the team",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        Screen.entries.forEach { target ->
            NavigationDrawerItem(
                label = { Text(target.label) },
                icon = { Icon(target.icon, contentDescription = null) },
                selected = target == current,
                onClick = { onSelect(target) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        }
    }
}
