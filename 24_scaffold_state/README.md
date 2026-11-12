# Scaffold State and the Navigation Drawer

`23_scaffold` was about the **slots**: where each part of the screen goes. This project is about the **state** behind those parts. That means things like: is the drawer open? Which snackbar is showing? How far has the top bar scrolled away? What survives when you rotate the phone?

It also adds the one big Material component the last project skipped: the **navigation drawer**.

## The app

The same Valorant squad, now with a drawer:

| Screen | What it teaches |
|---|---|
| **Roster** | Add agents with the FAB and delete them. **UNDO** now survives rotation, which it didn't in 23_scaffold. |
| **State Inspector** | Live values from `DrawerState`, `SnackbarHostState` and `TopAppBarState`, plus buttons that call their functions. |

Open the drawer with the ☰ button, by swiping in from the left edge, or with the buttons on the Inspector.

| File | What's in it |
|---|---|
| `SquadScaffoldState.kt` | **Start here.** A small class that groups all the state objects. |
| `SquadApp.kt` | `ModalNavigationDrawer` wrapping the `Scaffold`, the Back handler, and the rotation-safe UNDO. |
| `screens/InspectorScreen.kt` | Reads every state value live and calls `open()`, `close()`, `snapTo()`. |
| `screens/RosterScreen.kt` | A screen that owns no state. Everything is passed in. |

---

## 1. Where did `ScaffoldState` go?

Older tutorials (Material **2**) write this:

```kotlin
// Material 2, old
val scaffoldState = rememberScaffoldState()
Scaffold(scaffoldState = scaffoldState, drawerContent = { … }) { … }
scaffoldState.drawerState.open()
scaffoldState.snackbarHostState.showSnackbar("Hi")
```

**Material 3 has no `ScaffoldState`.** The drawer isn't part of `Scaffold` anymore either. Each component has its own state object, and you create the ones you need:

| State object | Create it with | Give it to |
|---|---|---|
| `DrawerState` | `rememberDrawerState(DrawerValue.Closed)` | `ModalNavigationDrawer(drawerState = …)` |
| `SnackbarHostState` | `remember { SnackbarHostState() }` | `SnackbarHost(…)` in the `snackbarHost` slot |
| `TopAppBarState` | `rememberTopAppBarState()` | `TopAppBarDefaults.enterAlwaysScrollBehavior(state)` |

You can still group them yourself if you like. That's what `SquadScaffoldState.kt` does:

```kotlin
@Stable
class SquadScaffoldState(
    val drawerState: DrawerState,
    val snackbarHostState: SnackbarHostState,
    val topBarState: TopAppBarState,
    val scope: CoroutineScope,
) {
    fun openDrawer()  { scope.launch { drawerState.open() } }
    fun closeDrawer() { scope.launch { drawerState.close() } }
    fun showMessage(message: String) { scope.launch { snackbarHostState.showSnackbar(message) } }
}

@Composable
fun rememberSquadScaffoldState(): SquadScaffoldState {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val topBarState = rememberTopAppBarState()
    val scope = rememberCoroutineScope()
    return remember(drawerState, snackbarHostState, topBarState, scope) {
        SquadScaffoldState(drawerState, snackbarHostState, topBarState, scope)
    }
}
```

This is called a **state holder**: a plain class plus a `rememberXxx()` function. It's the same pattern Compose itself uses for `rememberDrawerState`, `rememberLazyListState` and so on. It's optional. Three separate variables would work just as well.

## 2. The navigation drawer

### The drawer wraps the Scaffold

```kotlin
ModalNavigationDrawer(
    drawerState = state.drawerState,
    drawerContent = {
        ModalDrawerSheet {
            Text("SQUAD")
            NavigationDrawerItem(
                label = { Text("Roster") },
                selected = screen == Screen.Roster,
                onClick = { screen = Screen.Roster; state.closeDrawer() },
            )
            …
        }
    },
) {
    Scaffold(topBar = { … }) { innerPadding -> … }
}
```

```
ModalNavigationDrawer          ← slides over everything, including the top bar
└── Scaffold                   ← topBar, FAB, snackbarHost, content
    └── RosterScreen / InspectorScreen
```

