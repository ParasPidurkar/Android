# Network Status App — Stage 2: Wiring Up ConnectivityManager (Video Transcript)

A detailed line-by-line transcript for the second stage of the Network Status app, where we wire up the button to actually check the network state and update the text. Each section shows the exact code to display on screen followed by the narration.

---

## Intro

**[Show: the running app from Stage 1 — the text and button on screen, but tapping the button does nothing]**

Welcome back to Stage 2. In the last stage we built the visual layout — a status TextView on top and a "Check Network Status" button below it. But right now, tapping the button does absolutely nothing. It just clicks and stares back at you.

In this stage we're going to fix that. When the user taps the button, we'll use Android's ConnectivityManager to look at the current network state — Wi-Fi, cellular, ethernet, or offline — and update the TextView with a human-readable message. Let's open `MainActivity.kt`.

---

## The Starting Point

**[Show on screen — the file as Android Studio left it:]**

```kotlin
package com.example.connectivitypermission

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

    }
}
```

Here's what we have to start. Just the boilerplate `MainActivity` — package declaration, a few imports, the class, and an `onCreate` that calls `super.onCreate`, `enableEdgeToEdge`, and `setContentView`. The body after `setContentView` is empty. That's where we're going to add all our work.

---

## Adding the Imports

**[Show on screen — the updated import block:]**

```kotlin
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
```

First we add the imports for the classes we're going to use. Let me walk through the new ones.

`android.content.Context` — Context is one of the most fundamental classes in Android. It's your gateway to system services, resources, and more. We need it because we're going to call `getSystemService` with the `Context.CONNECTIVITY_SERVICE` constant, and that constant lives on the Context class.

`android.net.ConnectivityManager` — this is the system service that tells us about the device's network state. Whether Wi-Fi is on, whether cellular data is connected, whether there's actual internet reachability — all of that comes from ConnectivityManager.

`android.net.NetworkCapabilities` — this class describes what a specific network can do. We use it to check the transport type (Wi-Fi vs cellular) and whether the network claims to have internet access.

`android.widget.Button` and `android.widget.TextView` — standard widget imports. We're going to declare properties of these types.

The two imports for `ViewCompat` and `WindowInsetsCompat` from `androidx.core.view` were in the template but we're not using them, so I've dropped them to keep the file clean.

---

## The `lateinit` Properties

**[Show on screen — the two new properties at the top of the class:]**

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var textNetworkStatus: TextView
    private lateinit var buttonCheckNetwork: Button
```

At the top of the class we declare two properties — one for each view in the layout.

`private lateinit var textNetworkStatus: TextView` holds the status TextView. `private` because nothing outside the class should touch it. `lateinit var` because we can't initialize it until `setContentView` has run inside `onCreate`, and until then the view doesn't exist yet. The type is `TextView`, matching the widget we defined in XML.

`private lateinit var buttonCheckNetwork: Button` is the same idea, but for the button. Same `private`, same `lateinit`, but the type is `Button`.

Holding both as class properties means we can reach them from any method in the class — not just from `onCreate`. That'll matter later when the click listener needs to write to the TextView.

---

## Wiring Up the Views Inside `onCreate`

**[Show on screen — the new lines inside `onCreate`:]**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_main)

    // Link XML views to Kotlin variables.
    textNetworkStatus = findViewById(R.id.textNetworkStatus)
    buttonCheckNetwork = findViewById(R.id.buttonCheckNetwork)

    // When the button is tapped, check the network and update the text.
    buttonCheckNetwork.setOnClickListener {
        textNetworkStatus.text = getNetworkStatus()
    }
}
```

The top of `onCreate` — the calls to `super.onCreate`, `enableEdgeToEdge`, and `setContentView` — is the same as before. But now we've added meaningful body below.

`textNetworkStatus = findViewById(R.id.textNetworkStatus)` uses Android's standard `findViewById` to look up the TextView we defined in the XML with the id `textNetworkStatus`. Kotlin infers the return type from the property declaration, so we don't need angle brackets. After this line, `textNetworkStatus` points at the real TextView on screen and we can update its text.

`buttonCheckNetwork = findViewById(R.id.buttonCheckNetwork)` does the same thing for the button.

Then we register a click listener:

