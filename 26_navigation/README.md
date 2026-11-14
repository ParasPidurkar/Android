# Navigation: NavHost and NavController

Up to `25_scaffold_types`, every "screen change" was an `if` or a `when` on a state variable. That's fine for two tabs, but it breaks down quickly. There is no Back button support, no history, and no clean way to pass data between screens.

**Navigation Compose** fixes that. You get a real back stack, the system Back button just works, and you can pass arguments and return results.

## The app

A Valorant match flow:

```
Lobby ──Choose map──▶ Choose Map ──(result: "Ascent")──▶ back to Lobby
  │
  └─Select agent──▶ Agent Select(map) ──tap──▶ Agent(agentId, map) ──LOCK IN──▶ Match
                                                                                  │
  ◀───────────────────────── Back to lobby (popBackStack<Lobby>) ─────────────────┘

👤 icon in the top bar (from any screen) ──▶ Career   (launchSingleTop)
```

| Screen | What it teaches |
|---|---|
| **Lobby** | Start destination. Receives a **result** from Choose Map. |
| **Choose Map** | Sends a result back with `SavedStateHandle`, then pops itself. |
| **Agent Select** | Receives an **argument** (`map`). |
| **Agent** | Two arguments. **LOCK IN** uses `popUpTo` to clear the stack. |
| **Match** | `popBackStack<Lobby>()` jumps back several screens at once. |
| **Career** | `launchSingleTop`, so tapping 👤 twice doesn't stack two copies. |

| File | What's in it |
|---|---|
| `ValoNavApp.kt` | **Start here.** `NavController`, `NavHost`, every `navigate()` call. |
| `Routes.kt` | One `@Serializable` object or class per screen. |
| `ValoData.kt` | Agents and maps. |
| `screens/*.kt` | The screens. None of them knows the `NavController` exists. |

---

## 1. The three pieces

| Piece | Think of it as | Job |
|---|---|---|
| `NavController` | The remote control | Holds the **back stack**. `navigate()` pushes, `popBackStack()` pops. |
| `NavHost` | The TV | Shows whatever is on top of the back stack. |
| `composable<Route> { }` | A channel | "When this route is on top, draw this composable." |

```kotlin
val navController = rememberNavController()      // 1. one controller, remembered

NavHost(navController, startDestination = Lobby) {  // 2. the host + where to start
    composable<Lobby>   { LobbyScreen(...) }         // 3. one entry per screen
    composable<MapPick> { MapPickScreen(...) }
    ...
}
```

`rememberNavController()` survives recomposition **and rotation**. Rotate on the Agent screen and you stay on the Agent screen with the same back stack.

## 2. Routes: type-safe (Navigation 2.8+)

Older tutorials use strings: `"agentDetail/{agentId}"`, plus `navArgument(...)` and `getString("agentId")`. It's easy to make typos, and nothing checks them.

Since 2.8, **a route is just a Kotlin type**:

```kotlin
@Serializable object Lobby                                          // no arguments
@Serializable data class AgentDetail(val agentId: String, val map: String)  // arguments = properties
```

That needs the serialization plugin and library (see `build.gradle.kts` and `app/build.gradle.kts`).

## 3. Navigating and passing arguments

```kotlin
// go
navController.navigate(AgentDetail(agentId = "jett", map = "Ascent"))

// read on the other side
composable<AgentDetail> { entry ->
    val route = entry.toRoute<AgentDetail>()   // AgentDetail("jett", "Ascent")
    AgentDetailScreen(agent = ValoData.agent(route.agentId), map = route.map, ...)
}
```

**Pass ids, not objects.** Arguments are stored in a `Bundle` and have to survive process death. Send `"jett"` and look the agent up on the other side. Don't send the whole `Agent`.

## 4. Going back

| Call | Does |
|---|---|
| `navController.popBackStack()` | Remove the top screen. Same as the system Back button. |
| `navController.navigateUp()` | The ← in the toolbar. In a single-activity app it behaves the same. |
| `navController.popBackStack<Lobby>(inclusive = false)` | Pop **everything above** Lobby. Lobby stays. |

The ← only appears when `navController.previousBackStackEntry != null`, so the start screen has none.

## 5. `popUpTo`: clearing the stack while navigating

Once you lock in an agent, Back shouldn't return to agent select:

```kotlin
navController.navigate(Match(agentId, map)) {
    popUpTo<Lobby> { inclusive = false }
}
```

```
before:  [Lobby, AgentSelect, AgentDetail]
after:   [Lobby, Match]
```

With `inclusive = true`, Lobby would be removed too and Back from Match would close the app. That's what you want for screens like Login → Home.

