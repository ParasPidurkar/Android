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
 * ============================================================================================
 *  THE WHOLE NAVIGATION SETUP LIVES IN THIS ONE FILE. Read it top to bottom.
 * ============================================================================================
 *
 *  The three pieces:
 *
 *    NavController  = the remote control. It OWNS the back stack (an ordered list of
 *                     NavBackStackEntry objects) and has navigate() / popBackStack().
 *
 *    NavHost        = the TV screen. It looks at the TOP entry of the back stack and draws the
 *                     composable registered for that entry's route. Entries below the top are
 *                     NOT drawn (they're kept in memory, but not composed).
 *
 *    composable<R>  = one channel. "When an entry with route R is on top, draw this."
 *
 *  What is a NavBackStackEntry? Think of it as a "mini Activity" for one screen:
 *    - destination      -> which composable<R> block it belongs to
 *    - arguments        -> the route's properties, stored in a Bundle (survive process death)
 *    - savedStateHandle -> a private key/value store for this entry (we use it for results)
 *    - lifecycle        -> RESUMED when on top, CREATED when covered, DESTROYED when popped
 *    - ViewModelStore   -> viewModel() inside composable<R> { } is scoped to THIS entry
 *
 *  Design rule used in this app: screens never see the NavController. They get plain lambdas
 *  (onPickMap, onLockIn, ...). Benefits:
 *    1. Every navigation rule is in this file, so you can read the whole app's flow in one place.
 *    2. Screens can be shown in @Preview and tested without creating a NavController.
 *    3. A screen can be reused in another flow by passing different lambdas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValoNavApp() {
    // ----------------------------------------------------------------------------------------
    // 1. CREATE THE NAVCONTROLLER
    // ----------------------------------------------------------------------------------------
    // rememberNavController() creates ONE NavController and keeps it:
    //   - across recomposition (like remember)
    //   - across rotation and process death (it saves the whole back stack into the Activity's
    //     saved-instance Bundle, like rememberSaveable)
    //
    // Call it exactly once, at the top of the app. If a screen called rememberNavController()
    // again, it would get a brand-new, EMPTY controller that knows nothing about this back stack.
    val navController = rememberNavController()

    // ----------------------------------------------------------------------------------------
    // OBSERVING WHERE WE ARE
    // ----------------------------------------------------------------------------------------
    // currentBackStackEntryAsState() turns "the top entry" into Compose State. Every navigate()
    // or pop changes the top entry, so this function recomposes and the top bar updates.
    //
    // Plain navController.currentBackStackEntry also exists, but it is NOT observable: Compose
    // would not know to redraw when it changes. Always use the ...AsState() version in UI.
    val currentEntry by navController.currentBackStackEntryAsState()
    val destination = currentEntry?.destination

    // previousBackStackEntry = the entry just below the top. If there is none, we're on the
    // start destination (Lobby) and should NOT show a back arrow.
    //
    // previousBackStackEntry itself is not observable either. It only stays correct here because
    // reading currentEntry above already makes this function recompose on every navigation.
    val canGoBack = navController.previousBackStackEntry != null

    // The Scaffold is OUTSIDE the NavHost. That's the big win over "one Activity per screen":
    // the top bar is created once and stays on screen; only the content area below it changes.
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleFor(destination), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    if (canGoBack) {
                        // navigateUp()   = the ← in the toolbar ("go to the parent screen").
                        // popBackStack() = the system Back button ("undo the last navigation").
                        //
                        // Inside a single app they do the same thing: remove the top entry.
                        // They only differ when the app was opened from a DEEP LINK by another
                        // app: Up stays in your app and goes to the logical parent, while Back
                        // returns to the app that opened you. 27_deep_links shows this.
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // The lambda after navigate(...) is a NavOptionsBuilder: it configures
                        // HOW this single navigation behaves.
                        navController.navigate(Career) {
                            // launchSingleTop = true:
                            //   "If the TOP entry already has this route, don't push a new one."
                            //
                            //   Without it:  [Lobby, Career]  -> tap 👤 -> [Lobby, Career, Career]
                            //   With it:     [Lobby, Career]  -> tap 👤 -> [Lobby, Career]
                            //
                            // Note: it only checks the TOP. If Career is deeper in the stack
                            // (e.g. [Lobby, Career, MapPick]), a new Career is still pushed.
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
        // 2. THE NAVHOST AND THE NAVIGATION GRAPH
        // ------------------------------------------------------------------------------------
        // NavHost connects three things:
        //   - navController     : whose back stack to display
        //   - startDestination  : the first entry pushed when the stack is empty (app launch).
        //                         Pressing Back on it finishes the Activity and closes the app.
        //   - the trailing lambda: the NAVIGATION GRAPH, i.e. the list of every screen that
        //                          exists. navigate() to a route not registered here crashes
        //                          with IllegalArgumentException.
        //
        // The graph lambda runs once to BUILD the graph. It is not a normal composable body:
        // the code inside each composable<R> { ... } block is what runs as UI.
        //
        // By default, NavHost cross-fades between screens. You can pass enterTransition /
        // exitTransition / popEnterTransition / popExitTransition to change it.
        NavHost(
            navController = navController,
            startDestination = Lobby,
            modifier = Modifier.padding(innerPadding),
        ) {

            // ================================================================================
            // LOBBY  (start destination, RECEIVES a result)
            // ================================================================================
            // The lambda parameter `entry` is this screen's NavBackStackEntry.
            composable<Lobby> { entry ->
                // RECEIVING A RESULT
                // MapPick writes "picked_map" into THIS entry's savedStateHandle and pops itself
                // (see MapPick below). Here we read it.
                //
                // getStateFlow(key, initialValue) returns a StateFlow that emits whenever that
                // key changes. collectAsState() converts it to Compose State, so when MapPick
                // writes "Ascent", the Lobby recomposes with the new map.
                //
                // Why does this survive rotation and process death? Because savedStateHandle is
                // saved together with the back stack entry. The value stays until Lobby is
                // popped, so "selected map" behaves like persistent screen state.
                //
                // (For a ONE-SHOT event, like "show a snackbar once", you'd call
                //  entry.savedStateHandle.remove<String>(KEY) after handling it. Otherwise it
                //  would fire again after rotation.)
                val pickedMap by entry.savedStateHandle
                    .getStateFlow<String?>(PICKED_MAP_KEY, null)
                    .collectAsState()

                LobbyScreen(
                    selectedMap = pickedMap,
                    // A route with NO arguments is an object, so navigate to the object itself.
                    // Stack: [Lobby] -> [Lobby, MapPick]
                    onPickMap = { navController.navigate(MapPick) },
                    // A route WITH arguments is a data class: create an instance with the values.
                    // Stack: [Lobby] -> [Lobby, AgentSelect(map)]
                    onSelectAgent = { map -> navController.navigate(AgentSelect(map)) },
                )
            }

            // ================================================================================
            // MAP PICK  (SENDS a result back, then pops itself)
            // ================================================================================
            composable<MapPick> {
                MapPickScreen(
                    maps = ValoData.maps,
                    onMapPicked = { map ->
                        // 3. RETURNING A RESULT
                        //
                        // There's no startActivityForResult() between composable screens.
                        // Instead, write into the PREVIOUS entry's savedStateHandle:
                        //
                        //   back stack:  [Lobby, MapPick]
                        //                   ▲       ▲
                        //   previousBackStackEntry  currentBackStackEntry
                        //
                        // ORDER MATTERS: write first, then pop. After popBackStack(), Lobby is
                        // on top and previousBackStackEntry would be null (nothing under Lobby).
                        //
                        // ?. is used because previousBackStackEntry can be null in theory (if
                        // MapPick were ever the start destination).
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(PICKED_MAP_KEY, map)

                        // Remove MapPick from the stack. Its entry is DESTROYED: its lifecycle
                        // ends and any ViewModels scoped to it are cleared.
                        // Stack: [Lobby, MapPick] -> [Lobby]
                        navController.popBackStack()
                    },
                )
            }

            // ================================================================================
            // AGENT SELECT  (RECEIVES an argument)
            // ================================================================================
            composable<AgentSelect> { entry ->
                // 4. READING ARGUMENTS
                //
                // When we called navigate(AgentSelect("Ascent")), Navigation encoded the data
                // class into the entry's arguments Bundle (map = "Ascent").
                // toRoute<AgentSelect>() decodes that Bundle back into an AgentSelect instance.
                //
                // Because it comes from the Bundle, this works after rotation and process
                // death too, with no extra code.
                val route = entry.toRoute<AgentSelect>()

                AgentSelectScreen(
                    map = route.map,
                    agents = ValoData.agents,
                    onAgentClick = { agent ->
                        // Pass the agent's ID, not the Agent object.
                        //   - Arguments live in a Bundle, which must survive process death and
                        //     has a size limit (~1 MB for the whole Activity's saved state).
                        //   - The receiving screen looks the agent up again by id.
                        // This is the same rule as with Intents: putExtra(id), not the object.
                        //
                        // Stack: [Lobby, AgentSelect] -> [Lobby, AgentSelect, AgentDetail]
                        navController.navigate(AgentDetail(agentId = agent.id, map = route.map))
                    },
                )
            }

            // ================================================================================
            // AGENT DETAIL  (two arguments, and clears the stack on LOCK IN)
            // ================================================================================
            composable<AgentDetail> { entry ->
                val route = entry.toRoute<AgentDetail>()
                AgentDetailScreen(
                    // id -> object lookup happens HERE, at the navigation layer, so the screen
                    // just receives a ready-to-use Agent.
                    agent = ValoData.agent(route.agentId),
                    map = route.map,
                    onLockIn = {
                        navController.navigate(Match(route.agentId, route.map)) {
                            // 5. CLEARING THE STACK WITH popUpTo
                            //
                            // popUpTo<Lobby> means: BEFORE pushing Match, pop entries off the
                            // top until Lobby is on top.
                            //
                            //   inclusive = false -> Lobby itself stays.
                            //   inclusive = true  -> Lobby is popped too, so Back from Match
                            //                        would close the app. Use that for
                            //                        Login -> Home ("never go back to login").
                            //
                            // Step by step:
                            //   start:         [Lobby, AgentSelect, AgentDetail]
                            //   popUpTo Lobby: [Lobby]
                            //   push Match:    [Lobby, Match]
                            //
                            // Result: once you lock in, Back can't return to agent select.
                            // The popped entries are DESTROYED, so their ViewModels and saved
                            // state are freed.
                            //
                            // The Intent equivalent needs FLAG_ACTIVITY_CLEAR_TOP plus finish()
                            // calls. Here the resulting stack is visible in one line.
                            popUpTo<Lobby> { inclusive = false }
                        }
                    },
                )
            }

            // ================================================================================
            // MATCH  (jumps back several screens at once)
            // ================================================================================
            composable<Match> { entry ->
                val route = entry.toRoute<Match>()
                MatchScreen(
                    agent = ValoData.agent(route.agentId),
                    map = route.map,
                    // 6. POPPING TO A KNOWN DESTINATION
                    //
                    // popBackStack()          -> remove exactly ONE entry.
                    // popBackStack<Lobby>(..) -> remove entries until Lobby is on top, however
                    //                            many there are. (inclusive = false keeps Lobby.)
                    //
                    // Here the stack is [Lobby, Match], so both behave the same. But if Career
                    // were opened on top ([Lobby, Match, Career]) and Career had this button,
                    // popBackStack<Lobby> would still go straight to the Lobby.
                    //
                    // It returns a Boolean: false if Lobby wasn't found in the stack.
                    //
                    // Note: Lobby's savedStateHandle still holds "picked_map", so the lobby
                    // still shows the map you chose earlier.
                    onBackToLobby = { navController.popBackStack<Lobby>(inclusive = false) },
                )
            }

            // ================================================================================
            // CAREER  (no arguments, opened with launchSingleTop from the top bar)
            // ================================================================================
            composable<Career> {
                CareerScreen()
            }
        }
    }
}

/**
 * Picks the top-bar title for whatever destination is on top.
 *
 * hasRoute<T>() asks "was this destination declared with composable<T>?". It compares the route
 * TYPE, not the argument values, so AgentDetail("jett", ...) and AgentDetail("sova", ...) both
 * match hasRoute<AgentDetail>().
 *
 * Use the same check to highlight the selected item in a bottom navigation bar.
 */
private fun titleFor(destination: NavDestination?): String = when {
    // null for a moment before the NavHost has pushed the start destination.
    destination == null -> ""
    destination.hasRoute<Lobby>() -> "LOBBY"
    destination.hasRoute<MapPick>() -> "CHOOSE MAP"
    destination.hasRoute<AgentSelect>() -> "AGENT SELECT"
    destination.hasRoute<AgentDetail>() -> "AGENT"
    destination.hasRoute<Match>() -> "MATCH"
    destination.hasRoute<Career>() -> "CAREER"
    else -> ""
}
