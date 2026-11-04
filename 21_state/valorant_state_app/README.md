# Valorant State: Compose state in gaming terms

A one-screen Jetpack Compose app with **10 "rounds"**. Each round is a small playable card
that teaches one state concept, with game terms standing in for the theory.
Read the code of a round, play it on the phone, then read the 💡 lesson at the bottom of the card.

Theory notes: [`../README.md`](../README.md)

## The rounds

| Round | Game situation | Concept | File |
|---|---|---|---|
| 1 | Kills that never count | ❌ `mutableStateOf` **without** `remember`: the value resets | `Round01NoRemember.kt` |
| 2 | HUD that doesn't refresh | ❌ `remember` **without** State: the value changes but the UI doesn't redraw | `Round02NoState.kt` |
| 3 | Kill counter that works | ✅ `remember { mutableStateOf(0) }` (rotate the phone: it resets) | `Round03RememberState.kt` |
| 4 | Credits survive rotation | `rememberSaveable` | `Round04RememberSaveable.kt` |
| 5 | Buy menu | State hoisting: state goes down, events go up | `Round05Hoisting.kt` |
| 6 | Ult ready? | `derivedStateOf` | `Round06DerivedState.kt` |
| 7 | Agent select | `remember(key)` | `Round07RememberKey.kt` |
| 8 | Team lobby | `mutableStateListOf` | `Round08StateList.kt` |
| 9 | Spike timer | `LaunchedEffect(key)` + coroutines | `Round09LaunchedEffect.kt` |
| 10 | Match score | `ViewModel` + `StateFlow` + `collectAsStateWithLifecycle` | `Round10ViewModel.kt` |

All rounds are in `app/src/main/java/com/paras/valorantstate/rounds/`.

## Cheat sheet: game term → Compose term

| In the game | In Compose |
|---|---|
| Scoreboard that refreshes the HUD | `mutableStateOf`: an observable value |
| Keeping the scoreboard for the whole match | `remember`: survives recomposition |
| Credits saved on your account | `rememberSaveable`: survives rotation and process death |
| Shop screen that doesn't hold your money | Stateless composable (hoisted state) |
| "ULT READY" banner | `derivedStateOf`: recalculated only when the result changes |
| New game plan when you switch agent | `remember(agent) { }`: recalculated when the key changes |
| Team lobby | `mutableStateListOf`: an observable list |
| Spike countdown | `LaunchedEffect`: a coroutine tied to the composition |
| Game server holding the match score | `ViewModel`: survives rotation, owns the rules |
| HUD redrawing | Recomposition |

## Build and run

Open this folder (`valorant_state_app`) in Android Studio and press Run, or:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Built with AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06.00, minSdk 24, compileSdk 34.

## Try this

1. **Round 1:** tap "Get a kill". It stays at 0. Why?
2. **Round 2:** tap "Get a kill" 3 times, then "Refresh HUD". Where did the kills come from?
3. **Rounds 3 vs 4:** add kills and credits, then rotate. Which one survives?
4. **Round 7:** get kills and watch the "built N times" counter. Then switch agent.
5. **Round 9:** plant, then defuse at 5 seconds. What cancelled the timer?
6. **Round 10:** win 5 rounds, rotate. The score is still there. Who's holding it?