## 6. `launchSingleTop`

```kotlin
navController.navigate(Career) { launchSingleTop = true }
```

If Career is **already on top**, nothing is pushed. Without it, tapping 👤 three times means pressing Back three times to leave.

## 7. Sending a result back

There's no `startActivityForResult` here. Instead, the screen writes into the **previous** entry's `SavedStateHandle`:

```kotlin
// MapPick: send
navController.previousBackStackEntry?.savedStateHandle?.set(PICKED_MAP_KEY, map)
navController.popBackStack()

// Lobby: receive
val pickedMap by entry.savedStateHandle
    .getStateFlow<String?>(PICKED_MAP_KEY, null)
    .collectAsState()
```

The value lives in Lobby's back stack entry, so it also survives rotation.

## 8. Reading where you are

```kotlin
val currentEntry by navController.currentBackStackEntryAsState()
currentEntry?.destination?.hasRoute<Lobby>()   // true when Lobby is on top
```

The top bar title uses this. It's also how you'd highlight the selected item in a bottom bar.

## 9. Keep the NavController out of your screens

Screens take lambdas:

```kotlin
LobbyScreen(
    selectedMap = pickedMap,
    onPickMap = { navController.navigate(MapPick) },
    onSelectAgent = { map -> navController.navigate(AgentSelect(map)) },
)
```

so all the navigation rules live in one file (`ValoNavApp.kt`), and the screens can be previewed and tested without a NavController.

---

# Going deeper

Sections 1–9 are what you type. The rest explains what Navigation does underneath, so that the behaviour you see makes sense and you can predict it.

## 10. The back stack is a list of `NavBackStackEntry`

The back stack isn't a list of screens or composables. It's a list of **`NavBackStackEntry`** objects. One entry is created every time you `navigate()`, and it's destroyed when it's popped.

Each entry holds:

| Inside an entry | What it's for |
|---|---|
| `destination` | Which `composable<…>` block it belongs to. `hasRoute<T>()` checks this. |
| `arguments` | A `Bundle` with the route's properties. `toRoute<T>()` reads it back into your data class. |
| `savedStateHandle` | A key-value store that belongs only to this entry. Used for results (section 7). |
| A `Lifecycle` | Like an Activity's lifecycle, but per screen. |
| A `ViewModelStore` | `viewModel()` called inside `composable<T> { }` gets a ViewModel that belongs to *this entry*. |

So every screen behaves like a small Activity, with its own lifecycle, saved state and ViewModels, but they all live inside one real Activity.

Walk through the app and watch the stack:

```
start                        [Lobby]
tap "Choose map"             [Lobby, MapPick]
tap Ascent                   [Lobby]                     MapPick entry destroyed. "Ascent" was written into Lobby's handle first.
tap "Select agent"           [Lobby, AgentSelect(Ascent)]
tap Jett                     [Lobby, AgentSelect(Ascent), AgentDetail(jett, Ascent)]
LOCK IN (popUpTo<Lobby>)     [Lobby, Match(jett, Ascent)]  AgentSelect and AgentDetail both destroyed.
tap 👤                        [Lobby, Match(...), Career]
tap 👤 again                  [Lobby, Match(...), Career]   launchSingleTop: Career is already on top, so nothing happens.
Back                         [Lobby, Match(...)]
"Back to lobby"              [Lobby]
Back                         []  → the Activity finishes and the app closes.
```

The same route can appear more than once. `navigate(AgentDetail("jett", "Ascent"))` twice in a row gives you two separate entries with separate state, unless you use `launchSingleTop`.

## 11. Entry lifecycle: what happens to the screens underneath

The `NavHost` only *composes* the top entry (plus the outgoing one while the transition animation runs). The entries below it still exist in the back stack, but their composables are not running.

| Where the entry is | Lifecycle state | Composable |
|---|---|---|
| On top, transition finished | `RESUMED` | Composed and visible |
| Animating in or out | `STARTED` | Composed |
| Below the top | `CREATED` | **Not** composed |
| Popped | `DESTROYED` | Gone. Its ViewModels get `onCleared()`. |

This has a consequence you will hit sooner or later:

```kotlin
composable<AgentSelect> {
    var search by remember { mutableStateOf("") }          // LOST when you navigate forward and come back
    var search by rememberSaveable { mutableStateOf("") }  // KEPT
}
```

When you go `AgentSelect → AgentDetail`, the AgentSelect composable leaves composition, so plain `remember` values are thrown away. `NavHost` wraps each entry in a `SaveableStateHolder`, so **`rememberSaveable` values are stored per entry** and come back when you press Back. Scroll position (`rememberLazyListState()`) uses `rememberSaveable` internally, which is why lists keep their scroll position.

