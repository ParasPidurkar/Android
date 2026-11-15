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
 * ALL the navigation of the app is in this one file. Read it from top to bottom.
 *
 * The 3 main pieces:
 *
 *   NavController  = the REMOTE CONTROL.
 *                    It keeps the list of open screens (the "back stack") and has the buttons:
 *                    navigate() to open a screen, popBackStack() to close one.
 *
 *   NavHost        = the TV.
 *                    It shows the screen that is on TOP of the back stack. Nothing else.
 *
 *   composable<X>  = one CHANNEL.
 *                    "When screen X is on top, draw this UI."
 *
 * The back stack is just a list. The last item is the screen you see:
 *
 *     [Lobby]                         -> you see Lobby
 *     [Lobby, AgentSelect]            -> you see AgentSelect
 *     [Lobby, AgentSelect, Agent]     -> you see Agent. Back removes it.
 *
 * Each item in the list is a "NavBackStackEntry". Think of it as one open screen with its own:
 *   - arguments        -> the data it was opened with (e.g. map = "Ascent")
 *   - savedStateHandle -> a small box to store values in (we use it to send results)
 *   - lifecycle        -> knows if it's on top, hidden below, or closed
 *   - ViewModels       -> a viewModel() created inside the screen belongs to this entry
 *
 * One rule in this app: screens never get the NavController.
 * They only get simple lambdas like onPickMap = { ... }. This file decides what they do.
 * Why?
 *   1. All navigation is in one place, easy to read.
 *   2. Screens can be previewed and tested without a NavController.
 *   3. The same screen can be reused in a different flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValoNavApp() {
    // ----------------------------------------------------------------------------------------
    // 1. CREATE THE NAVCONTROLLER
    // ----------------------------------------------------------------------------------------
    // rememberNavController() creates the remote control ONCE and keeps it.
    // It survives recomposition, screen rotation, and even the app being killed in the
    // background (it saves the whole back stack and restores it).
    //
    // Create it only here, at the top. If a screen called rememberNavController() again, it
    // would get a NEW, EMPTY remote control that knows nothing about our screens.
    val navController = rememberNavController()

    // ----------------------------------------------------------------------------------------
    // WHICH SCREEN ARE WE ON?
    // ----------------------------------------------------------------------------------------
    // currentEntry = the screen on top of the back stack.
    // "AsState" means: when the top screen changes, this code runs again (recomposes),
    // so the top bar title and the back arrow update automatically.
    //
    // (navController.currentBackStackEntry also exists, but Compose isn't told when it changes,
    //  so the UI would not update. In UI code, always use the ...AsState() version.)
    val currentEntry by navController.currentBackStackEntryAsState()

    // destination = WHICH screen is on top (Lobby? AgentDetail?), without its data.
    // currentEntry = one open screen WITH its data (e.g. AgentDetail for "jett").
    // We only need "which screen" to pick the title, so we use destination.
    // It can be null for a split second at app start, before Lobby is opened.
    val destination = currentEntry?.destination

    // previousBackStackEntry = the screen just BELOW the top one.
    // If there is none, we are on the first screen (Lobby), so we hide the back arrow.
    //
    // Note: this value doesn't update the UI by itself. It still stays correct because
    // currentEntry (above) already makes this code re-run after every navigation.
    val canGoBack = navController.previousBackStackEntry != null

    // The Scaffold (top bar + content) is OUTSIDE the NavHost.
    // So the top bar is created once and stays. Only the content area below it changes.
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleFor(destination), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    if (canGoBack) {
                        // navigateUp()   = the ← arrow in the top bar: "go to the parent screen".
                        // popBackStack() = the phone's Back button: "close the top screen".
                        //
                        // Inside our app both do the same thing: remove the top screen.
                        // They are only different when ANOTHER app opened us with a link:
                        // Up stays in our app, Back goes back to the other app.
                        // 27_deep_links shows this.
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // The { } after navigate(...) holds OPTIONS for this one navigation.
                        navController.navigate(Career) {
                            // launchSingleTop = true means:
                            // "If Career is ALREADY the top screen, don't open it again."
                            //
                            //   Without it: [Lobby, Career] -> tap 👤 -> [Lobby, Career, Career]
                            //   With it:    [Lobby, Career] -> tap 👤 -> [Lobby, Career]
                            //
                            // It only looks at the TOP screen. If Career is lower in the list,
                            // a new one is still opened.
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

        // ------------------------------------------------------------------------------------
        // 2. THE NAVHOST: the list of all screens
        // ------------------------------------------------------------------------------------
        // navController    = which remote control to follow.
        // startDestination = the first screen when the app opens (Lobby).
        //                    Pressing Back on it closes the app.
        // the { } block    = every screen that exists in the app.
        //                    Calling navigate() to a screen that is NOT listed here crashes.
        //
        // By default, screens fade into each other. You can change this with
        // enterTransition / exitTransition (see the README).
        NavHost(
            navController = navController,
            startDestination = Lobby,
            modifier = Modifier.padding(innerPadding),
        ) {

            // ================================================================================
            // LOBBY: the first screen. It RECEIVES a result from MapPick.
            // ================================================================================
            // `entry` = this screen's own back stack entry (its data, its storage box, ...).
            composable<Lobby> { entry ->
                // RECEIVING A RESULT
                // MapPick puts the chosen map into Lobby's savedStateHandle (its storage box)
                // under the key "picked_map", then closes itself.
                //
                // Here we WATCH that key:
                //   getStateFlow(key, null) -> gives a value that changes when the key changes
                //   collectAsState()        -> turns it into Compose state, so the UI updates
                //
                // So when MapPick writes "Ascent", the Lobby redraws and shows "ASCENT".
                //
                // The value is saved with the screen, so it survives rotation. It stays until
                // the Lobby is closed.
                //
                // (For a one-time event, like showing a message once, you would remove the
                //  value after using it: entry.savedStateHandle.remove<String>(KEY).
                //  Otherwise it would show again after rotation.)
                val pickedMap by entry.savedStateHandle
                    .getStateFlow<String?>(PICKED_MAP_KEY, null)
                    .collectAsState()

                LobbyScreen(
                    selectedMap = pickedMap,
                    // Screen with NO data -> navigate to the object itself.
                    // Back stack: [Lobby] -> [Lobby, MapPick]
                    onPickMap = { navController.navigate(MapPick) },
                    // Screen WITH data -> create the class with the values.
                    // Back stack: [Lobby] -> [Lobby, AgentSelect("Ascent")]
                    onSelectAgent = { map -> navController.navigate(AgentSelect(map)) },
                )
            }

            // ================================================================================
            // MAP PICK: SENDS a result back to Lobby, then closes itself.
            // ================================================================================
            composable<MapPick> {
                MapPickScreen(
                    maps = ValoData.maps,
                    onMapPicked = { map ->
                        // 3. SENDING A RESULT BACK
                        //
                        // There is no startActivityForResult() here. Instead we write the value
                        // into the storage box of the screen BELOW us (the Lobby):
                        //
                        //   back stack:  [Lobby, MapPick]
                        //                   ▲       ▲
                        //   previousBackStackEntry  currentBackStackEntry
                        //
                        // ORDER MATTERS: write first, then close.
                        // After popBackStack(), Lobby is on top and there is nothing below it,
                        // so previousBackStackEntry would be null and the write would be lost.
                        //
                        // ?. = "only if it's not null", just to be safe.
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(PICKED_MAP_KEY, map)

                        // Close MapPick. It is removed and destroyed (its ViewModels too).
                        // Back stack: [Lobby, MapPick] -> [Lobby]
                        navController.popBackStack()
                    },
                )
            }

            // ================================================================================
            // AGENT SELECT: RECEIVES one argument (map).
            // ================================================================================
            composable<AgentSelect> { entry ->
                // 4. READING ARGUMENTS
                //
                // We opened this screen with navigate(AgentSelect("Ascent")).
                // toRoute<AgentSelect>() gives us that AgentSelect object back, so route.map
                // is "Ascent".
                //
                // The data is saved with the screen, so this also works after rotation or
                // after Android kills and restores the app. No extra code needed.
                val route = entry.toRoute<AgentSelect>()

                AgentSelectScreen(
                    map = route.map,
                    agents = ValoData.agents,
                    onAgentClick = { agent ->
                        // Send only the agent's ID, not the whole Agent object.
                        //   - Arguments are saved in a Bundle, which must be small and simple.
                        //   - The next screen finds the agent again with the ID.
                        // Same rule as with Intents: putExtra(id), not the whole object.
                        //
                        // Back stack: [Lobby, AgentSelect] -> [Lobby, AgentSelect, AgentDetail]
                        navController.navigate(AgentDetail(agentId = agent.id, map = route.map))
                    },
                )
            }

            // ================================================================================
            // AGENT DETAIL: two arguments. LOCK IN clears the screens in between.
            // ================================================================================
            composable<AgentDetail> { entry ->
                val route = entry.toRoute<AgentDetail>()
                AgentDetailScreen(
                    // Turn the ID back into an Agent HERE, so the screen gets a ready Agent.
                    agent = ValoData.agent(route.agentId),
                    map = route.map,
                    onLockIn = {
                        navController.navigate(Match(route.agentId, route.map)) {
                            // 5. CLEARING SCREENS WITH popUpTo
                            //
                            // popUpTo<Lobby> means: BEFORE opening Match, close every screen
                            // above Lobby.
                            //
                            //   inclusive = false -> Lobby itself stays.
                            //   inclusive = true  -> Lobby is closed too. Then Back from Match
                            //                        would close the app. (Useful for
                            //                        Login -> Home: never go back to login.)
                            //
                            // Step by step:
                            //   start:          [Lobby, AgentSelect, AgentDetail]
                            //   popUpTo Lobby:  [Lobby]
                            //   open Match:     [Lobby, Match]
                            //
                            // Result: after LOCK IN, Back can't return to agent select.
                            popUpTo<Lobby> { inclusive = false }
                        }
                    },
                )
            }

            // ================================================================================
            // MATCH: goes back to Lobby in one step.
            // ================================================================================
            composable<Match> { entry ->
                val route = entry.toRoute<Match>()
                MatchScreen(
                    agent = ValoData.agent(route.agentId),
                    map = route.map,
                    // 6. GOING BACK TO A SPECIFIC SCREEN
                    //
                    // popBackStack()          -> close ONE screen.
                    // popBackStack<Lobby>(..) -> close screens until Lobby is on top,
                    //                            no matter how many. (inclusive = false keeps Lobby.)
                    //
                    // Here the stack is [Lobby, Match], so both do the same.
                    // But with [Lobby, Match, Career], popBackStack<Lobby> would still jump
                    // straight to Lobby.
                    //
                    // The Lobby still shows the map you picked, because its storage box
                    // was never cleared.
                    onBackToLobby = { navController.popBackStack<Lobby>(inclusive = false) },
                )
            }

            // ================================================================================
            // CAREER: no arguments. Opened from the 👤 icon with launchSingleTop.
            // ================================================================================
            composable<Career> {
                CareerScreen()
            }
        }
    }
}

/**
 * Picks the top bar title for the screen on top.
 *
 * hasRoute<X>() asks: "is this the X screen?"
 * It checks only the screen TYPE, not the data. So AgentDetail for "jett" and AgentDetail
 * for "sova" both match hasRoute<AgentDetail>().
 *
 * You'd use the same check to highlight the selected tab in a bottom bar.
 */
private fun titleFor(destination: NavDestination?): String = when {
    // null for a split second at app start, before Lobby is opened.
    destination == null -> ""
    destination.hasRoute<Lobby>() -> "LOBBY"
    destination.hasRoute<MapPick>() -> "CHOOSE MAP"
    destination.hasRoute<AgentSelect>() -> "AGENT SELECT"
    destination.hasRoute<AgentDetail>() -> "AGENT"
    destination.hasRoute<Match>() -> "MATCH"
    destination.hasRoute<Career>() -> "CAREER"
    else -> ""
}
