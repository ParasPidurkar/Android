package com.paras.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.paras.navigation.screens.AgentDetailScreen
import com.paras.navigation.screens.AgentSelectScreen
import com.paras.navigation.screens.CareerScreen
import com.paras.navigation.screens.LobbyScreen
import com.paras.navigation.screens.MapPickScreen
import com.paras.navigation.screens.MatchScreen

/**
 * The whole navigation setup lives in this one file.
 *
 *   NavController  = the remote control. Holds the back stack and has navigate() / popBackStack().
 *   NavHost        = the TV screen. Shows whichever destination is on top of the back stack.
 *   composable<R>  = one channel. "When route R is on top, draw this."
 *
 * Screens never see the NavController. They get plain lambdas (onPickMap, onLockIn, ...),
 * so they stay easy to preview and test, and all navigation rules stay in one place.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValoNavApp() {
    // 1. Create ONE NavController and remember it. It survives recomposition and rotation.
    val navController = rememberNavController()

    // Observe the back stack so the top bar updates whenever we navigate.
    val currentEntry by navController.currentBackStackEntryAsState()
    val destination = currentEntry?.destination
    val canGoBack = navController.previousBackStackEntry != null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleFor(destination), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    if (canGoBack) {
                        // navigateUp() = the ← in the toolbar. popBackStack() = the system Back.
                        // Inside a single app they do the same thing.
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Career) {
                            // Tapping Career while already on Career must not stack a 2nd copy.
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Filled.Person, contentDescription = "Career")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        // 2. NavHost: link the controller to a graph and say where to start.
        NavHost(
            navController = navController,
            startDestination = Lobby,
            modifier = Modifier.padding(innerPadding),
        ) {

            composable<Lobby> { entry ->
                // Read the result MapPick left for us (see MapPick below). Using a StateFlow
                // means the Lobby redraws as soon as a value arrives.
                val pickedMap by entry.savedStateHandle
                    .getStateFlow<String?>(PICKED_MAP_KEY, null)
                    .collectAsState()

                LobbyScreen(
                    selectedMap = pickedMap,
                    onPickMap = { navController.navigate(MapPick) },
                    onSelectAgent = { map -> navController.navigate(AgentSelect(map)) },
                )
            }

            composable<MapPick> {
                MapPickScreen(
                    maps = ValoData.maps,
                    onMapPicked = { map ->
                        // 3. RETURNING A RESULT: write into the PREVIOUS entry's SavedStateHandle,
                        //    then pop ourselves off. Lobby reads it from its own handle.
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(PICKED_MAP_KEY, map)
                        navController.popBackStack()
                    },
                )
            }

            composable<AgentSelect> { entry ->
                // 4. ARGUMENTS: toRoute() rebuilds the data class from the back stack entry.
                val route = entry.toRoute<AgentSelect>()
                AgentSelectScreen(
                    map = route.map,
                    agents = ValoData.agents,
                    onAgentClick = { agent ->
                        navController.navigate(AgentDetail(agentId = agent.id, map = route.map))
                    },
                )
            }

            composable<AgentDetail> { entry ->
                val route = entry.toRoute<AgentDetail>()
                AgentDetailScreen(
                    agent = ValoData.agent(route.agentId),
                    map = route.map,
                    onLockIn = {
                        navController.navigate(Match(route.agentId, route.map)) {
                            // 5. CLEARING THE STACK: once you lock in there is no going back to
                            //    agent select. Remove everything above Lobby (Lobby itself stays,
                            //    because inclusive = false), then push Match.
                            //    Stack before: Lobby, AgentSelect, AgentDetail
                            //    Stack after:  Lobby, Match
                            popUpTo<Lobby> { inclusive = false }
                        }
                    },
                )
            }

            composable<Match> { entry ->
                val route = entry.toRoute<Match>()
                MatchScreen(
                    agent = ValoData.agent(route.agentId),
                    map = route.map,
                    // 6. Pop straight back to a known destination, however deep we are.
                    onBackToLobby = { navController.popBackStack<Lobby>(inclusive = false) },
                )
            }

            composable<Career> {
                CareerScreen()
            }
        }
    }
}

/** hasRoute<T>() asks "is this destination the one declared with composable<T>?" */
private fun titleFor(destination: NavDestination?): String = when {
    destination == null -> ""
    destination.hasRoute<Lobby>() -> "LOBBY"
    destination.hasRoute<MapPick>() -> "CHOOSE MAP"
    destination.hasRoute<AgentSelect>() -> "AGENT SELECT"
    destination.hasRoute<AgentDetail>() -> "AGENT"
    destination.hasRoute<Match>() -> "MATCH"
    destination.hasRoute<Career>() -> "CAREER"
    else -> ""
}
