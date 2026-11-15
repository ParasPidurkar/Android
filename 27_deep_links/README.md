# Deep Links with Navigation Compose

This is `26_navigation` with **deep links** added. It's the same Valorant match flow (Lobby → Choose Map → Agent Select → Agent → Match, plus Career), but four of its screens can now be opened straight from a URL, with their arguments filled in. The URL can come from a browser, a chat message, a notification, another app, or `adb`.

Read `26_navigation` first. This project assumes you know `NavHost`, `NavController`, type-safe `@Serializable` routes, `toRoute()` and how the back stack works, and only explains what deep links add on top.

## What changed compared to 26

| File | Change |
|---|---|
| `AndroidManifest.xml` | A second `<intent-filter>` (`VIEW` + `DEFAULT` + `BROWSABLE`, `valo://app`) and `launchMode="singleTop"`. |
| `Routes.kt` | `DEEP_LINK_BASE = "valo://app"`, with the table of every URL. |
| `ValoNavApp.kt` | `deepLinks = listOf(navDeepLink<R>(...))` on four screens, an `onNewIntent` listener that calls `handleDeepLink()`, and the ← arrow rule for deep-linked stacks. |
| `ValoData.kt` | `agentOrNull()`, because an id from a URL can be anything. |
| `MainActivity.kt` | No code change, only comments explaining why the deep link code lives in `ValoNavApp`. |
| `screens/*.kt` | Unchanged. A screen can't tell whether it was opened by a tap or by a URL. |

The package is `com.paras.deeplinks`, so this app installs next to `26_navigation` instead of replacing it.

## The links

| URL | Opens | Back stack you get (usual case) |
|---|---|---|
| `valo://app/lobby` | `Lobby` | `[Lobby]` |
| `valo://app/career` | `Career` | `[Lobby, Career]` |
| `valo://app/agents/{map}` | `AgentSelect(map)` | `[Lobby, AgentSelect]` |
| `valo://app/agent/{agentId}/{map}` | `AgentDetail(agentId, map)` | `[Lobby, AgentDetail]` |

`MapPick` and `Match` are deliberately **not** linkable. MapPick only works when the Lobby is underneath waiting for its result. Match should only be reachable by actually locking in.

## 1. The big picture: two layers

A deep link goes through two separate filters:

```
 valo://app/agent/sova/Haven
        │
        ▼
 ┌──────────────────────────────┐   "Which APP handles valo://app ?"
 │ Android + AndroidManifest.xml │   intent-filter: VIEW + DEFAULT + BROWSABLE, scheme=valo host=app
 └──────────────────────────────┘
        │  starts MainActivity (or calls onNewIntent on it)
        ▼
 ┌──────────────────────────────┐   "Which SCREEN handles /agent/sova/Haven ?"
 │ NavController.handleDeepLink │   matches navDeepLink patterns in ValoNavApp.kt
 └──────────────────────────────┘
        │  AgentDetail(agentId = "sova", map = "Haven")
        ▼
   back stack = [Lobby, AgentDetail]
```

The manifest only claims a URL space for the app. Picking the screen from the path is the NavController's job. So adding a new linkable screen under `valo://app` needs **no manifest change**.

## 2. Layer 1: the manifest

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop">

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="valo" android:host="app" />
    </intent-filter>
</activity>
```

| Line | Why it's there |
|---|---|
| `action VIEW` | The standard action for "show the thing at this URI". Every link sender uses it. |
| `category DEFAULT` | `startActivity()` adds `CATEGORY_DEFAULT` to every implicit Intent. Leave it out and the filter **never** matches. Easy to forget. |
| `category BROWSABLE` | Lets browsers start you. Without it, `adb` works but tapping the link in Chrome does nothing. |
| `data scheme/host` | Which URIs. No `path` attribute, so any path under `valo://app` is ours. |
| `exported="true"` | Other apps may start this Activity. Needed for the launcher and for links. |
| `launchMode="singleTop"` | If the app is already open, reuse the Activity (`onNewIntent`) instead of stacking a second copy with its own separate back stack. |

**Custom scheme vs. https App Links.** `valo://` is quick to test and needs no website. But any app can register the same scheme, and most apps won't turn `valo://...` in plain text into a tappable link. Real apps usually use `https://valo.example.com/...` with `android:autoVerify="true"` and an `assetlinks.json` file on that domain. That proves you own the domain, so Android opens your app directly without showing an app picker. The Navigation code stays the same; only the base URL changes.

## 3. Layer 2: `navDeepLink` on each screen

```kotlin
const val DEEP_LINK_BASE = "valo://app"          // Routes.kt

composable<AgentDetail>(
    deepLinks = listOf(navDeepLink<AgentDetail>(basePath = "$DEEP_LINK_BASE/agent")),
) { entry ->
    val route = entry.toRoute<AgentDetail>()      // same as before: no special deep-link code
    ...
}
```