```
buttonCheckNetwork.setOnClickListener {
    textNetworkStatus.text = getNetworkStatus()
}
```

`setOnClickListener` takes a lambda — a block of code to run when the button is tapped. Inside the lambda, we call `getNetworkStatus()`, which returns a String describing the current network state, and assign that String to `textNetworkStatus.text`. That's the whole click flow — tap, check, display. All the actual work is delegated to `getNetworkStatus`, which we're about to write.

---

## The `getNetworkStatus` Method — Getting the ConnectivityManager

**[Show on screen — the top of the new private method:]**

```kotlin
// Uses ConnectivityManager to figure out the current network state and
// returns a user-friendly message describing it.
private fun getNetworkStatus(): String {
    // Get the system-wide ConnectivityManager.
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
```

Now for the actual network-checking logic.

`private fun getNetworkStatus(): String` declares a private method that returns a String. That String is what we'll display on the TextView.

The first line inside is where we get access to Android's network subsystem:

`val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager`

Let me unpack this. `getSystemService` is a method that all Contexts have — it's how you access system-level services like the ConnectivityManager, WifiManager, LocationManager, and so on. It takes a string constant naming which service you want, and returns a generic `Any?` object.

`Context.CONNECTIVITY_SERVICE` is the specific constant for the network manager. It's just a String under the hood — literally the value `"connectivity"` — but using the named constant is much safer than typing the string yourself.

Since `getSystemService` returns a generic type, we cast it with `as ConnectivityManager` to tell Kotlin "I know this is really a ConnectivityManager". After this line, we have a ConnectivityManager reference we can query.

---

## The `getNetworkStatus` Method — Reading the Active Network

**[Show on screen — the next two lines:]**

```kotlin
    // The currently active network, or null if the device is offline.
    val activeNetwork = connectivityManager.activeNetwork
        ?: return "No network connection"

    // The capabilities object tells us the transport type (Wi-Fi, Cellular,
    // etc.) and whether the network actually has internet access.
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        ?: return "No network connection"
```

Next we read the active network.

`connectivityManager.activeNetwork` gives us a Network object representing whatever the device is currently using — or null if nothing is active. Null is a real possibility if the user turned off Wi-Fi and cellular, or if they're in airplane mode.

The `?:` operator is called the Elvis operator in Kotlin. It says "if the left side is non-null, use it; if it's null, evaluate the right side instead". The right side here is `return "No network connection"`. So this whole expression means: "get the active network, and if there isn't one, immediately return the string 'No network connection' from this whole method."

That's a really tidy pattern for handling a null-and-early-exit case in one line.

The next chunk does the same thing for `getNetworkCapabilities`. Given a Network, this method returns a NetworkCapabilities object describing what that network can do. If for some reason the capabilities can't be read — which is rare but possible if the network state changes at the exact wrong moment — we again return "No network connection".

---

## The `getNetworkStatus` Method — Checking Internet Capability

**[Show on screen — the internet-capability check:]**

```kotlin
    val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
```

This is a subtle but important check. Just because a network is connected doesn't mean it actually has internet access. Think about a captive-portal Wi-Fi at a coffee shop where you have to sign in first, or a corporate network with a proxy, or a home router that isn't paying its ISP bill this month. The Wi-Fi transport is up, but the network can't actually reach the internet.

`hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)` returns a boolean telling us whether the current network actually has internet access. Android probes networks in the background to determine this, so it's reasonably reliable.

We store the answer in `hasInternet` because we're going to use it in the next block.

---

## The `getNetworkStatus` Method — Reporting the Transport Type

**[Show on screen — the `when` block:]**

```kotlin
    // Report the transport type plus whether real internet is available.
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
            if (hasInternet) "Connected via Wi-Fi" else "Wi-Fi connected (no internet)"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
            if (hasInternet) "Connected via Cellular" else "Cellular connected (no internet)"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
            if (hasInternet) "Connected via Ethernet" else "Ethernet connected (no internet)"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ->
            "Connected via VPN"
        else -> "Connected (unknown network type)"
    }
}
```

Finally, we build the actual message using a `when` block with no argument — Kotlin's version of if-else-if-else chains.

