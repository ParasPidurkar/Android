package com.paras.deeplinks

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.util.Consumer
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.paras.deeplinks.screens.AgentDetailScreen
import com.paras.deeplinks.screens.AgentSelectScreen
import com.paras.deeplinks.screens.CareerScreen
import com.paras.deeplinks.screens.LobbyScreen
import com.paras.deeplinks.screens.MapPickScreen
import com.paras.deeplinks.screens.MatchScreen

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
 *
 * DEEP LINKS (what this project adds on top of 26_navigation)
 * A deep link is a link that opens one screen of the app directly, e.g.
 *     valo://app/agent/sova/Haven  -> opens the Sova screen for the map Haven.
 * The link can come from a browser, a chat message, a notification, or adb.
 * Look for "DEEP LINKS" below. There are 2 parts:
 *   PART 1: make sure the NavController receives the link.
 *   PART 2: say which link opens which screen.
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
    // DEEP LINKS, PART 1: give the link to the NavController
    // ----------------------------------------------------------------------------------------
    // A link arrives as an Intent (action = VIEW, data = the link).
    // The NavController must see that Intent. There are 2 cases:
    //
    // A) The app was CLOSED.
    //    Android starts MainActivity with the link inside it.
    //    We do nothing: the NavController reads the link by itself and opens the right screen.
    //    (It also remembers it already did this, so rotating the phone won't open it again.)
    //
    // B) The app was already OPEN.
    //    The manifest has launchMode="singleTop", so Android does NOT start a second
    //    MainActivity. It gives the new Intent to the existing one (onNewIntent).
    //    But the NavController isn't told about it! Without the code below, the app would
    //    just come to the front and ignore the link.
    //    So we listen for new Intents and pass each one to navController.handleDeepLink().
    //
    // What handleDeepLink() does:
    //   - Checks if the link matches one of the navDeepLink patterns (PART 2).
    //     No match -> does nothing. So passing every Intent is safe.
    //   - If it matches, it opens the screen with Lobby under it: [Lobby, AgentDetail].
    //     So Back goes to the Lobby instead of closing the app.
    //   - Links from adb or a browser RESTART the app with a fresh back stack.
    //     So anything open before (like the map picked on the Lobby) is gone.
    //   - Special case: if another app opens our link inside ITS OWN window (task), the
    //     back stack is only [AgentDetail]. Then:
    //        Back -> returns to the other app.
    //        Up ← -> opens our app on the Lobby.
    //     That's why the ← arrow (below) shows on every screen except the Lobby.
    //
    // DisposableEffect = "do this when the screen appears, and undo it when it goes away".
    // We add the listener, and remove it in onDispose so the Activity doesn't keep an old
    // NavController around (a memory leak).
    //
    // LocalContext.current is our Activity, because setContent { } was called on it.
    val activity = LocalContext.current as ComponentActivity
    DisposableEffect(navController, activity) {
        val listener = Consumer<Intent> { intent -> navController.handleDeepLink(intent) }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }

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

    // When to show the ← arrow:
    //   - there is a screen below the top one (previousBackStackEntry != null), OR
    //   - we are on any screen except the Lobby.
    //
    // The second rule is for deep links: another app can open us with only [AgentDetail]
    // on the stack (see PART 1). Nothing is below, but ← should still take you to the Lobby.
    // On the Lobby with nothing below, there's nowhere to go up to, so no arrow.
    //
    // Note: previousBackStackEntry doesn't update the UI by itself. It still stays correct
    // because currentEntry (above) already makes this code re-run after every navigation.
    val canGoBack = navController.previousBackStackEntry != null ||
        (destination != null && !destination.hasRoute<Lobby>())

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
                        // (See DEEP LINKS PART 1 above.)
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

            // --------------------------------------------------------------------------------
            // DEEP LINKS, PART 2: which link opens which screen
            // --------------------------------------------------------------------------------
            // Add `deepLinks = listOf(navDeepLink<X>(basePath = "..."))` to a composable<X>,
            // and that link will also open screen X.
            //
            // You only write the START of the link (basePath).
            // Navigation adds the rest from the route's properties:
            //
            //   navDeepLink<Lobby>(basePath = "valo://app/lobby")
            //       -> valo://app/lobby                    (Lobby has no data)
            //   navDeepLink<AgentSelect>(basePath = "valo://app/agents")
            //       -> valo://app/agents/{map}             (AgentSelect has map)
            //   navDeepLink<AgentDetail>(basePath = "valo://app/agent")
            //       -> valo://app/agent/{agentId}/{map}    (AgentDetail has agentId and map)
            //
            // The values from the link become the screen's data. So for
            // valo://app/agent/sova/Haven, toRoute<AgentDetail>() gives
            // AgentDetail(agentId = "sova", map = "Haven"), exactly like a normal navigate().
            // The screen can't tell if it was opened by a tap or by a link.
            //
            // Good to know:
            //   - Every {part} must be in the link. valo://app/agent/jett (no map) matches
            //     nothing, so the app just opens on the Lobby.
            //   - Spaces etc. are decoded: valo://app/agents/Ice%20box -> map = "Ice box".
            //
            // SAFETY: anyone can send us any link, with any values.
            // Always check the values (see agentOrNull in AgentDetail below).
            //
            // TRY IT (with the app closed, and again with it open):
            //   adb shell am start -a android.intent.action.VIEW -d "valo://app/career"
            //   adb shell am start -a android.intent.action.VIEW -d "valo://app/agents/Bind"
            //   adb shell am start -a android.intent.action.VIEW -d "valo://app/agent/sova/Haven"
            //   adb shell am start -a android.intent.action.VIEW -d "valo://app/agent/nobody/Haven"

            // ================================================================================
            // LOBBY: the first screen. It RECEIVES a result from MapPick.
            // Link: valo://app/lobby  (just opens the app on the Lobby, with a fresh stack)
            // ================================================================================
            // `entry` = this screen's own back stack entry (its data, its storage box, ...).
            composable<Lobby>(
                deepLinks = listOf(navDeepLink<Lobby>(basePath = "$DEEP_LINK_BASE/lobby")),
            ) { entry ->
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
            // No link: it only works when the Lobby is below it, waiting for the result.
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
            // Link: valo://app/agents/{map}   e.g. valo://app/agents/Bind
            //
            // The map from the link is not checked: valo://app/agents/Narnia shows "Narnia".
            // That's harmless here (it's only shown as text). In a real app, check it.
            // ================================================================================
            composable<AgentSelect>(
                deepLinks = listOf(navDeepLink<AgentSelect>(basePath = "$DEEP_LINK_BASE/agents")),
            ) { entry ->
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
            // Link: valo://app/agent/{agentId}/{map}   e.g. valo://app/agent/sova/Haven
            //
            // Back stack from the link: [Lobby, AgentDetail].
            // Note: AgentSelect is NOT added in between. A link only adds the first screen
            // (Lobby) + the target screen.
            // ================================================================================
            composable<AgentDetail>(
                deepLinks = listOf(navDeepLink<AgentDetail>(basePath = "$DEEP_LINK_BASE/agent")),
            ) { entry ->
                val route = entry.toRoute<AgentDetail>()

                // Turn the ID back into an Agent HERE, so the screen gets a ready Agent.
                //
                // We use agentOrNull, not agent(): the ID may come from a link that anyone
                // typed, like valo://app/agent/nobody/Haven. Show a message instead of crashing.
                val agent = ValoData.agentOrNull(route.agentId)
                if (agent == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Unknown agent: ${route.agentId}")
                    }
                    // Stop here: the message is the whole screen. The ← arrow still shows,
                    // so the user can go to the Lobby.
                    return@composable
                }

                AgentDetailScreen(
                    agent = agent,
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
            // No link: you should only get here by pressing LOCK IN.
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
            // Link: valo://app/career   Back stack: [Lobby, Career]
            // ================================================================================
            composable<Career>(
                deepLinks = listOf(navDeepLink<Career>(basePath = "$DEEP_LINK_BASE/career")),
            ) {
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
