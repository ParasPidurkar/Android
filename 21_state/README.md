# State in Programming and Android

## 1. What is "state"?

**State is the data a program remembers at a given moment, which decides what it does or shows next.**

If you could pause a running program and write down every value it holds (variables, objects, open connections, what's on screen), that snapshot is its **state**.

```kotlin
var count = 0          // state
fun increment() {
    count++            // changes state
}
```

Each call to `increment()` returns a different result because `count` (the state) changed. A function with **no** state always gives the same output for the same input. That's a *pure* or *stateless* function:

```kotlin
fun add(a: Int, b: Int) = a + b   // stateless: no remembered data
```

### Stateful vs. stateless

| Stateless | Stateful |
|---|---|
| Output depends only on input | Output depends on input **and** stored data |
| Easy to test and reason about | Harder: you must know the current state |
| `fun square(x: Int) = x * x` | `class Counter { var value = 0 }` |

## 2. General kinds of state

| Kind | Description | Example |
|---|---|---|
| **Local state** | Owned by one function, object, or component | A loop counter, a text field's current text |
| **Shared state** | Read or written by several parts of the program | A shopping cart used by multiple screens |
| **Global state** | Accessible from anywhere (use sparingly) | Singletons, static variables |
| **Derived state** | Computed from other state, not stored separately | `isCartEmpty = items.isEmpty()` |
| **Transient state** | Lives in memory and is lost when the program stops | Scroll position, an animation in progress |
| **Persistent state** | Saved to disk or network and survives restarts | Database rows, user settings, files |

### Mutable vs. immutable state

- **Mutable** (`var`, `MutableList`) can be changed in place. It's flexible but error-prone, especially when multiple threads touch it.
- **Immutable** (`val`, `List`, `data class` + `copy()`) can't change. You create a new value instead, which is safer and easier to track.

```kotlin
data class User(val name: String, val age: Int)

val u1 = User("Paras", 28)
val u2 = u1.copy(age = 29)   // new state; u1 is unchanged
```

### Common state-related problems

- **Race conditions**: two threads modify the same state at once.
- **Stale state**: the UI shows old data after the underlying data changed.
- **Inconsistent state**: two copies of the same data disagree.
- **Lost state**: data disappears unexpectedly, such as after a crash or restart.

These problems are why most modern architectures keep a **single source of truth** and treat state as immutable.

## 3. State in Android

In Android, state is **any value that can change over time** and that the app shows or relies on: text a user typed, a list loaded from the network, whether a dialog is open, the logged-in user, and so on.

Android makes state harder to manage than in a plain program because **the system can destroy and recreate your UI at any time**.

### 3.1 Why Android state is tricky: the lifecycle

An `Activity` or `Fragment` moves through lifecycle states:

```
onCreate → onStart → onResume → (running) → onPause → onStop → onDestroy
```

Your UI objects can be destroyed in three situations:

| Event | What happens | Is in-memory state lost? |
|---|---|---|
| **Configuration change** (rotation, dark mode, language, window resize) | The Activity is destroyed and recreated | Yes, unless you save it |
| **System-initiated process death** (app in background, low memory) | The whole process is killed and restored later when the user returns | Yes, unless you save it |
| **User dismisses** (Back button, swipe away from recents) | The Activity finishes for good | Yes, and that's expected |

So "where do I keep this value so it survives?" is one of the most important questions in Android development.

### 3.2 Where to keep state

| Mechanism | Survives config change | Survives process death | Good for |
|---|---|---|---|
| Plain variable in Activity/Composable | ❌ | ❌ | Truly throwaway values |
| `remember { }` (Compose) | ❌ | ❌ | Short-lived UI state within a composition |
| `ViewModel` | ✅ | ❌ | Screen state, loaded data |
| `onSaveInstanceState` / `rememberSaveable` | ✅ | ✅ | Small UI state (IDs, text input, selected tab) |
| `SavedStateHandle` (in a ViewModel) | ✅ | ✅ | Small screen state that the ViewModel owns |
| `DataStore` / `SharedPreferences` | ✅ | ✅ (on disk) | Settings, flags, small key-value data |
| `Room` / SQLite / files | ✅ | ✅ (on disk) | Structured, large, or long-lived data |

> **Rule of thumb:** keep only small, simple data in the saved-instance-state `Bundle` (it has a size limit of roughly 1 MB per transaction). Store large data in a database and save only its **ID**.

### 3.3 The classic View system

#### Saving with `onSaveInstanceState`

```kotlin
class MainActivity : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        count = savedInstanceState?.getInt(KEY_COUNT) ?: 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_COUNT, count)
    }

    companion object { private const val KEY_COUNT = "count" }
}
```

Views that have an `android:id`, such as `EditText`, automatically save some of their own state, like typed text and cursor position.

#### Using a `ViewModel`

A `ViewModel` outlives configuration changes, so you don't need to reload data after every rotation.

```kotlin
class CounterViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Survives rotation and process death
    val count: StateFlow<Int> = savedStateHandle.getStateFlow("count", 0)

    fun increment() {
        savedStateHandle["count"] = count.value + 1
    }
}
```

```kotlin
class MainActivity : AppCompatActivity() {
    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.count.collect { binding.countText.text = it.toString() }
            }
        }
        binding.button.setOnClickListener { viewModel.increment() }
    }
}
```

### 3.4 State in Jetpack Compose

Compose is **declarative**: the UI is a function of state.

```
UI = f(state)
```

When state changes, Compose **recomposes**: it re-runs the composable functions that read that state and updates the screen. You never call `setText()` yourself.

#### `mutableStateOf` + `remember`

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }

    Button(onClick = { count++ }) {
        Text("Clicked $count times")
    }
}
```

- `mutableStateOf(0)` creates an **observable** value. Compose tracks who reads it.
- `remember { }` keeps the value across recompositions. Without it, `count` would reset to 0 every time the function re-runs.
- `remember` does **not** survive rotation or process death.

#### `rememberSaveable`

```kotlin
var count by rememberSaveable { mutableStateOf(0) }
```

This works like `remember`, but the value is also stored in the saved-instance-state `Bundle`, so it survives configuration changes and process death.

#### State hoisting

**State hoisting** means moving state *up* to the caller so the composable becomes **stateless** and reusable:

```kotlin
// Stateless: receives state, emits events
@Composable
fun Counter(count: Int, onIncrement: () -> Unit) {
    Button(onClick = onIncrement) { Text("Clicked $count times") }
}