The type-safe `navDeepLink<R>(basePath)` (Navigation 2.8+) needs only the **start** of the URL. Navigation reads `R`'s `@Serializable` fields and appends them with the same rules it uses for the route (`26_navigation`, section 13):

| Field in the route class | Becomes | Example |
|---|---|---|
| required (`val map: String`) | path segment | `/{map}` |
| has a default or is nullable (`val skin: String = "default"`) | query parameter, optional in the URL | `?skin={skin}` |

So:

```
navDeepLink<Lobby>(basePath = "valo://app/lobby")         →  valo://app/lobby
navDeepLink<AgentSelect>(basePath = "valo://app/agents")  →  valo://app/agents/{map}
navDeepLink<AgentDetail>(basePath = "valo://app/agent")   →  valo://app/agent/{agentId}/{map}
```

When a URL matches, the values go into the new entry's **arguments Bundle**, exactly as if you had called `navigate(AgentDetail("sova", "Haven"))`. That's why `toRoute<AgentDetail>()` works unchanged. The screen can't tell whether it was opened by a tap or by a URL.

**Matching rules worth knowing:**

- Scheme and host must match exactly.
- Every `{placeholder}` must be present. `valo://app/agent/jett` (no map) matches nothing. The app just opens on the Lobby.
- Values are URL-decoded: `valo://app/agents/Ice%20box` gives `map = "Ice box"`.
- Values must convert to the field's type. An `Int` field given `"abc"` doesn't match.
- Renaming a route property changes the URL. Links people already shared would stop working, so treat deep link URLs as a public API.

> **The old string way**, for comparison: `navDeepLink { uriPattern = "valo://app/agent/{agentId}/{map}" }` plus a `navArgument` per placeholder. You type the pattern by hand, and a typo in a placeholder name fails only at runtime.

## 4. Getting the Intent to the NavController

How the link reaches the NavController depends on whether the app is already running.

**Cold start** (the app wasn't running). Android creates `MainActivity` with the link as `activity.intent`. You write nothing: when `NavHost` gives the graph to the NavController, it checks `activity.intent` by itself and opens the matching screen *instead of* only the start destination. It also saves a "deep link handled" flag, so rotating the phone doesn't open the screen again.

**Warm start** (the app is open or in the background). Because of `singleTop`, Android reuses the existing Activity and calls `onNewIntent(intent)`. `activity.intent` is **not** replaced, and the NavController isn't told. Without extra code, the link would just bring the app to the front and do nothing. So `ValoNavApp` forwards new Intents:

```kotlin
val activity = LocalContext.current as ComponentActivity
DisposableEffect(navController, activity) {
    val listener = Consumer<Intent> { intent -> navController.handleDeepLink(intent) }
    activity.addOnNewIntentListener(listener)
    onDispose { activity.removeOnNewIntentListener(listener) }
}
```

- `addOnNewIntentListener` is provided by `ComponentActivity`, so the Activity doesn't need a reference to the NavController. All navigation code stays in `ValoNavApp.kt`.
- `DisposableEffect` removes the listener when the composable leaves (or when a key changes), so the Activity never holds on to a stale NavController.
- `handleDeepLink()` returns `false` for Intents that match no `navDeepLink`, so forwarding every Intent is safe.

## 5. The back stack a deep link builds

A deep link doesn't just push the target screen. `handleDeepLink()` builds a **synthetic back stack**: the graph's start destination, then the target. What happens next depends on the Intent's flags. This is how Navigation 2.8.0 behaves (read from the library's bytecode):

| Intent flags | Typical sender | What happens | Resulting stack |
|---|---|---|---|
| `NEW_TASK` (no `CLEAR_TASK`) | `adb`, browsers, most apps | Adds `CLEAR_TASK`, **restarts** `MainActivity` in a fresh task, finishes the old one. Continues as the next row. | (see next row) |
| `NEW_TASK` + `CLEAR_TASK` | the restart above, or a `TaskStackBuilder` PendingIntent (section 8) | Pops everything, pushes the start destination, then the target. | `[Lobby, AgentDetail]` |
| no `NEW_TASK` | another app's Activity calling `startActivity()` directly | `MainActivity` runs **inside the other app's task**. The target replaces the stack. | `[AgentDetail]` |

Consequences:

- In the usual (`NEW_TASK`) case, the old stack is thrown away. If you had picked a map on the Lobby, it's gone after a deep link. You might also see a short flicker from the restart.
- The synthetic stack is only *start destination + target*. `valo://app/agent/sova/Haven` gives `[Lobby, AgentDetail]`, **not** `[Lobby, AgentSelect, AgentDetail]`. Navigation can't know AgentSelect "belongs" in between. If AgentSelect and AgentDetail were in a nested graph (`26_navigation`, section 17), that graph's start destination would be added too.

