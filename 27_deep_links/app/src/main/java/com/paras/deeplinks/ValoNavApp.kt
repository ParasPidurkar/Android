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
    // DEEP LINKS, PART 1: getting the Intent to the NavController
    // ----------------------------------------------------------------------------------------
    // A deep link reaches us as an Intent: action = VIEW, data = the URI
    // (e.g. valo://app/agent/jett/Ascent). The manifest's intent-filter decides that Android
    // sends it to MainActivity. From there, the NavController has to SEE the Intent before it
    // can open a screen. There are two ways it arrives:
    //
    // (a) COLD START: the app wasn't running, or MainActivity was destroyed.
    //     Android creates MainActivity with the deep-link Intent as activity.intent.
    //     Nothing to write here: when NavHost hands the graph to the NavController (setGraph),
    //     the NavController checks activity.intent by itself, calls handleDeepLink(intent), and
    //     if the URI matches a navDeepLink in the graph, opens that screen INSTEAD of pushing
    //     only the start destination.
    //     It also remembers "deep link already handled" in its saved state, so a rotation
    //     (which re-runs this code with the same activity.intent) won't open the screen again.
    //
    // (b) WARM START: MainActivity is already on screen / in the background.
    //     The manifest says launchMode="singleTop", so Android REUSES the existing Activity and
    //     calls onNewIntent(intent) on it. activity.intent is NOT replaced, and the
    //     NavController is never told. If we did nothing, the link would just bring the app to
    //     the front and silently do nothing.
    //     So we listen for new Intents and pass each one to handleDeepLink() ourselves.
    //
    // WHAT handleDeepLink() DOES (Navigation 2.8, read from the library):
    //   1. Matches intent.data against every navDeepLink in the graph. No match -> returns
    //      false and nothing happens (non-deep-link Intents are therefore harmless).
    //   2. Builds the "deep link stack": the graph's start destination + the matched screen,
    //      i.e. [Lobby, AgentDetail]. Then it picks one of three strategies by Intent flags:
    //
    //      FLAG_ACTIVITY_NEW_TASK, without CLEAR_TASK   (adb, browsers, most other apps)
    //          -> adds CLEAR_TASK, restarts MainActivity in a fresh task, finish()es this one.
    //             The restarted Activity then hits the next case. You may see a short flicker,
    //             and anything that was on the old stack (e.g. Lobby's picked map) is gone.
    //
    //      NEW_TASK + CLEAR_TASK
    //          -> pops the whole back stack and pushes Lobby, then the target:
    //             stack = [Lobby, AgentDetail]. Back goes to Lobby, then closes the app.
    //
    //      no NEW_TASK  (another app's Activity called startActivity(VIEW uri) directly, so
    //                    MainActivity was started INSIDE THAT APP'S TASK)
    //          -> the target replaces everything: stack = [AgentDetail] only.
    //             Back  -> finishes MainActivity and returns to the app that sent the link.
    //             Up    -> navigateUp() sees "only one screen + launched from a deep link" and
    //                      relaunches our app in OUR OWN task at its start destination (Lobby).
    //             That's the real difference between Up and Back, and why the top bar below
    //             shows the ← arrow on any screen that isn't Lobby.
    //
    // WHY A LISTENER, NOT override fun onNewIntent() IN MainActivity?
    // The NavController lives here, inside Compose. ComponentActivity lets anyone register an
    // OnNewIntent listener, so the Activity doesn't need a reference to the NavController.
    //
    // WHY DisposableEffect?
    // A listener registered on the Activity must be removed again, or the Activity would keep
    // a reference to an old NavController (a leak, and a stale controller handling links).
    // DisposableEffect runs its block when it enters composition and onDispose when it leaves
    // (or when a key changes). Keys = navController and activity: if either changed, the old
    // listener is removed and a new one registered for the new pair.
    //
    // LocalContext.current is the Activity here because setContent { } was called on it.
    val activity = LocalContext.current as ComponentActivity
    DisposableEffect(navController, activity) {
        // androidx.core.util.Consumer<Intent>: a one-method callback, (Intent) -> Unit.
        // handleDeepLink returns true/false (handled or not); we don't need the result.
        val listener = Consumer<Intent> { intent -> navController.handleDeepLink(intent) }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }

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

    // When should the top bar show the ← (Up) arrow?
    //
    //   previousBackStackEntry != null -> there is a screen below us to go up to.
    //
    //   destination isn't Lobby        -> covers the DEEP LINK case where the stack is only
    //                                     [AgentDetail] (opened inside another app's task, see
    //                                     DEEP LINKS PART 1). There's nothing below, but Up
    //                                     still has a meaning: "go to this app's home". Then
    //                                     navigateUp() relaunches the app at Lobby.
    //
    // On Lobby with nothing below it, there's no "up" left, so no arrow.
    //
    // previousBackStackEntry itself is not observable. It only stays correct here because
    // reading currentEntry above already makes this function recompose on every navigation.
    val canGoBack = navController.previousBackStackEntry != null ||
        (destination != null && !destination.hasRoute<Lobby>())

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
                        // returns to the app that opened you.
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
            // --------------------------------------------------------------------------------
            // DEEP LINKS, PART 2: which URL opens which screen
            // --------------------------------------------------------------------------------
            // Any composable<R> can take a `deepLinks` list. Each entry says "a URI of this
            // shape ALSO opens this screen". A screen without deepLinks can only be reached by
            // navigate() from inside the app (MapPick and Match below, on purpose).
            //
            // navDeepLink<R>(basePath = "...") is the TYPE-SAFE version (Navigation 2.8+).
            // You give only the start of the URL. Navigation reads R's @Serializable fields
            // and appends them using the SAME rules it uses to build the route itself:
            //
            //   required field (no default, not nullable) -> path segment   /{field}
            //   field with a default value, or nullable    -> query param    ?field={field}
            //
            //   navDeepLink<Lobby>(basePath = "valo://app/lobby")        -> valo://app/lobby
            //   navDeepLink<AgentSelect>(basePath = "valo://app/agents") -> valo://app/agents/{map}
            //   navDeepLink<AgentDetail>(basePath = "valo://app/agent")  -> valo://app/agent/{agentId}/{map}
            //
            // If AgentDetail had `val skin: String = "default"`, the pattern would become
            // valo://app/agent/{agentId}/{map}?skin={skin}, and a URL without ?skin= would
            // still match (skin = "default").
            //
            // MATCHING RULES worth knowing:
            //   - Scheme and host must match exactly ("valo", "app").
            //   - Every {placeholder} must be present. valo://app/agent/jett (no map) matches
            //     nothing, so handleDeepLink() returns false and the app just opens normally.
            //   - Values are URL-decoded: valo://app/agents/Ice%20box gives map = "Ice box".
            //   - Values are converted to the field's type. An Int field given "abc" won't match.
            //
            // HOW THE VALUES REACH THE SCREEN
            // The matched placeholders are put into the new entry's arguments Bundle, exactly
            // as navigate(AgentDetail("jett", "Ascent")) would. So toRoute<AgentDetail>() works
            // unchanged, and the screen can't tell whether it was opened by a tap or a URL.
            //
            // SECURITY: the Activity is exported, so ANY app (or a web page) can send these
            // URLs with ANY values. Treat deep-link arguments as untrusted user input: validate
            // them (see the agentOrNull check in AgentDetail) and never deep-link straight to a
            // screen that does something destructive or skips a confirmation.
            //
            // TRY IT (app installed, running or not):
            //   adb shell am start -a android.intent.action.VIEW -d "valo://app/career"
            //   adb shell am start -a android.intent.action.VIEW -d "valo://app/agents/Bind"
            //   adb shell am start -a android.intent.action.VIEW -d "valo://app/agent/sova/Haven"
            //   adb shell am start -a android.intent.action.VIEW -d "valo://app/agent/nobody/Haven"
            //
            // ================================================================================
            // valo://app/lobby  -> Lobby
            // Since Lobby is the start destination, this mostly means "open the app, and throw
            // away whatever stack it had" (the NEW_TASK path in PART 1 rebuilds it from scratch).
            composable<Lobby>(
                deepLinks = listOf(navDeepLink<Lobby>(basePath = "$DEEP_LINK_BASE/lobby")),
            ) { entry ->
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
            // No deepLinks: MapPick only makes sense when Lobby is underneath waiting for the
            // result. Opened from a URL, there'd be no previousBackStackEntry to write to.
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
            composable<AgentSelect>(
                // valo://app/agents/{map}  e.g. valo://app/agents/Bind -> AgentSelect(map = "Bind")
                //
                // `map` is not validated: valo://app/agents/Narnia shows the agent list for
                // "Narnia". That's harmless here (it's only displayed), but in a real app you'd
                // check it against ValoData.maps, just like agentId is checked below.
                deepLinks = listOf(navDeepLink<AgentSelect>(basePath = "$DEEP_LINK_BASE/agents")),
            ) { entry ->
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
            composable<AgentDetail>(
                // valo://app/agent/{agentId}/{map}
                //   e.g. valo://app/agent/sova/Haven -> AgentDetail(agentId = "sova", map = "Haven")
                // Resulting stack: [Lobby, AgentDetail]. Back goes to Lobby, NOT to AgentSelect:
                // the deep link stack is only "start destination + target". Navigation can't know
                // that AgentSelect "should" be in between, because nothing in the graph says so.
                // (Put AgentSelect and AgentDetail in a nested graph, and that graph's start
                // destination would be added too.)
                deepLinks = listOf(navDeepLink<AgentDetail>(basePath = "$DEEP_LINK_BASE/agent")),
            ) { entry ->
                val route = entry.toRoute<AgentDetail>()
                // id -> object lookup happens HERE, at the navigation layer, so the screen
                // just receives a ready-to-use Agent.
                //
                // agentOrNull, not agent(): this screen is deep-linkable, so agentId may be
                // anything a user typed in a URL. Show a message instead of crashing.
                val agent = ValoData.agentOrNull(route.agentId)
                if (agent == null) {
                    // return@composable leaves this block early: the error text is the whole
                    // screen. The top bar still shows ← so the user can go Up to the Lobby.
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Unknown agent: ${route.agentId}")
                    }
                    return@composable
                }
                AgentDetailScreen(
                    agent = agent,
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
            // No deepLinks: a match result should only be reachable by actually locking in.
            // If it were linkable, anyone could open valo://app/match/jett/Ascent.
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
            composable<Career>(
                // valo://app/career -> Career. Stack: [Lobby, Career].
                deepLinks = listOf(navDeepLink<Career>(basePath = "$DEEP_LINK_BASE/career")),
            ) {
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