// Stateful: owns the state
@Composable
fun CounterScreen() {
    var count by rememberSaveable { mutableStateOf(0) }
    Counter(count = count, onIncrement = { count++ })
}
```

The pattern is **state goes down, events go up.**

#### Derived state

```kotlin
val listState = rememberLazyListState()
val showScrollToTop by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}
```

`derivedStateOf` recomputes only when its inputs change, and it triggers recomposition only when the *result* changes.

#### Collecting ViewModel state in Compose

```kotlin
@Composable
fun CounterRoute(viewModel: CounterViewModel = viewModel()) {
    val count by viewModel.count.collectAsStateWithLifecycle()
    Counter(count = count, onIncrement = viewModel::increment)
}
```

### 3.5 The Remember API (Compose)

Composable functions are **re-run (recomposed)** whenever the state they read changes. Any normal local variable is re-created from scratch on every run:

```kotlin
@Composable
fun Broken() {
    var count = 0                       // reset to 0 on every recomposition
    Button(onClick = { count++ }) { Text("$count") }   // never updates
}
```

The **Remember API** is the set of functions that let a composable **keep a value across recompositions**.

#### How `remember` works

- On the **first composition**, `remember { ... }` runs the lambda and stores the result in the composition (the *slot table*).
- On **recomposition**, it skips the lambda and returns the stored value.
- When the composable **leaves the composition** (for example, it's hidden by an `if`), the value is **forgotten**. If it comes back, the value starts fresh.
- Stored values are tied to the **position of the call** in the UI tree, not to a variable name.

```kotlin
val formatter = remember { DecimalFormat("#,##0.00") }   // created once, reused
```

> `remember` caches **any** object, not just state. On its own it doesn't cause recomposition. To make the UI update when a value changes, combine it with a **State** object: `remember { mutableStateOf(...) }`.

#### `remember` with keys

When any key changes, the cached value is thrown away and the lambda runs again:

```kotlin
@Composable
fun UserAvatar(userId: String) {
    // Recomputed only when userId changes
    val avatarUrl = remember(userId) { buildAvatarUrl(userId) }
    AsyncImage(model = avatarUrl, contentDescription = null)
}
```

Overloads exist for one, two, three, or any number of keys: `remember(key1, key2) { }` and `remember(vararg keys) { }`.

#### `rememberSaveable`

This works like `remember`, but it also saves the value into the saved-instance-state `Bundle`, so it survives **configuration changes and process death**.

```kotlin
var query by rememberSaveable { mutableStateOf("") }
```

It works automatically for types that can go into a `Bundle`: primitives, `String`, `Parcelable`, and `Serializable`. For other types, use one of these:

```kotlin
// Option 1: make it Parcelable (needs the kotlin-parcelize plugin)
@Parcelize
data class City(val name: String, val country: String) : Parcelable