- `ModalDrawerSheet` gives the drawer its standard width, shape and colour. Put your header and items inside it.
- `NavigationDrawerItem` is one row. It's highlighted when `selected = true`.
- After the user picks an item, **close the drawer yourself**. It doesn't close on its own.
- Other drawers: `DismissibleNavigationDrawer` pushes the content aside instead of covering it. `PermanentNavigationDrawer` is always visible (for tablets).

### Opening the drawer

There are three ways, and the app has all of them:

| How | Code |
|---|---|
| ☰ button in the top bar | `navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { … } }` |
| Swipe in from the left edge | Nothing to write. It works when `gesturesEnabled = true` |
| From code (any button, any event) | `scope.launch { drawerState.open() }` |

`open()` and `close()` are **suspend** functions. They run the slide animation and only return once it's finished, so you need a coroutine. That's why there's `rememberCoroutineScope()`. Because they wait, you can write steps in order:

```kotlin
scope.launch {
    drawerState.open()      // waits until fully open
    delay(1500)
    drawerState.close()     // waits until fully closed
    snackbarHostState.showSnackbar("Done")   // shows after the drawer is gone
}
```

That's the **Open 1.5 s, then report** button on the Inspector.

To jump with no animation, use `drawerState.snapTo(DrawerValue.Open)`. To start the app with the drawer already open, use `rememberDrawerState(DrawerValue.Open)`.

### Reading the drawer's state

| Property | Meaning |
|---|---|
| `currentValue` | `Open` or `Closed`. During a drag it **stays at the old value** until the drawer settles. |
| `targetValue` | Where it's heading. Changes as soon as a drag passes the halfway point. |
| `isOpen` / `isClosed` | Shortcuts for `currentValue == Open` / `Closed` |
| `isAnimationRunning` | `true` while it's sliding |

Drag the drawer halfway with the Inspector open and watch `currentValue` and `targetValue` disagree.

### The Back button

In this version of Material 3, pressing Back with the drawer open **leaves the app** instead of closing the drawer. Fix it with a `BackHandler` that's only enabled while the drawer is open:

```kotlin
BackHandler(enabled = state.drawerState.isOpen) {
    state.closeDrawer()
}
```

### Turning off the swipe

```kotlin
gesturesEnabled = gesturesEnabled || drawerState.isOpen
```

Sometimes you don't want an edge swipe to open the drawer, for example on a screen that has its own horizontal swiping. But if gestures are fully off, the user also can't swipe the drawer **closed** or tap the dark scrim to close it. The `|| drawerState.isOpen` part keeps gestures on whenever the drawer is already open. Try the `gesturesEnabled` switch on the Inspector.

## 3. Hoisting: put state where everyone can reach it

All the state lives at the top of `SquadApp()`, above both the drawer and the Scaffold:

```kotlin
val state = rememberSquadScaffoldState()
var screen by rememberSaveable { mutableStateOf(Screen.Roster) }
val roster = rememberSaveable(saver = RosterSaver) { … }
val listState = rememberLazyListState()
```

Many parts read or change the same values:

- The **drawer** reads `screen` (to highlight an item) and `roster.size` (header), and changes `screen`.
- The **top bar** opens the drawer and shows `screen` as the title.
- The **FAB** and the **Roster** both change `roster`.
- The **Inspector** reads all three state objects.

State has to live in a parent of everything that uses it.

### Why `listState` is hoisted

`rememberLazyListState()` is saveable, so the scroll position survives rotation even inside `RosterScreen`. But switching to the Inspector **removes `RosterScreen` from the composition**, and everything it remembered is thrown away. Coming back would start at the top again.

Creating `listState` in `SquadApp` (which never leaves) and passing it down keeps the scroll position when you switch screens.

## 4. What survives rotation