## 6. Up vs. Back, finally different

Inside the app, ← (`navigateUp()`) and system Back (`popBackStack()`) do the same thing. In the third row above, they don't:

| Stack `[AgentDetail]`, running in another app's task | Result |
|---|---|
| **Back** | Finishes `MainActivity` and returns to **the app that sent the link**. |
| **Up (←)** | `navigateUp()` sees "one screen, opened from a deep link" and relaunches **our app in its own task** at the Lobby. |

That's why the top bar shows ← on every screen except the Lobby, even when nothing is below it (`ValoNavApp`, `canGoBack`).

## 7. Deep link arguments are untrusted input

The Activity is exported, so **any** app or web page can send any URL with any values. Navigation turns them into a route object, but it can't check that they make sense.

```kotlin
val agent = ValoData.agentOrNull(route.agentId)   // not agent(): that would throw
if (agent == null) {
    Text("Unknown agent: ${route.agentId}")
    return@composable
}
```

Before deep links, `ValoData.agent(id)` could use `first { }` safely, because ids only came from the in-app list. With `valo://app/agent/nobody/Haven`, it would crash the app. Rules of thumb:

- Validate every deep-link argument, and look ids up with a nullable result.
- Never link straight to a screen that performs an action (delete, pay, lock in). Link to a screen that shows a confirm button.
- Don't put secrets or tokens in deep link URLs; other apps can see and send them.

## 8. Creating deep links from your own code

**Inside the app**, you can navigate by URL (handy when a server sends you a link string):

```kotlin
navController.navigate("valo://app/agent/sova/Haven".toUri())
```

This uses the same `navDeepLink` matching, but it's an ordinary `navigate()`: it pushes onto the current stack and doesn't rebuild it.

**From a notification**, wrap the URL in an Intent aimed at your Activity:

```kotlin
val intent = Intent(Intent.ACTION_VIEW, "valo://app/career".toUri(), context, MainActivity::class.java)
val pending = TaskStackBuilder.create(context).run {
    addNextIntentWithParentStack(intent)
    getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
```

Setting the class makes the Intent **explicit**, so no other app can intercept it. The URI still tells the NavController which screen to open.

## 9. Testing

```bash
adb shell am start -a android.intent.action.VIEW -d "valo://app/career"
adb shell am start -a android.intent.action.VIEW -d "valo://app/agents/Bind"
adb shell am start -a android.intent.action.VIEW -d "valo://app/agent/sova/Haven"
adb shell am start -a android.intent.action.VIEW -d "valo://app/agent/nobody/Haven"   # unknown agent message
adb shell am start -a android.intent.action.VIEW -d "valo://app/agent/jett"           # no match: opens on Lobby
```

Run each one with the app closed **and** with it open, to exercise both the cold-start and the `onNewIntent` path. Quote the URL so the shell doesn't interpret `?` or `&` in query parameters. If nothing opens at all, check the manifest first (`DEFAULT` category, scheme and host spelling). If the app opens but on the wrong screen, check the `navDeepLink` base path.

## 10. Common mistakes

| Symptom | Cause |
|---|---|
| `adb` says "Activity not started, unable to resolve Intent" | The manifest filter doesn't match: missing `DEFAULT`, wrong scheme or host, or `exported="false"`. |
| Works in `adb`, not when tapped in a browser | Missing `BROWSABLE`, or the browser doesn't make custom schemes clickable (use https App Links). |
| Works from a closed app, does nothing when the app is open | `onNewIntent` isn't forwarded to `handleDeepLink()`. |
| Crash on a typed URL | An argument used without validation (`first { }`, `!!`, array index). |
| Old shared links stopped working | A route property or the `basePath` was renamed. |

## Try it

Install the app (`./gradlew installDebug`), then:

1. With the app closed, run `adb shell am start -a android.intent.action.VIEW -d "valo://app/agent/sova/Haven"`. Sova opens, and Back goes to the Lobby (synthetic back stack, section 5).
2. Choose a map on the Lobby, then run the same command again with the app open. Sova opens, and the picked map is gone because the task was rebuilt (section 5).
3. Run `valo://app/agents/Bind`. Agent Select opens with Bind as the map. Tap an agent: from here on it's normal navigation.
4. Try `valo://app/agent/nobody/Haven`. You get "Unknown agent", not a crash (section 7).
5. Try `valo://app/agent/jett` (map missing). No pattern matches, so the app opens on the Lobby.
6. Comment out the `DisposableEffect` that calls `handleDeepLink()` and repeat step 2. The app comes to the front but stays on the Lobby: links only work from a cold start (section 4).

## Run

```
./gradlew installDebug
```

To build an APK you can copy to a phone:

```
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```