var city by rememberSaveable { mutableStateOf(City("Pune", "India")) }

// Option 2: mapSaver
val CitySaver = mapSaver(
    save = { mapOf("name" to it.name, "country" to it.country) },
    restore = { City(it["name"] as String, it["country"] as String) }
)

// Option 3: listSaver
val CityListSaver = listSaver<City, Any>(
    save = { listOf(it.name, it.country) },
    restore = { City(it[0] as String, it[1] as String) }
)

var city2 by rememberSaveable(stateSaver = CitySaver) {
    mutableStateOf(City("Mumbai", "India"))
}
```

#### Other `remember*` functions

| Function | Purpose |
|---|---|
| `remember { }` | Cache a value across recompositions |
| `remember(key) { }` | Cache a value, recalculating when the key changes |
| `rememberSaveable { }` | Cache a value that also survives rotation and process death |
| `rememberCoroutineScope()` | A `CoroutineScope` tied to the composition, for launching coroutines from callbacks such as `onClick` |
| `rememberUpdatedState(value)` | Always holds the **latest** value, so a long-running effect can use it without restarting |
| `rememberScrollState()`, `rememberLazyListState()` | Built-in state holders for scrolling; they're already saveable |
| `rememberNavController()`, `rememberDrawerState()`, `rememberPagerState()`, and others | State holders that components provide |

```kotlin
@Composable
fun SaveButton(onSave: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    Button(onClick = { scope.launch { onSave() } }) { Text("Save") }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val currentOnTimeout by rememberUpdatedState(onTimeout)
    LaunchedEffect(Unit) {           // runs once, never restarts
        delay(2_000)
        currentOnTimeout()           // but always calls the latest lambda
    }
}
```

#### Writing your own state holder with `remember`

```kotlin
class SearchState(initial: String) {
    var query by mutableStateOf(initial)
    val isEmpty get() = query.isBlank()
    fun clear() { query = "" }
}

@Composable
fun rememberSearchState(initial: String = "") = remember { SearchState(initial) }
```

### 3.6 The State API (Compose)

The **State API** is how Compose knows *when* to recompose. A `State<T>` is an **observable value holder**:

- When a composable **reads** `state.value`, Compose records that dependency.
- When someone **writes** a new value, Compose schedules recomposition for **only** the composables that read it.

```kotlin
interface State<out T> { val value: T }                  // read-only
interface MutableState<T> : State<T> { override var value: T }   // read and write
```

#### Creating state

```kotlin
val count = mutableStateOf(0)            // MutableState<Int>
count.value++                            // write: triggers recomposition
Text("${count.value}")                   // read: registers the dependency
```

There are three ways to declare it inside a composable:

```kotlin
// 1. Access .value directly
val name = remember { mutableStateOf("") }
TextField(value = name.value, onValueChange = { name.value = it })

// 2. Property delegate (most common)
//    needs: import androidx.compose.runtime.getValue / setValue
var name2 by remember { mutableStateOf("") }
TextField(value = name2, onValueChange = { name2 = it })

