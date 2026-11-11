# Scaffold in Jetpack Compose

Almost every Material screen has the same parts: a bar at the top, sometimes a bar at the bottom, a floating action button, and short pop-up messages (snackbars). `Scaffold` is the Material 3 composable that puts each part in its standard place, so you don't position them yourself.

You don't lay out a `Scaffold`. You fill its **slots**:

```
┌──────────────── topBar ────────────────┐   ← TopAppBar
│                                        │
│                                        │
│          content(innerPadding)         │   ← your screen
│                                        │
│                                 [FAB]  │   ← floatingActionButton
│  [ snackbar ]                          │   ← snackbarHost
└────────────── bottomBar ───────────────┘   ← NavigationBar / BottomAppBar
```

```kotlin
Scaffold(
    topBar = { /* a composable */ },
    bottomBar = { /* a composable */ },
    floatingActionButton = { /* a composable */ },
    snackbarHost = { /* a composable */ },
) { innerPadding ->
    /* your screen content */
}
```

All slots are optional. Leave one out and Scaffold just doesn't reserve space for it.

## The app

A Valorant-style team roster with two tabs:

| Tab | What it teaches |
|---|---|
| **Roster** | All five slots working together. Add agents with the FAB, delete them, and use **UNDO** in the snackbar. Scroll and watch the top bar hide. |
| **Scaffold Lab** | Switches that turn each slot on and off, plus a live read-out of `innerPadding`. Includes a switch that reproduces the most common Scaffold bug. |

| File | What's in it |
|---|---|
| `AgentScaffold.kt` | **Start here.** The `Scaffold` and every slot, with comments. |
| `screens/RosterScreen.kt` | Passing `innerPadding` to a `LazyColumn`, with extra space for the FAB. |
| `screens/LabScreen.kt` | Passing `innerPadding` to a normal `Column`, and the Lab switches. |
| `MainActivity.kt` | `enableEdgeToEdge()` and `setContent`. |
| `ui/theme/Theme.kt` | Dark red and teal colour scheme. |

---

## 1. `topBar`

```kotlin
val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
        TopAppBar(
            title = { Text("ROSTER") },
            actions = { IconButton(onClick = { … }) { Icon(Icons.Filled.Info, null) } },
            scrollBehavior = scrollBehavior,
        )
    },
) { … }
```

- `title`, `navigationIcon` (left, e.g. a back arrow) and `actions` (right-hand icons) are slots of `TopAppBar`.
- Other versions: `CenterAlignedTopAppBar`, `MediumTopAppBar`, `LargeTopAppBar`.
- **Scroll behaviour** makes the bar react when the content scrolls. It needs two things: give `scrollBehavior` to the bar, and add `Modifier.nestedScroll(...)` to the Scaffold so the bar hears the scroll events.

| Behaviour | Effect |
|---|---|
| `pinnedScrollBehavior()` | Bar stays put, only changes colour |
| `enterAlwaysScrollBehavior()` | Hides when you scroll down, comes back as soon as you scroll up (used here) |
| `exitUntilCollapsedScrollBehavior()` | For Medium/Large bars: shrinks down to a small bar |

Top app bar APIs need `@OptIn(ExperimentalMaterial3Api::class)`.

## 2. `bottomBar`

Usually a `NavigationBar` for switching between 3–5 top-level screens:

```kotlin
bottomBar = {
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
```

`selectedTab` is plain state (`rememberSaveable`). The content slot reads it with a `when` to decide which screen to show. In a bigger app you'd use Navigation Compose here, but the Scaffold part stays the same.

## 3. `floatingActionButton`

```kotlin
floatingActionButton = {
    ExtendedFloatingActionButton(
        onClick = ::addAgent,
        icon = { Icon(Icons.Filled.Add, null) },
        text = { Text("Add agent") },
    )
},
floatingActionButtonPosition = FabPosition.End,   // or Center
```

- Use `FloatingActionButton` for an icon only, or `ExtendedFloatingActionButton` for icon + text.
- Scaffold places it above the bottom bar for you.
- You can show it on some screens only. Here it's wrapped in `if (selectedTab == Tab.Roster)`.

## 4. `snackbarHost` and `SnackbarHostState`

There are two pieces, and beginners often mix them up:

| Piece | Job |
|---|---|
| `SnackbarHostState` | The **remote control**. You call `showSnackbar(...)` on it. |
| `SnackbarHost(state)` in the slot | The **place on screen** where the snackbar is drawn. |

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { … }

// in a click handler:
scope.launch {
    val result = snackbarHostState.showSnackbar(
        message = "Jett removed",
        actionLabel = "UNDO",
    )
    if (result == SnackbarResult.ActionPerformed) {
        // user tapped UNDO, so put the agent back
    }
}
```

Why the coroutine? `showSnackbar` is a **suspend** function: it waits until the snackbar is gone and then **returns why**, either `ActionPerformed` (button tapped) or `Dismissed` (timed out or swiped away). That makes "undo" a simple `if`. A click handler isn't a coroutine, so we start one with `rememberCoroutineScope()`.

If you call `showSnackbar` several times quickly, the messages wait in a queue and show one after another.

## 5. `content` and `innerPadding` (the important bit)

The content lambda receives a `PaddingValues`, usually called `innerPadding`. It tells you how much room the bars take up:

- **top** = status bar + `topBar`
- **bottom** = `bottomBar` (or just the system navigation bar if there's no bottom bar)

Scaffold draws your content **full screen, underneath the bars**. If you don't apply `innerPadding`, the first item hides under the top bar and the last item under the bottom bar. Try it: in the app, open **Scaffold Lab** and turn on **Ignore innerPadding**.

How to apply it depends on the content:

```kotlin
// Normal Column/Box: use padding as a modifier
Column(Modifier.padding(innerPadding)) { … }

// LazyColumn: use contentPadding instead
LazyColumn(contentPadding = innerPadding) { … }
```

With `contentPadding`, the list itself still fills the whole screen, so items scroll smoothly **behind** the bars. The padding only makes sure the first and last items can scroll fully into view. With `Modifier.padding`, the list would be cut off at the bar's edge.

### Adding your own padding too

`PaddingValues` can't simply be added together, so take each side and add to it (see `RosterScreen.kt`):

```kotlin
val dir = LocalLayoutDirection.current
LazyColumn(
    contentPadding = PaddingValues(
        start  = innerPadding.calculateStartPadding(dir) + 16.dp,
        top    = innerPadding.calculateTopPadding() + 16.dp,
        end    = innerPadding.calculateEndPadding(dir) + 16.dp,
        bottom = innerPadding.calculateBottomPadding() + 88.dp,  // room for the FAB
    ),
)
```

**The FAB is not part of `innerPadding`.** It floats over the content, so on a list you add extra bottom space yourself, or the FAB covers the last row.

### Edge-to-edge

`MainActivity` calls `enableEdgeToEdge()`, so the app draws behind the status bar and navigation bar. Scaffold already includes those system bars in `innerPadding` (through its `contentWindowInsets` parameter). That's why the Lab still shows a top padding of about 24dp when the top bar is hidden: that's the status bar.

---

## Common mistakes

| Mistake | What you see | Fix |
|---|---|---|
| Ignoring `innerPadding` | Content hidden under the bars (Android Studio even warns: *"Content padding parameter it is not used"*) | `Modifier.padding(innerPadding)` or `contentPadding = innerPadding` |
| `Modifier.padding(innerPadding)` on a `LazyColumn` | List is clipped below the bar instead of scrolling behind it | Use `contentPadding` |
| Forgetting FAB space | FAB covers the last list item | Add extra bottom padding |
| Calling `showSnackbar` directly in `onClick` | Compile error: suspend function outside a coroutine | `scope.launch { … }` with `rememberCoroutineScope()` |
| `scrollBehavior` on the bar but no `nestedScroll` | Bar never collapses | Add `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)` to the Scaffold |
| A Scaffold inside a Scaffold | Padding counted twice, so there are large empty gaps | One Scaffold per screen; inner screens just take the padding |

## Other Scaffold parameters

| Parameter | Use |
|---|---|
| `containerColor` / `contentColor` | Background and default text colour of the screen |
| `contentWindowInsets` | Which system insets go into `innerPadding` (default: system bars) |
| `floatingActionButtonPosition` | `FabPosition.End`, `Center`, `Start`, `EndOverlay` |

Navigation drawers are **not** a Scaffold slot in Material 3. Wrap the Scaffold in a `ModalNavigationDrawer` instead.

## Try this

1. **Roster:** delete an agent, then tap **UNDO**. Delete three quickly and see the snackbars queue.
2. **Roster:** add agents until the list scrolls. Scroll down and the top bar hides. Scroll up a little and it comes back.
3. **Lab:** switch off `topBar` and `bottomBar` one at a time and watch the `innerPadding` numbers.
4. **Lab:** turn on **Ignore innerPadding**, then look at the first line of the Lab and the first agent on the Roster.
5. Change `enterAlwaysScrollBehavior` to `pinnedScrollBehavior` in `AgentScaffold.kt` and compare.

## Run it

Open this folder in Android Studio, or run `./gradlew installDebug` with a device connected.
