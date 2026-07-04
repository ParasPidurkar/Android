# Phone Dialer App — Stage 2: The Dangerous Permission Flow (Video Transcript)

A detailed line-by-line transcript for the second stage of the Phone Dialer app, where we wire up the button, request the `CALL_PHONE` permission at runtime, and place the call. Each section shows the exact code to display on screen followed by the narration.

---

## Intro

**[Show: the app from Stage 1 — user taps the input, types a number, taps Dial. Nothing happens.]**

Welcome back to Stage 2 of the Phone Dialer app. In the last stage we built the layout — a label, a phone-optimized input field, and a Dial button. But right now the button does absolutely nothing. Tap it and the app just sits there.

In this stage we're going to fix that, and along the way we're going to see something we haven't dealt with yet — a **dangerous permission**. Unlike the network-state permission from our previous project, which Android grants silently at install time, dangerous permissions require an actual runtime dialog. We have to ask the user in person, wait for their answer, and respond gracefully whether they say Allow or Deny.

`CALL_PHONE` is the permission we need to place a call directly. It's dangerous because letting an app dial any number in the background, without the user seeing what's happening, could be abused — think toll-fraud malware racking up premium-rate calls. So Android forces us through a runtime request every time. Let's build it.

---

## The Starting Point

**[Show on screen — the file as Android Studio left it:]**

```kotlin
package com.example.dangerouspermission

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

Here's the empty MainActivity Android Studio gave us. Standard boilerplate — package, imports, class extending AppCompatActivity, an `onCreate` with `super.onCreate`, `enableEdgeToEdge`, and `setContentView`. Everything after `setContentView` is empty, and that's where our work begins.

---

## Adding the Imports

**[Show on screen — the new import block:]**

```kotlin
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
```

We have a lot of new imports because dangerous permissions touch several parts of the Android SDK. Let me walk through each new one.

`android.Manifest` — this is the class that holds constants for every platform permission. When we need to name the `CALL_PHONE` permission, we'll write `Manifest.permission.CALL_PHONE`, which is safer than typing the string manually.

`android.content.Intent` — Intents are Android's messaging system. To launch the phone dialer we're going to build an Intent with the `ACTION_CALL` action.

`android.content.pm.PackageManager` — this contains constants like `PERMISSION_GRANTED` that we compare against when checking whether we already have a permission.

`android.net.Uri` — Uri is Android's URI class. We're going to build a `tel:` URI wrapping the phone number.

`android.widget.EditText` and `android.widget.Toast` — the EditText widget for the input field, and Toast for those little floating messages that pop up briefly at the bottom of the screen. Button was already implied but we add it too.

`androidx.activity.result.contract.ActivityResultContracts` — this is the modern class for launching activities and permission requests and getting their results. It replaces the old-and-deprecated `onActivityResult` and `onRequestPermissionsResult` pattern.

`androidx.core.content.ContextCompat` — a compatibility wrapper around Context. We use it for `checkSelfPermission`, which works the same across all Android versions.

The two `ViewCompat` and `WindowInsetsCompat` imports the template gave us are unused, so I've dropped them.

---

## The `lateinit` Properties

**[Show on screen — the two view properties:]**

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var editPhoneNumber: EditText
    private lateinit var buttonDial: Button
```

At the top of the class we declare the two views we'll interact with. `editPhoneNumber` for the input field, `buttonDial` for the button. Both are `private lateinit var` — private because nothing outside the class needs them, `lateinit` because they can't be initialized until `setContentView` runs.

---

## Registering the Permission Launcher

**[Show on screen — the permission launcher property, highlighted:]**

```kotlin
// Registered permission launcher — shows the system permission dialog when
// .launch() is called, then delivers the user's decision back to this
// callback (true = granted, false = denied).
private val callPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            placeCall()
        } else {
            Toast.makeText(
                this,
                "Permission denied — can't place the call.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
```

This is the heart of the modern permission flow. Let me unpack it very carefully because there's a lot here.

`private val callPermissionLauncher = ...` declares a read-only property. Once we set it up, we won't reassign it.

`registerForActivityResult(...)` is a method on AppCompatActivity that tells Android "I'm going to launch something later, and here's how I want the result delivered when it comes back". It returns an `ActivityResultLauncher` object that we can hold onto and later call `.launch()` on.

The first argument, `ActivityResultContracts.RequestPermission()`, is called a **contract**. There are contracts for taking a picture, picking a file, requesting a permission, etc. `RequestPermission` is the contract that says "I'm going to ask for one permission, and the result will be a Boolean — true if granted, false if denied."

The second argument — the lambda in curly braces — is the callback. It receives the result once the user has made their choice. The parameter `granted` is that Boolean.

Inside the callback we branch on the result. If `granted` is true, we call `placeCall()`, which is a method we'll write below that actually fires the call intent. If `granted` is false, we show a Toast saying "Permission denied — can't place the call."

`Toast.makeText(this, message, duration).show()` is the standard pattern for showing a Toast. `this` is the Context. The message is our string. `Toast.LENGTH_SHORT` shows it for about two seconds. And `.show()` is what actually displays it — a common gotcha is that `makeText` alone doesn't display anything.