// 3. Destructuring
val (name3, setName3) = remember { mutableStateOf("") }
TextField(value = name3, onValueChange = setName3)
```

#### Specialized state types

| Function | Type | Why |
|---|---|---|
| `mutableStateOf(T)` | `MutableState<T>` | General purpose |
| `mutableIntStateOf(0)` | `MutableIntState` | Avoids boxing for `Int` |
| `mutableLongStateOf`, `mutableFloatStateOf`, `mutableDoubleStateOf` | Primitive state types | Avoid boxing |
| `mutableStateListOf()` | `SnapshotStateList<T>` | Observable list: adding or removing items triggers recomposition |
| `mutableStateMapOf()` | `SnapshotStateMap<K, V>` | Observable map |

> ⚠️ **Common pitfall:** changing a normal mutable collection inside state does **not** trigger recomposition, because the reference itself didn't change.
> ```kotlin
> val items = remember { mutableStateOf(arrayListOf<String>()) }
> items.value.add("x")                               // ❌ UI won't update
>
> val items2 = remember { mutableStateListOf<String>() }
> items2.add("x")                                    // ✅ UI updates
>
> var items3 by remember { mutableStateOf(listOf<String>()) }
> items3 = items3 + "x"                              // ✅ new list, UI updates
> ```

#### Mutation policies

A **mutation policy** decides whether a write counts as a change. You pass it as the second argument to `mutableStateOf`:

| Policy | Treats the write as a change when |
|---|---|
| `structuralEqualityPolicy()` (default) | `old != new` (compared with `equals`) |
| `referentialEqualityPolicy()` | `old !== new` (a different instance) |
| `neverEqualPolicy()` | Always, even if the value is the same |

```kotlin
val trigger = mutableStateOf(Unit, neverEqualPolicy())
```

#### Derived and converted state

| API | What it does |
|---|---|
| `derivedStateOf { }` | Computes state from other state, and recomposes only when the **result** changes |
| `produceState(initial, keys) { }` | Converts non-Compose sources (callbacks, suspend functions) into `State<T>` |
| `Flow.collectAsState()` | Converts a `Flow` into `State<T>` |
| `Flow.collectAsStateWithLifecycle()` | Same, but stops collecting when the app is in the background (recommended on Android) |
| `LiveData.observeAsState()` | Converts `LiveData` into `State<T?>` |
| `snapshotFlow { }` | The reverse: converts Compose state reads into a `Flow` |

```kotlin
// derivedStateOf: avoids recomposing on every scroll pixel
val listState = rememberLazyListState()
val showFab by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

// produceState: load data into State
@Composable
fun loadUser(id: String, repo: UserRepository): State<User?> =
    produceState<User?>(initialValue = null, id) {
        value = repo.getUser(id)
    }

// snapshotFlow: react to state changes inside a coroutine
LaunchedEffect(listState) {
    snapshotFlow { listState.firstVisibleItemIndex }
        .distinctUntilChanged()
        .collect { index -> analytics.logScroll(index) }
}
```

#### Under the hood: the Snapshot system

Compose state is built on **snapshots**, which work like database transactions for in-memory state:

- Every read and write of a `State` object happens inside a snapshot, and Compose records which state each composable read.
- When a write is applied, Compose finds the composables that read that state and **invalidates** them. Only those recompose.
- Snapshot state is **thread-safe**: you can write state from a background thread, and Compose will pick up the change.

#### Stability: `@Stable` and `@Immutable`

Compose can **skip** recomposing a composable if all its parameters are *stable* and unchanged.

- `@Immutable` promises that the object's public properties never change after it's built.
- `@Stable` promises that changes are always reported through Compose `State`.

```kotlin
@Immutable
data class Product(val id: String, val name: String, val price: Double)
```

Primitives, `String`, function types, and classes whose properties are all `val` and stable are inferred as stable automatically. The standard `List` and `Map` types are treated as unstable unless you use immutable collections or turn on strong skipping mode, which is the default in recent Compose compiler versions.

#### Remember API vs. State API

| | Remember API | State API |
|---|---|---|
| Question it answers | *"Keep this value across recompositions"* | *"Tell Compose when this value changes"* |
| Main functions | `remember`, `rememberSaveable` | `mutableStateOf`, `derivedStateOf`, `collectAsState` |
| Causes recomposition? | No | Yes, when the value changes |
| Used together | `var x by remember { mutableStateOf(0) }`: **remember** keeps the State object alive, and **State** makes changes observable | |

#### Is it a function or an object?

**Both `mutableStateOf` and `remember` are functions.** They differ in what they return and where you can call them.

```kotlin
// A plain function (not @Composable) that RETURNS an object of type MutableState<T>
fun <T> mutableStateOf(value: T, policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy()): MutableState<T>