Rule of thumb: anything the user typed or chose on a screen goes in `rememberSaveable` or a ViewModel, never plain `remember`.

## 12. ViewModels are scoped to the entry

```kotlin
composable<AgentDetail> {
    val vm: AgentDetailViewModel = viewModel()   // owned by THIS back stack entry
}
```

- It's created the first time the entry is composed.
- It survives rotation, and it survives navigating forward and back.
- It's cleared when the entry is **popped**. After LOCK IN, the `popUpTo` clears it.
- The ViewModel's own `SavedStateHandle` already contains the route arguments, so you can call `savedStateHandle.toRoute<AgentDetail>()` inside the ViewModel instead of passing the id in.

If two screens need to share one ViewModel (for example, a multi-step sign-up flow), put them in a nested graph (section 17) and scope the ViewModel to the graph's entry:

```kotlin
val parent = remember(entry) { navController.getBackStackEntry<SignUpFlow>() }
val sharedVm: SignUpViewModel = viewModel(parent)
```

## 13. How a type-safe route becomes a "URL"

Navigation was originally built on string routes, and underneath it still is. Your `@Serializable` class is converted into a route pattern using its serializer:

```
@Serializable data class AgentDetail(val agentId: String, val map: String)
        ↓  pattern registered by composable<AgentDetail>
com.paras.navigation.AgentDetail/{agentId}/{map}
        ↓  navigate(AgentDetail("jett", "Ascent"))
com.paras.navigation.AgentDetail/jett/Ascent
```

- Required properties (no default, not nullable) become **path** segments: `/{agentId}`.
- Properties with a default value or a nullable type become **query** parameters: `?map={map}`.
- `toRoute<AgentDetail>()` runs this in reverse: it reads the `Bundle` and decodes it into your data class.

This is why `@Serializable` is required. Without the serialization plugin, there is nothing to generate the pattern or decode the bundle.

Supported argument types out of the box: `String`, `Int`, `Long`, `Float`, `Double`, `Boolean`, their nullable versions, and some lists and arrays of them. For anything else you must write a custom `NavType` and pass a `typeMap`. Usually that's a sign you should be passing an id instead.

## 14. Process death: why "pass ids, not objects" matters

Android can kill your app in the background to save memory. When the user returns:

1. A new process starts and `MainActivity` is recreated with its saved `Bundle`.
2. `rememberNavController()` restores the **back stack** from that bundle: every entry, its arguments, its `savedStateHandle` and its `rememberSaveable` values.
3. Only the top entry is composed. It calls `toRoute()` and gets `AgentDetail("jett", "Ascent")` back.

Anything that wasn't in a bundle (static objects you created at runtime, in-memory caches, plain `remember`) is gone. If you'd passed a whole `Agent` object, it would have to be serialised into the bundle, and bundles have a size limit of roughly 1 MB for the whole Activity. Pass `"jett"` and look it up again (from `ValoData`, a database or a repository).

Test it: open the Agent screen, press Home, run `adb shell am kill com.paras.navigation`, then reopen the app from Recents. You land back on the Agent screen with the same stack.

## 15. The result pattern in detail

```
MapPick                                     Lobby
───────                                     ─────
previousBackStackEntry                      entry.savedStateHandle
  .savedStateHandle["picked_map"] = "Ascent"  .getStateFlow("picked_map", null)  ──▶ "Ascent"
popBackStack()
```

- MapPick doesn't call Lobby. It writes into **Lobby's** entry, which it reaches through `previousBackStackEntry`. Write **before** `popBackStack()`. After the pop, `previousBackStackEntry` points at something else.
- The value stays in Lobby's handle until Lobby is popped. That's what you want here, because "selected map" is state.
- For a **one-shot event** (for example "show a snackbar once"), remove it after you read it: `savedStateHandle.remove<String>(KEY)`. Otherwise it fires again after rotation.
- Values must be bundle-safe types (String, Int, Parcelable, …) because the handle is saved across process death.
- `previousBackStackEntry` is whatever is underneath, which isn't always the screen you expect. If a screen can be opened from several places, the result goes to whichever one opened it. Usually that's correct.

## 16. Pitfalls

**Don't call `navigate()` directly in a composable's body.**

```kotlin
composable<Splash> {
    navController.navigate(Lobby)                      // ❌ runs on EVERY recomposition
    LaunchedEffect(Unit) { navController.navigate(Lobby) { popUpTo<Splash> { inclusive = true } } }  // ✅ runs once
}
```