Now here's the really important detail about *where* we register this launcher. It's declared at the class level as a property, not inside `onCreate`. This matters. The registration must happen before the Activity reaches its "started" state — that means either as a property initializer or in `onCreate` before `super.onCreate` completes. If you tried to register it later, say inside the button's click listener, Android would throw an exception. Declaring it as a property is the cleanest way to guarantee correct timing.

---

## Wiring Up the Views

**[Show on screen — the view-lookup lines inside `onCreate`:]**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_main)

    // Link XML views to Kotlin variables.
    editPhoneNumber = findViewById(R.id.editPhoneNumber)
    buttonDial = findViewById(R.id.buttonDial)
```

Standard view wiring. The top three lines — `super.onCreate`, `enableEdgeToEdge`, `setContentView` — are exactly what the template gave us. Then we call `findViewById` for each view and assign them to our `lateinit` properties.

Nothing surprising here. If you followed the previous transcripts this should feel familiar.

---

## The Click Listener — Empty Input Guard

**[Show on screen — the top of the click listener:]**

```kotlin
buttonDial.setOnClickListener {
    // Nothing entered? Tell the user and stop.
    if (editPhoneNumber.text.isBlank()) {
        Toast.makeText(this, "Enter a phone number first.", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
    }
```

Now the click listener. The first thing it does is a small sanity check.

`if (editPhoneNumber.text.isBlank())` — we ask the EditText for its text and use Kotlin's `isBlank()`, which returns true if the string is empty or contains only whitespace. If the user tapped Dial without typing anything, there's nothing to call.

We show a Toast telling them to enter a number first, then `return@setOnClickListener` bails out of the lambda. The `@setOnClickListener` label is Kotlin's way of specifying which function you're returning from — because we're inside a nested lambda, a bare `return` wouldn't work the way you'd expect. This label makes the intent explicit.

Small polish, but it prevents the app from trying to dial an empty number and looking broken.

---

## Checking If We Already Have Permission

**[Show on screen — the permission check:]**

```kotlin
    // Check if CALL_PHONE has already been granted for this app.
    val alreadyGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CALL_PHONE
    ) == PackageManager.PERMISSION_GRANTED

    if (alreadyGranted) {
        // Already have permission → place the call immediately.
        placeCall()
    } else {
        // No permission yet → ask the user. The result comes back to
        // callPermissionLauncher's callback above.
        callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
    }
}
```

This is the branch that decides "do I ask, or do I just go?"

`ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)` asks the system "does my app currently have this permission granted?" It returns an integer that's either `PackageManager.PERMISSION_GRANTED` or `PackageManager.PERMISSION_DENIED`. We compare it against `PERMISSION_GRANTED` and store the boolean result in `alreadyGranted`.

Why `ContextCompat` instead of just calling `checkSelfPermission` on the Activity directly? Because `checkSelfPermission` was only added to Context in API level 23, and `ContextCompat` handles the compatibility layer for older devices. Using the compat version is the safe habit.

Then the branch:

If `alreadyGranted` is true, the user already gave us permission at some earlier point in this or a previous session, and Android remembered. We can go straight to `placeCall()` — no dialog needed.

If `alreadyGranted` is false, we call `callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)`. This does two things: it displays the system's permission-request dialog, and it registers that when the user chooses, the result should be delivered to our launcher's callback — the one we defined at the top of the class.

Note how clean this is. We don't have to manually override `onRequestPermissionsResult` and match request codes. The launcher handles the plumbing.

---

## The `placeCall` Method

**[Show on screen — the private helper method:]**

```kotlin
// Fires the ACTION_CALL intent which places the call immediately without
// opening the dialer UI. This is why CALL_PHONE is a *dangerous* permission.
private fun placeCall() {
    val number = editPhoneNumber.text.toString().trim()
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
    startActivity(intent)
}
```

This is the method that actually places the call, called only after we've confirmed we have permission.

`val number = editPhoneNumber.text.toString().trim()` reads the current text from the EditText, converts it to a plain String — `.text` returns an `Editable`, not a String, so we call `toString()` — and trims any leading or trailing whitespace with `.trim()`.

`Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))` builds an Intent. Intents in Android are messages — you can think of them as "here's what I want to happen". The first argument, `Intent.ACTION_CALL`, is the action — "place a phone call". The second argument is the data — a URI describing what to call.

`Uri.parse("tel:$number")` builds a `tel:` scheme URI. The `tel:` scheme is a standard URI that phone-related intents understand. If the number is `+15551234567`, the URI is `tel:+15551234567`. The `$number` is Kotlin's string-template syntax — it inlines the value of the variable.

`startActivity(intent)` fires the intent. The Android system looks at the action and data, finds the app registered to handle `ACTION_CALL` for `tel:` URIs — normally the built-in phone app — and hands control over.

Now here's the really important detail. `ACTION_CALL` doesn't open the dialer UI and pre-fill the number — it just dials immediately. That's why it needs `CALL_PHONE`. There's a related action called `ACTION_DIAL` that opens the dialer with the number pre-filled and doesn't require any permission at all. `ACTION_DIAL` is safer, but for this project we're using `ACTION_CALL` on purpose because the whole point is to demonstrate the dangerous-permission flow. In a real app, you should almost always prefer `ACTION_DIAL` — let the user hit the green call button themselves.

---

## The Manifest Permission

**[Show on screen — the manifest line to add:]**

```xml
<uses-permission android:name="android.permission.CALL_PHONE" />
```

Just like last time, we need to declare the permission in `AndroidManifest.xml`. Open the file and add this line inside the `<manifest>` tag, above `<application>`.

But there's a critical difference from the previous app. Last time, that manifest line was the *entire* fix — because `ACCESS_NETWORK_STATE` was a normal permission, Android granted it automatically. This time, `CALL_PHONE` is dangerous. The manifest line is required — without it, Android won't even show the permission dialog when we call `launch()` — but the manifest line is only half the story. We also need the runtime `.launch()` call we already wrote to actually get the permission granted.

So: manifest declares intent, `checkSelfPermission` checks current state, `launcher.launch()` asks the user, callback handles the answer. All four moving parts have to be right.

---

## The Complete Kotlin File

**[Show on screen — the complete finished file:]**

```kotlin
package com.example.dangerouspermission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var editPhoneNumber: EditText
    private lateinit var buttonDial: Button

    private val callPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                placeCall()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied — can't place the call.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        editPhoneNumber = findViewById(R.id.editPhoneNumber)
        buttonDial = findViewById(R.id.buttonDial)

        buttonDial.setOnClickListener {
            if (editPhoneNumber.text.isBlank()) {
                Toast.makeText(this, "Enter a phone number first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val alreadyGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED

            if (alreadyGranted) {
                placeCall()
            } else {
                callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    private fun placeCall() {
        val number = editPhoneNumber.text.toString().trim()
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        startActivity(intent)
    }
}
```

Here's the whole file, top to bottom. Imports, two `lateinit` view properties, the registered permission launcher, `onCreate` doing view wiring and the click listener, and the `placeCall` helper at the bottom.

---

## Live Demo Walkthrough

**[Show: run the app on the emulator. Type a number. Tap Dial.]**

Let's run it and watch every branch of the flow.

**First run.** The app opens, I type a number into the input field, and tap Dial. `onClick` runs. The empty check passes because I typed something. We call `checkSelfPermission` — no permission yet, so `alreadyGranted` is false. We hit the `else` branch and call `callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)`.

**[Show: the system permission dialog appears — "Allow Dangerous Permission to make and manage phone calls?" with Allow / Don't allow buttons.]**

Android shows this dialog. This is the runtime permission prompt. It says our app's name, describes what permission we're asking for, and gives the user two buttons. The exact wording differs slightly between Android versions but the meaning is always the same. This dialog is drawn by the system, not by us — we can't customize it. That's a security feature, so bad actors can't disguise it.

I tap Allow.

**[Show: the phone app opens and starts dialing the number.]**

The dialog dismisses. Behind the scenes, the launcher's callback is invoked with `granted = true`. Our callback calls `placeCall()`, which builds the `ACTION_CALL` intent with the `tel:` URI and calls `startActivity`. The phone app takes over and dials the number.

**[Emulator note: on an emulator without a SIM the call obviously can't complete, but you'll see the dialer screen open and try — that's proof the permission and intent flow worked.]**

**Second tap.** Now let's say the user reopens the app, types a number, taps Dial. This time `checkSelfPermission` returns `PERMISSION_GRANTED` — Android remembered our approval. `alreadyGranted` is true, and we go straight to `placeCall`. No dialog, no re-prompt. That's the exact behavior a good app should have — don't nag the user.

**Denial path.** Uninstall and reinstall the app to reset the permission. Tap Dial, see the dialog, tap Don't Allow. This time the callback receives `granted = false`, and we show the Toast "Permission denied — can't place the call." The call never happens. The user sees a friendly message instead of a crash.

---

## Outro

**[Show: the app in action, going through the permission flow and successfully dialing]**

That wraps up Stage 2. Let's zoom out on what we learned.

Dangerous permissions are handled fundamentally differently from normal ones. The manifest declaration is required, but by itself it does nothing at runtime — you also have to explicitly request the permission using the modern `registerForActivityResult` API, check the current grant state with `ContextCompat.checkSelfPermission`, and gracefully handle both the granted and denied paths. All four moving parts have to be correct.

We also used our first real Intent to hand off work to another app. `Intent.ACTION_CALL` combined with a `tel:` URI hands the phone number to whichever app is registered as the dialer. Intents are how Android apps compose and cooperate.

There's still more to explore. In the next stage — if we do one — we could handle the "denied and don't ask again" edge case, or add a rationale dialog that appears before the system prompt to explain *why* we need the permission. Both are polish steps that turn a good app into a great one.

Thanks for watching. Next time you're building an Android feature and you're not sure whether a permission is dangerous, remember the rule of thumb — if it touches personal data, contacts, the camera, the microphone, precise location, or does something the user would be scared to see happen behind their back, it's dangerous, and you need the runtime flow we just wrote.

See you in the next one.
