# Network Status App — Stage 3: Understanding Permissions and Fixing the Crash (Video Transcript)

A detailed transcript explaining what permissions are in Android, why our app just crashed, and how to fix it by declaring the right permission in the manifest. Each section shows what to display on screen followed by the narration.

---

## Intro

**[Show: the running app. The user taps the "Check Network Status" button. The app crashes with the "keeps stopping" dialog.]**

Alright — take a look at what just happened. I run the app, the layout looks fine, everything from Stage 2 is in place. I tap the "Check Network Status" button, expecting to see "Connected via Wi-Fi" appear on the screen. Instead, the app crashes. The system shows the "keeps stopping" dialog.

This isn't a bug in our Kotlin logic. Our code is correct. What's actually happening is that Android is blocking us from reading the network state because we haven't asked for permission to do so. And that's the perfect entry point for the real topic of this app — **Android permissions**.

In this stage I'm going to explain what permissions are, why Android has them, why our specific call crashed, and how to fix it by declaring the right permission in the manifest file. By the end you'll not just have a working app — you'll actually understand what the permission system is doing behind the scenes.

Let's dive in.

---

## Part 1 — What Are Permissions in Android?

**[Show on screen: a simple diagram or slide with "Permissions = Boundaries around sensitive things"]**

Let me start with the big picture. What is a permission, really?

Android is an operating system where hundreds of apps live side by side on your phone. Some of those apps are yours, some come from strangers you barely trust, and all of them share the same hardware — the camera, the microphone, your contacts, your location, your files, and yes, information about your network. If any app could freely read any of those things, that would be a privacy and security disaster.

So Android draws a line around each sensitive capability. On one side of the line is normal app code that anyone can call — displaying text, changing colors, drawing a button. On the other side of the line are the sensitive APIs — reading contacts, tracking location, accessing the microphone, seeing which network you're on. To cross that line, your app has to explicitly declare that it needs to. That declaration is called a **permission**.

Think of a permission like a badge at an office building. You can walk into the lobby without a badge — that's your normal app. But to get into the server room, you need a badge that says "authorized to enter server room". Android's permission system is that badge system, applied to sensitive OS capabilities.

There are hundreds of these permissions. Some examples: `INTERNET` to open network connections, `CAMERA` to take pictures, `READ_CONTACTS` to see the address book, `ACCESS_FINE_LOCATION` to get GPS coordinates, and — the one relevant to us today — `ACCESS_NETWORK_STATE` to read information about network connectivity.

---

## Part 2 — The Four Categories of Permissions

**[Show on screen: a slide with four categories: Normal, Dangerous, Signature, Special/AppOp]**

Android sorts permissions into a few categories based on how risky they are.

**Normal permissions** are things that pose very little privacy risk. Reading the network state, changing the vibration setting, setting an alarm. Android grants these automatically at install time. You just have to declare them in the manifest — no dialog, no user prompt, no runtime request. `ACCESS_NETWORK_STATE` — the one we need — is in this category. That's why fixing our crash is going to be very simple.

**Dangerous permissions** are the ones that touch real personal data. Camera, microphone, contacts, calendar, precise location. These require both a manifest declaration AND a runtime prompt where the user sees a dialog and chooses "Allow" or "Deny". Since Android 6, dangerous permissions can't be granted automatically — the user makes the call while the app is running.

**Signature permissions** are for apps signed with the same key as another app, often used by system apps and OEM apps to share data safely. Most everyday app developers never touch these.

**Special or AppOps permissions** are the extra-sensitive ones — draw over other apps, ignore battery optimizations, install other apps. These require the user to go into Settings and toggle a switch — a regular in-app dialog isn't enough.

For today, we only care about the first category — normal permissions. That's the bucket `ACCESS_NETWORK_STATE` lives in.

---

## Part 3 — Reading the Crash Log

**[Show on screen: the Logcat error output, highlighting the key lines]**

