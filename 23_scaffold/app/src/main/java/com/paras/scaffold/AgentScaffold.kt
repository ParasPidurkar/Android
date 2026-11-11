package com.paras.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import com.paras.scaffold.screens.LabScreen
import com.paras.scaffold.screens.LabSettings
import com.paras.scaffold.screens.LabSettingsSaver
import com.paras.scaffold.screens.RosterScreen
import kotlinx.coroutines.launch

/** The two destinations shown in the bottom bar. */
enum class Tab(val label: String, val icon: ImageVector) {
    Roster("Roster", Icons.Filled.Person),
    Lab("Scaffold Lab", Icons.Filled.Settings),
}

data class Agent(val name: String, val role: String)

private val agentPool = listOf(
    Agent("Jett", "Duelist"), Agent("Sova", "Initiator"), Agent("Sage", "Sentinel"),
    Agent("Omen", "Controller"), Agent("Phoenix", "Duelist"), Agent("Killjoy", "Sentinel"),
    Agent("Breach", "Initiator"), Agent("Viper", "Controller"), Agent("Reyna", "Duelist"),
    Agent("Cypher", "Sentinel"), Agent("Skye", "Initiator"), Agent("Brimstone", "Controller"),
)

/**
 * The whole screen is ONE Scaffold. Each named parameter below is a "slot":
 * you hand Scaffold a composable and it decides where to place it.
 *
 *   ┌──────────── topBar ────────────┐
 *   │                                │
 *   │      content(innerPadding)     │
 *   │                         [FAB]  │
 *   │  [snackbarHost]                │
 *   └────────── bottomBar ───────────┘
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScaffold() {
    // ---- State the slots share -------------------------------------------------
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Roster) }
    var lab by rememberSaveable(stateSaver = LabSettingsSaver) { mutableStateOf(LabSettings()) }
    val roster = remember { mutableStateListOf(*agentPool.take(6).toTypedArray()) }

    // A snackbar is shown by calling a *suspend* function, so we need a coroutine scope.
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Lets the top bar collapse when the content scrolls up and come back on scroll down.
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    fun addAgent() {
        val next = agentPool.firstOrNull { it !in roster } ?: return
        roster.add(next)
        scope.launch {
            snackbarHostState.showSnackbar("${next.name} joined the team")
        }
    }

    fun removeAgent(agent: Agent) {
        val index = roster.indexOf(agent)
        roster.removeAt(index)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "${agent.name} removed",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short,
            )
            // showSnackbar suspends until the snackbar goes away and tells us why.
            if (result == SnackbarResult.ActionPerformed) {
                roster.add(index.coerceAtMost(roster.size), agent)
            }
        }
    }

    Scaffold(
        // Connect the content's scrolling to the top bar's scroll behaviour.
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        // ---- Slot 1: topBar ------------------------------------------------------
        topBar = {
            if (lab.showTopBar) {
                TopAppBar(
                    title = { Text(selectedTab.label.uppercase(), fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("This bar is Scaffold's topBar slot")
                            }
                        }) {
                            Icon(Icons.Filled.Info, contentDescription = "About this bar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    scrollBehavior = scrollBehavior,
                )
            }
        },

        // ---- Slot 2: bottomBar ---------------------------------------------------
        bottomBar = {
            if (lab.showBottomBar) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = tab == selectedTab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },

        // ---- Slot 3: floatingActionButton ----------------------------------------
        floatingActionButton = {
            if (lab.showFab && selectedTab == Tab.Roster) {
                ExtendedFloatingActionButton(
                    onClick = ::addAgent,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add agent") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        floatingActionButtonPosition = if (lab.centerFab) FabPosition.Center else FabPosition.End,

        // ---- Slot 4: snackbarHost ------------------------------------------------
        // The host is the *place* snackbars appear. The state object decides *what* shows.
        snackbarHost = { SnackbarHost(snackbarHostState) },

        containerColor = MaterialTheme.colorScheme.background,

        // ---- Slot 5: content -----------------------------------------------------
    ) { innerPadding ->
        // innerPadding = the space taken by topBar, bottomBar and the system bars.
        // Apply it, or your content slides underneath them.
        val padding = if (lab.ignoreInnerPadding) PaddingValues() else innerPadding

        when (selectedTab) {
            Tab.Roster -> RosterScreen(
                roster = roster,
                contentPadding = padding,
                onRemove = ::removeAgent,
            )
            Tab.Lab -> LabScreen(
                settings = lab,
                onSettingsChange = { lab = it },
                innerPadding = innerPadding,
                contentPadding = padding,
            )
        }
    }
}