| State | After rotation | Why |
|---|---|---|
| `DrawerState` | ✅ kept | `rememberDrawerState` uses `rememberSaveable` inside |
| `TopAppBarState` | ✅ kept | `rememberTopAppBarState` uses `rememberSaveable` inside |
| `LazyListState` | ✅ kept | `rememberLazyListState` uses `rememberSaveable` inside |
| `SnackbarHostState` | ❌ lost | Plain `remember`. There is no Saver for it. |
| `screen`, `gesturesEnabled` | ✅ kept | `rememberSaveable` (enum and Boolean are saved automatically) |
| `roster` | ✅ kept | `rememberSaveable` + custom `Saver` (see 23_scaffold) |
| A coroutine that is running | ❌ cancelled | `rememberCoroutineScope` is cancelled when the Activity is destroyed |

### Fixing UNDO on rotation

23_scaffold had a bug. Delete an agent, rotate while the "UNDO" snackbar is showing, and the snackbar disappears. The agent can't be brought back. The code waiting for UNDO was a running coroutine, and the last two rows of the table say both of those are lost.

The fix is to **save the information, not the coroutine**:

```kotlin
@Parcelize
data class RemovedAgent(val agent: Agent, val index: Int) : Parcelable

var pendingUndo by rememberSaveable { mutableStateOf<RemovedAgent?>(null) }

LaunchedEffect(pendingUndo) {
    val removed = pendingUndo ?: return@LaunchedEffect
    val result = snackbarHostState.showSnackbar("${removed.agent.name} removed", actionLabel = "UNDO")
    if (result == SnackbarResult.ActionPerformed) {
        roster.add(removed.index.coerceAtMost(roster.size), removed.agent)
    }
    pendingUndo = null
}

fun removeAgent(agent: Agent) {
    val index = roster.indexOf(agent)
    roster.removeAt(index)
    pendingUndo = RemovedAgent(agent, index)   // this starts the effect above
}
```

After rotation, `pendingUndo` comes back from the Bundle. `LaunchedEffect` runs again in the new Activity and shows the snackbar again, so UNDO still works.

`LaunchedEffect(key)` cancels and restarts when the key changes. So if you delete a second agent before the first snackbar is gone, only the **latest** delete can be undone. Gmail works the same way.

---

## Common mistakes

| Mistake | What you see | Fix |
|---|---|---|
| Looking for `rememberScaffoldState()` / `drawerContent` in M3 | Unresolved reference | Use `ModalNavigationDrawer` + `rememberDrawerState` |
| Calling `drawerState.open()` directly in `onClick` | Compile error: suspend function | `scope.launch { drawerState.open() }` |
| Not closing the drawer after a click | You pick a screen and the drawer stays open | Call `drawerState.close()` in the item's `onClick` |
| No `BackHandler` | Back exits the app while the drawer is open | `BackHandler(enabled = drawerState.isOpen) { … }` |
| `gesturesEnabled = false` with nothing else | Drawer can't be swiped closed, and tapping the scrim does nothing | `gesturesEnabled = enabled \|\| drawerState.isOpen` |
| Checking `currentValue` during a drag | Says `Closed` while the drawer is half open | Use `targetValue` or `isAnimationRunning` |
| `rememberLazyListState()` inside a screen you switch away from | List jumps to the top when you come back | Hoist it above the `when` |
| Saving a running coroutine's job in your head | UNDO lost on rotation | Save the data with `rememberSaveable`, and redo the work in `LaunchedEffect` |

## Try this

1. Open the drawer with ☰, by swiping from the left edge, and with the Inspector buttons. Press Back while it's open.
2. **Inspector:** drag the drawer halfway and hold. Compare `currentValue` and `targetValue`.
3. **Inspector:** turn `gesturesEnabled` off. The edge swipe stops working, but ☰ still opens the drawer, and you can still swipe it closed.
4. Open the drawer and rotate the phone. It's still open.
5. **Roster:** delete an agent and rotate before the snackbar goes away. The UNDO snackbar comes back. Compare with 23_scaffold.
6. **Roster:** scroll down, go to the Inspector through the drawer, then come back. The scroll position is kept.
7. In `SquadScaffoldState.kt`, change the default to `DrawerValue.Open` and run the app.

## Run it

Open this folder in Android Studio, or run `./gradlew installDebug` with a device connected.