```
E  FATAL EXCEPTION: main
   Process: com.example.connectivitypermission, PID: 32094
   java.lang.SecurityException: ConnectivityService: Neither user 10229 nor current process has android.permission.ACCESS_NETWORK_STATE.
       at android.net.ConnectivityManager.getActiveNetwork(ConnectivityManager.java:1043)
       at com.example.connectivitypermission.MainActivity.getNetworkStatus(MainActivity.kt:40)
       at com.example.connectivitypermission.MainActivity.onCreate$lambda$0(MainActivity.kt:28)
```

Let's look at the crash log because it tells us exactly what went wrong.

The first line says `FATAL EXCEPTION: main`. "Fatal" means unrecoverable — the app couldn't continue running. "Main" is the main UI thread, so the crash happened in response to a UI event, in our case the button tap.

The second line, `Process: com.example.connectivitypermission`, confirms it's our app that crashed.

Then the important line — `java.lang.SecurityException: ConnectivityService: Neither user 10229 nor current process has android.permission.ACCESS_NETWORK_STATE`.

Let me unpack this. `SecurityException` is the Java exception type Android throws whenever a permission is missing. So this specific class of exception almost always means "you forgot a permission somewhere". The message spells out exactly which permission — `android.permission.ACCESS_NETWORK_STATE`. Android is basically saying to us, "Hey, I would tell you about the network, but you never asked for the badge."

The stack trace below shows where the error originated. `ConnectivityManager.getActiveNetwork` on line 1043 of the framework — that's Android's code. Below that, `MainActivity.getNetworkStatus` at line 40 of our file — that's the exact line where we called `connectivityManager.activeNetwork`. And below that, the click handler from line 28.

So the flow is: user taps button → click handler runs → click handler calls `getNetworkStatus` → inside that method, `connectivityManager.activeNetwork` runs → Android checks whether our app has the permission → we don't → Android throws SecurityException → the exception bubbles up and no one catches it → the whole app dies.

The fix is simple. We need to add the permission.

---

## Part 4 — The Fix: Editing AndroidManifest.xml

**[Show on screen: open AndroidManifest.xml, currently without the permission line]**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.ConnectivityPermission"
        tools:targetApiVersion="31">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

Now let's fix the crash. Every Android app has a file called `AndroidManifest.xml`. It sits under `app/src/main/AndroidManifest.xml`. This file is the app's official "declaration of intent" to the operating system. It tells Android things like: what your app is called, what activities it has, what intents each activity responds to, what SDK version you target, and — most importantly for us right now — what permissions it needs.

Here's what our manifest looks like right now. There's a `<manifest>` root, an `<application>` inside it, and one `<activity>` for `MainActivity`. Notice there is no `<uses-permission>` tag anywhere. That's why Android has no idea we intended to ask for network access.

---

## Part 5 — Adding the `<uses-permission>` Tag

**[Show on screen: the manifest with the new permission line highlighted]**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.ConnectivityPermission"
        tools:targetApiVersion="31">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

Here's the one line I've added:

`<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`

Let me break this down.

`<uses-permission ...>` is the XML tag Android looks for when it wants to know which permissions your app is requesting. Any permission your app needs — normal, dangerous, whatever category — you declare with this tag.

`android:name="android.permission.ACCESS_NETWORK_STATE"` is the fully-qualified name of the permission we want. `android.permission.` is the standard prefix for platform permissions. `ACCESS_NETWORK_STATE` is the specific one that unlocks the ConnectivityManager APIs we're using.

I've placed this tag between the closing of the manifest's opening tag and the opening of the `<application>` tag. That's the conventional spot. Technically you can put it anywhere directly inside `<manifest>` and Android will find it, but keeping it at the top makes it easy to see all your permission declarations in one glance.

Notice the tag is self-closing — it ends with `/>` because it has no children.

That's it. One line. Save the file.

---

## Part 6 — Why We Don't Need a Runtime Prompt