// A @Composable function that caches whatever the lambda returns
@Composable
inline fun <T> remember(calculation: () -> T): T
```

| | `mutableStateOf` | `remember` |
|---|---|---|
| What it is | A function | A `@Composable` inline function |
| What it returns | A `MutableState<T>` object (an observable box with `.value`) | The cached result of its lambda |
| Where you can call it | Anywhere, including a ViewModel or a plain class | Only inside a composable |
| Its job | Makes a value **observable** | Makes a value **survive recomposition** |

What happens if you drop one of them:

| Code | Result |
|---|---|
| `var x by mutableStateOf(0)` (no `remember`) | A new box starting at 0 is created on every recomposition, so the value resets right away |
| `var x = remember { 0 }` (no `mutableStateOf`) | The value is kept, but changing it doesn't trigger recomposition, so the UI never updates |
| `var x by remember { mutableStateOf(0) }` | ✅ The value is kept **and** the UI updates |

## 4. UI state vs. app state

| | UI state | App or business state |
|---|---|---|
| What | What the screen shows right now | The data and rules behind the app |
| Examples | Is loading, error message, scroll position, dialog open | User profile, cart contents, orders |
| Lives in | Composable / ViewModel | Repository, database, server |
| Lifetime | Usually one screen | Across screens and sessions |

A common way to model UI state is a single immutable class:

```kotlin
data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)
```

Or, when the states are mutually exclusive, a sealed interface:

```kotlin
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val user: User) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
```

## 5. Unidirectional Data Flow (UDF)

Modern Android architecture, as described in Google's *Guide to App Architecture*, recommends **unidirectional data flow**:

```
        ┌──────────── events (clicks, input) ────────────┐
        │                                                 ▼
   ┌─────────┐        state         ┌────────────┐   ┌──────────────┐
   │   UI    │ ◀─────────────────── │ ViewModel  │ ◀─│  Repository  │ ◀─ Room / Network
   └─────────┘                      └────────────┘   └──────────────┘
```

1. The **UI** displays state and sends **events** up.
2. The **ViewModel** handles events, updates state, and exposes it as `StateFlow`.
3. The **Repository** is the **single source of truth** for app data.

```kotlin
class ProfileViewModel(private val repo: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()   // read-only to the UI

    init { load() }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        runCatching { repo.getUser() }
            .onSuccess { user -> _uiState.update { it.copy(isLoading = false, user = user) } }
            .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
    }
}
```

Benefits of UDF:

- **Predictable**: state changes in one place only.
- **Testable**: you can test the ViewModel without any UI.
- **Consistent**: the UI is always a reflection of a single state object.

## 6. Observable state holders

| Type | Notes |
|---|---|
| `State<T>` / `MutableState<T>` | Compose's observable state; reads trigger recomposition |
| `StateFlow<T>` | Kotlin coroutines hot flow that always has a current value; the recommended way for a ViewModel to expose state |
| `SharedFlow<T>` | Hot flow with no required current value; useful for one-off events |
| `LiveData<T>` | Older lifecycle-aware observable; still common in View-based code |

## 7. Best practices checklist

- ✅ Keep a **single source of truth** for each piece of data.
- ✅ Expose **immutable** state (`StateFlow`, `State`) and keep the mutable version private.
- ✅ Model screen state as an immutable `data class` or `sealed interface`.
- ✅ Hoist state so composables stay stateless and reusable.
- ✅ Use a `ViewModel` for screen state and `SavedStateHandle` or `rememberSaveable` for small values that must survive process death.
- ✅ Persist important data (Room, DataStore) so it isn't lost.
- ✅ Collect flows in a lifecycle-aware way (`collectAsStateWithLifecycle`, `repeatOnLifecycle`).
- ❌ Don't hold `Context`, `View`, or `Activity` references in a ViewModel (this causes memory leaks).
- ❌ Don't put large objects or bitmaps in a `Bundle`.
- ❌ Don't duplicate state that can be **derived**.

## 8. Testing state survival

- **Rotation:** rotate the device or emulator, or toggle dark mode.
- **Process death:** put the app in the background, then run:
  ```bash
  adb shell am kill <your.package.name>
  ```
  Reopen the app from the recents screen. State saved with `rememberSaveable`, `SavedStateHandle`, or `onSaveInstanceState` should come back.
- **Developer options:** turn on *"Don't keep activities"* to destroy every Activity as soon as you leave it.

## 9. Further reading

- [State and Jetpack Compose](https://developer.android.com/develop/ui/compose/state)
- [Save UI states](https://developer.android.com/topic/libraries/architecture/saving-states)
- [ViewModel overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Guide to app architecture](https://developer.android.com/topic/architecture)
- [The activity lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle)