Navigate from event callbacks (`onClick`) or from a `LaunchedEffect`.

**Double taps push two screens.** Tap "Select agent" twice quickly and you can get two AgentSelect entries, because the first tap starts a transition and the button is still on screen. Fix it by only handling clicks when the entry is `RESUMED`:

```kotlin
// lifecycle-runtime-compose 2.8+
onPickMap = dropUnlessResumed { navController.navigate(MapPick) }
```

**`popBackStack()` on the start destination.** It returns `false` and leaves the NavHost empty, which shows a blank screen. The system Back button handles this correctly by finishing the Activity. Only call `popBackStack()` yourself when you know something is underneath.

**Creating the NavController in the wrong place.** Call `rememberNavController()` once, at the top. If you call it inside a screen, that screen gets a second, empty controller.

**Reading `previousBackStackEntry` for UI.** It isn't observable state. In this app it updates only because `currentBackStackEntryAsState()` triggers a recomposition on every navigation. Read the observable value first, as `ValoNavApp` does.

## 17. Patterns this app doesn't use (yet)

### Nested graphs

Group related screens under one route. You can then pop the whole flow at once and share ViewModels within it.

```kotlin
@Serializable object MatchFlow

NavHost(navController, startDestination = Lobby) {
    composable<Lobby> { ... }
    navigation<MatchFlow>(startDestination = MapPick) {
        composable<MapPick> { ... }
        composable<AgentSelect> { ... }
    }
}

navController.navigate(MatchFlow)                      // enters at MapPick
navController.popBackStack<MatchFlow>(inclusive = true) // leaves the whole flow
```

### Bottom navigation bar

Each tab should keep its own history, and switching tabs shouldn't pile up entries. That needs three options together:

```kotlin
navController.navigate(tab.route) {
    popUpTo(navController.graph.findStartDestination().id) { saveState = true }  // pop to the root, but SAVE the tab's stack
    launchSingleTop = true                                                       // no duplicates when re-tapping
    restoreState = true                                                          // bring back the saved stack of the tab you switch to
}
```

To highlight the selected tab, use `currentEntry?.destination?.hierarchy?.any { it.hasRoute(tab.route::class) }`. `hierarchy` also matches when you're on a screen nested inside that tab.

### Deep links

A URL can open a screen directly, and Navigation builds the back stack for it:

```kotlin
composable<AgentDetail>(
    deepLinks = listOf(navDeepLink<AgentDetail>(basePath = "valo://agent"))
) { ... }
```

You also need a matching `<intent-filter>` in `AndroidManifest.xml`. This is where `navigateUp()` and `popBackStack()` behave differently: if your app was opened from another app's deep link, **Up** goes to your app's parent screen, while **Back** returns to the app that opened you.

### Transitions

By default, `NavHost` cross-fades between screens. You can override this:

```kotlin
NavHost(
    navController, startDestination = Lobby,
    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
    exitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
    popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) },
    popExitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) },
)
```

`pop*` transitions are used when going back, so a screen can slide in from the right going forward and from the left going back.

## 18. Debugging the back stack

Paste this into `ValoNavApp` to log the whole stack on every change:

```kotlin
LaunchedEffect(navController) {
    navController.currentBackStack.collect { stack ->
        Log.d("Nav", stack.joinToString(" > ") { it.destination.route ?: "graph" })
    }
}
```

The first item is the `NavGraph` itself. It always sits at the bottom of the stack, which is why you see one extra entry. Then filter Logcat by `Nav` and try the steps below.

## 19. Mental model, in one paragraph

The `NavController` owns an ordered list of entries. Each entry pairs a destination with its arguments and gives it a private lifecycle, ViewModel store and saved state. `navigate()` adds an entry (after optionally popping some with `popUpTo`), Back removes the top one, and the `NavHost` composes only whatever is on top. Everything else follows from that: arguments live in the entry's bundle, results are written into another entry's handle, state underneath survives only if it's saveable, and the whole list is saved into the Activity's bundle so it can be rebuilt after process death.

---

## Try it

1. Choose a map, then press Back on the Lobby. The app closes, because Lobby is the start destination.
2. Choose map → Ascent. Rotate the phone. Ascent is still selected.
3. Select agent → Jett → LOCK IN, then press **system Back**. You go to the Lobby, not the agent screen (`popUpTo`).
4. Tap 👤 three times, then Back once. You've left Career (`launchSingleTop`).
5. Remove `launchSingleTop = true` and repeat step 4. Now you need three Backs.

## Run

```
./gradlew installDebug
```

To build an APK you can copy to a phone:

```
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```