**[Show on screen: a slide comparing normal vs dangerous permission flow]**

You might be wondering — do we need to do anything else in the Kotlin code? Are we going to see a permission dialog when the app runs?

For `ACCESS_NETWORK_STATE`, no. Remember from Part 2, this is a **normal** permission. Normal permissions are granted automatically at install time — no runtime prompt, no `ActivityCompat.requestPermissions` call in your code, no permission dialog shown to the user. The moment the app is installed, Android sees the manifest declaration, decides the permission is low-risk, and grants it silently.

If we were reading location or accessing the camera, this would be a different story — we'd have to add a runtime request in Kotlin, handle the "user tapped Deny" case, maybe show a rationale explaining why we need the permission. But for reading network state, the manifest line is the entire fix.

That's a nice contrast to keep in mind — the flow depends on the permission's risk level.

---

## Part 7 — Rebuilding and Running

**[Show: rebuild the project, then run on the emulator. The layout appears. Tap the button. This time the TextView updates with "Connected via Wi-Fi".]**

Alright, moment of truth. I save the manifest, hit the run button, wait for the app to reinstall on the emulator. The layout comes up looking the same — TextView on top, button below. I tap the button.

This time, no crash. The TextView updates to "Connected via Wi-Fi". Perfect.

What just happened under the hood: the click handler ran, called `getNetworkStatus`, which called `connectivityManager.activeNetwork`. Android's Connectivity Service asked "does this app have `ACCESS_NETWORK_STATE`?" This time the answer was yes, because we declared it in the manifest and the OS granted it at install time. So the service handed back a real Network object, we read its capabilities, saw the Wi-Fi transport, saw the internet capability, and returned "Connected via Wi-Fi". The TextView updated. No exception, no crash, no fatal error.

We went from "app crashes on tap" to "app works" by adding exactly one line to one file.

---

## Part 8 — Debugging Tips for Permission Errors

**[Show on screen: a slide with three debugging tips]**

Since you're going to hit permission issues plenty of times in your Android career, let me leave you with three quick tips.

**One.** Any time you see `java.lang.SecurityException` in the log, the very first thing you should suspect is a missing permission. The exception message will name the permission — that's your clue. Look for it right after "Neither user X nor current process has..." — the string right there is what you need to declare.

**Two.** After you add a permission to the manifest, always uninstall and reinstall the app — or at least do a fresh install, not just a hot reload. Some Android versions cache the granted-permission list at install time, and hot-reloading code without reinstalling doesn't refresh that list. If your fix "doesn't work", uninstall first and try again.

**Three.** If the permission is a dangerous one, adding it to the manifest alone isn't enough — you also need a runtime request. The failure mode looks the same at first — SecurityException — so if you added the manifest line and still see the exception, check whether the permission is normal or dangerous. The official docs list which is which. For dangerous ones, you'll also need `ActivityCompat.requestPermissions` and an `onRequestPermissionsResult` callback.

---

## Outro

**[Show: the fixed app in action, tapping the button and watching the network status appear]**

And that wraps up this stage. We saw an app crash the moment we tried to touch a protected API. We used the crash log to identify exactly which permission was missing. We added a single `<uses-permission>` tag to `AndroidManifest.xml`, and the app works.

But more importantly, we now understand *why* it works. Android's permission system is a security boundary around sensitive OS capabilities. Every app has to declare which sensitive capabilities it needs. Normal permissions like ours are granted automatically at install time. Dangerous ones require an additional runtime dialog. And skipping the declaration doesn't just cause a subtle bug — it causes an immediate SecurityException the moment you touch the API.

Next time you build an Android feature and you're not sure whether it needs a permission, the rule of thumb is: if it touches the user's data, hardware, or personal information, it probably does. Check the docs, add the manifest line, and if the API is dangerous, add the runtime request too.

Thanks for watching. If this helped clear up the permission model for you, please leave a like and subscribe, and I'll see you in the next one.
