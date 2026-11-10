# Surviving a configuration change

Rotating the phone destroys and recreates the Activity. Anything held only in `remember` is lost. `rememberSaveable` writes its value into the saved instance state `Bundle`, so it comes back. The value must be something a `Bundle` can hold.

| Your type | What to do | Example |
|---|---|---|
| You own the class | Mark it `@Parcelize` | `ParcelizeExample.kt` |
| You can't make it Parcelable | Write a `Saver` with `mapSaver` | `MapSaverExample.kt` |

## 1. `@Parcelize`

Add the plugin `org.jetbrains.kotlin.plugin.parcelize`, then:

```kotlin
@Parcelize
data class Loadout(val agent: String, val weapon: Weapon, val credits: Int) : Parcelable

var loadout by rememberSaveable { mutableStateOf(startingLoadout) }
```

Every property must also be saveable, which is why `Weapon` is `@Parcelize` too.

## 2. `mapSaver`

```kotlin
val MatchScoreSaver: Saver<MatchScore, Any> = mapSaver(
    save = { mapOf("attackers" to it.attackers, "defenders" to it.defenders) },
    restore = { MatchScore(it["attackers"] as Int, it["defenders"] as Int) },
)

var score by rememberSaveable(stateSaver = MatchScoreSaver) {
    mutableStateOf(MatchScore(0, 0))
}
```

Use `stateSaver` when the value is wrapped in `mutableStateOf`. Use `saver` only when you remember a plain value.

## Good to know

- Saved state is limited in size, so keep it small. Store an ID rather than a big list.
- `rememberSaveable` also survives Android killing the app in the background, but not the user closing it from Recents.

## Run it

Open this folder in Android Studio, or run `./gradlew installDebug`. Change some values and rotate the device.