Each branch checks a different transport type. `hasTransport(NetworkCapabilities.TRANSPORT_WIFI)` returns true if the current network is Wi-Fi. If it is, we return either "Connected via Wi-Fi" if there's real internet, or "Wi-Fi connected (no internet)" if there isn't. That distinction is really useful for debugging captive portals and misconfigured routers.

The next branches do the same for cellular and ethernet — probably ethernet is only relevant on emulators or Android TV devices, but including it is harmless.

The VPN branch is a little different. If the transport is VPN, we just say "Connected via VPN" without checking internet capability. That's because a VPN by design layers on top of another network, and the internet-capability flag reflects the underlying transport, not the VPN itself.

The `else` branch is a fallback in case none of the known transport types match. That could happen with Bluetooth tethering or some other unusual network. Saying "Connected (unknown network type)" is better than crashing.

Notice the whole `when` block is preceded by `return`. That means the value chosen by whichever branch matches is what our `getNetworkStatus` method returns.

---

## The Manifest Permission

**[Show on screen — the manifest edit:]**

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

One more critical thing that lives outside the Kotlin file — the manifest.

Open `AndroidManifest.xml` and add the line above inside the `<manifest>` tag, above `<application>`. This declares that our app needs permission to read the device's network state.

`ACCESS_NETWORK_STATE` is what Android calls a "normal" permission. That means Android grants it automatically at install time — no runtime dialog is shown to the user. But you still have to declare it in the manifest. If you skip this step, calling `getSystemService(Context.CONNECTIVITY_SERVICE)` will throw a SecurityException the moment you try to use the manager. Very easy trap to fall into, so declare it first.

---

## The Complete Kotlin File

**[Show on screen — the complete file:]**

```kotlin
package com.example.connectivitypermission

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var textNetworkStatus: TextView
    private lateinit var buttonCheckNetwork: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        textNetworkStatus = findViewById(R.id.textNetworkStatus)
        buttonCheckNetwork = findViewById(R.id.buttonCheckNetwork)

        buttonCheckNetwork.setOnClickListener {
            textNetworkStatus.text = getNetworkStatus()
        }
    }

    private fun getNetworkStatus(): String {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork
            ?: return "No network connection"

        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return "No network connection"

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                if (hasInternet) "Connected via Wi-Fi" else "Wi-Fi connected (no internet)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                if (hasInternet) "Connected via Cellular" else "Cellular connected (no internet)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                if (hasInternet) "Connected via Ethernet" else "Ethernet connected (no internet)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ->
                "Connected via VPN"
            else -> "Connected (unknown network type)"
        }
    }
}
```

Here's the finished file top to bottom. Imports at the top, class with two `lateinit` properties, `onCreate` that wires the views and registers the click listener, and the private `getNetworkStatus` method that talks to ConnectivityManager and builds the status message.

---

## Live Demo Walkthrough

**[Show: running the app on an emulator with Wi-Fi on. Tap the button. Message updates to "Connected via Wi-Fi".]**

Let's run it. When the app opens, the TextView still shows the instruction we set in the XML — "Tap the button to check network status". I tap the button, `onClick` runs, `getNetworkStatus` runs, it walks through ConnectivityManager, sees I'm on Wi-Fi with real internet, and returns "Connected via Wi-Fi". The TextView updates.

**[Toggle Wi-Fi off in the emulator, tap the button again. Message updates to "No network connection".]**

Now let me turn off Wi-Fi and turn off cellular. Tap the button again. This time `activeNetwork` is null, our Elvis operator kicks in, and we return "No network connection" immediately. The TextView reflects that.

**[Turn Wi-Fi back on, tap once more.]**

Turn Wi-Fi back on, tap once more, and we're back to "Connected via Wi-Fi". This is exactly what we wanted.

---

## Outro

**[Show: final app in action, tapping the button and watching the status change]**

That wraps up Stage 2 and the whole mini-project. We took an empty MainActivity, hooked up the layout, and used ConnectivityManager along with NetworkCapabilities to report the current network state in a human-readable way. The whole thing is under fifty lines of code.

From here you could take this further — register a `NetworkCallback` so the text updates automatically whenever the network changes without the user needing to press the button. Show a small icon next to the text for each transport type. Or expand it to actually try pinging a server to verify real connectivity beyond what the capability flag claims.

Thanks for watching, and I'll see you in the next project.
