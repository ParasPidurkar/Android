# Phone Dialer App — Stage 3: Permissions Beyond "Normal" (Video Transcript)

A detailed transcript explaining the permission categories that go beyond the "normal" bucket — dangerous permissions, signature permissions, and special / AppOps permissions — with concrete examples, when to use each, and what the developer flow looks like for each.

---

## Intro

**[Show: a slide titled "Beyond Normal Permissions" with three boxes labeled Dangerous, Signature, Special]**

Hi everyone. In our earlier Network Status app we saw the simplest kind of Android permission — a **normal** permission. We declared `ACCESS_NETWORK_STATE` in the manifest and Android silently granted it at install time. Done. No dialog. No runtime work.

But in the Phone Dialer app we just built, that story wasn't enough. We had to add a runtime request, wait for a system dialog, handle Allow, handle Deny. Why? Because `CALL_PHONE` isn't normal — it's **dangerous**.

In this stage I want to zoom out and go deep on every permission category that lives beyond normal. There are three big ones — dangerous, signature, and special (also called AppOps). Each is protected differently, each has a different developer flow, and each exists because some capabilities are riskier than others. Understanding the differences will save you hours of debugging and, more importantly, help you write safer apps.

Let's get into it.

---

## Part 1 — Quick Recap of "Normal"

**[Show on screen: a compact recap slide]**

Before we go beyond normal, let me remind you what normal actually means, because everything downstream is easier to understand in contrast.

Normal permissions are low-risk. They let your app do something slightly beyond the default sandbox, but they don't expose sensitive user data or hardware. Examples include reading the network state, setting an alarm, changing vibration, using the fingerprint sensor for biometric UI (not fingerprint data itself), and setting the wallpaper.

The developer flow for a normal permission is one step — declare it in the manifest:

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

That's it. Install-time grant. No runtime prompt. No user dialog. No Kotlin code.

Everything I'm about to talk about is harder than that.

---

## Part 2 — Dangerous Permissions

**[Show on screen: a slide listing common dangerous permission groups: Location, Camera, Microphone, Contacts, Phone, SMS, Storage, Calendar, Sensors]**

Dangerous permissions guard access to the things that would make you nervous if a random app could touch them without asking. Your location. Your camera. Your microphone. Your contacts. Your text messages. Your files. And, as we saw, your ability to place phone calls.

Android groups these into permission groups. Historically, granting one permission in a group automatically granted the rest, but that behavior was removed in Android 10 — today, every dangerous permission is granted individually. Still, thinking in groups is a useful mental model for you as a developer, because dangerous permissions come in themed clusters.

Some concrete examples of dangerous permissions:

- Under **Location**: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`
- Under **Camera**: `CAMERA`
- Under **Microphone**: `RECORD_AUDIO`
- Under **Contacts**: `READ_CONTACTS`, `WRITE_CONTACTS`, `GET_ACCOUNTS`
- Under **Phone**: `CALL_PHONE`, `READ_PHONE_STATE`, `READ_CALL_LOG`, `WRITE_CALL_LOG`, `USE_SIP`
- Under **SMS**: `SEND_SMS`, `RECEIVE_SMS`, `READ_SMS`
- Under **Calendar**: `READ_CALENDAR`, `WRITE_CALENDAR`
- Under **Sensors**: `BODY_SENSORS`, `ACTIVITY_RECOGNITION`
- Under **Storage** (deprecated on newer Android, replaced by scoped storage): `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`

These are the ones that Google Play flags for extra review, that generate the loudest privacy complaints, and that carry the biggest security consequences if misused.

---

## Part 3 — The Dangerous Permission Flow

**[Show on screen: a flowchart — Manifest → checkSelfPermission → if granted → do the thing / if not → request → callback with result]**

The developer flow for a dangerous permission has four moving parts. We've seen all of them in the dialer app, so let me summarize the pattern in abstract terms.

**Step 1 — Declare it in the manifest.** Just like normal permissions:

```xml
<uses-permission android:name="android.permission.CALL_PHONE" />
```

Without this, the runtime request will fail silently. Android decides "you didn't ask for this permission, so I won't even show the dialog." This is a really common gotcha — developers add the runtime code and forget the manifest line.

**Step 2 — Check the current grant state.** Before doing the sensitive thing, always ask "do I already have permission?" using `ContextCompat.checkSelfPermission`. If you already have it — because the user granted it earlier — you can skip the dialog and just do the work. Users hate being re-prompted for things they've already approved.

**Step 3 — If you don't have it, request it.** Launch the modern `registerForActivityResult` launcher. Android shows the system dialog. You don't design that dialog — the OS draws it, so no one can spoof it. Bad actors can't put "Allow this evil thing?" behind a friendly-looking prompt of their own design.

**Step 4 — Handle the result in the callback.** The user tapped Allow or Deny. Your callback receives a Boolean. Handle both paths gracefully — do the work if granted, show a helpful message if denied, and never crash.

That's the pattern for every single dangerous permission. Camera, mic, location, contacts, SMS — it's always these four steps.

---

## Part 4 — The "Deny and Don't Ask Again" Case

**[Show on screen: a screenshot of Android's permission dialog on API 30+, showing "While using the app", "Only this time", "Don't allow"]**

Here's a critical wrinkle we haven't seen yet. The permission dialog doesn't just have Allow and Deny anymore. On modern Android, the choices are more nuanced.

On Android 11 and later, common dangerous permissions like location, camera, and microphone show three options:

- **While using the app** — granted, but only when your app is in the foreground
- **Only this time** — granted for this session only (a one-time permission)
- **Don't allow** — denied

If the user picks "Only this time", the permission is revoked when the app goes to the background for a while. Your app has to be prepared to re-request it the next time it needs it.

If the user denies twice in a row — or picks a stronger "Don't allow" — Android silently ignores subsequent request calls. Your `.launch()` call will look like it worked, but the callback fires almost immediately with `granted = false` and no dialog is ever shown. This is Android's "leave the user alone" mechanism.

The proper handling for this involves a method called `shouldShowRequestPermissionRationale`. It's a slightly awkwardly named function that returns true if the user has denied the permission at least once but hasn't hit "don't ask again". You call it before requesting, and if it returns true, you show your own explanation dialog — "Hey, we need microphone access for voice notes, here's why" — before triggering the system prompt.

If `shouldShowRequestPermissionRationale` returns false AND the permission is not granted, either the user has never seen the dialog before (first request — just ask) or they've selected "don't ask again" (send them to Settings so they can flip the toggle manually — you can't ask again through code).

Handling all this correctly is what separates a well-behaved app from an annoying one.

---

## Part 5 — Signature Permissions

**[Show on screen: a slide labeled "Signature — Only Apps Signed by the Same Key"]**

Now let's talk about signature permissions. These are less commonly seen by app developers, but they exist and it's worth understanding.

A signature permission is granted only if the app requesting it is signed with the exact same signing key as the app or system component that declared the permission. If the signing keys don't match, Android refuses. There's no user dialog, no Settings toggle — just a hard binary check.

**Where you see them.** System apps that ship with the operating system use signature permissions heavily to talk to each other. For example, Google Play Services uses signature permissions so only apps signed by Google can access certain internal APIs. Some phone manufacturers use signature permissions for OEM services that only their own factory apps can call.

**Where regular developers use them.** If your company ships two apps and you want them to share data privately — say a paid app and a companion widget — you can define your own signature permission in one app and require it in the other. Because both are signed with your key, they can talk. Any third-party app would be refused.

**The flow.** You declare a signature permission in your app's manifest using `<permission>` with `android:protectionLevel="signature"`. Then in the other app you declare `<uses-permission>` with the same name. If both apps ship with your signing key, done. If not, denied.

Signature permissions are the least visible category — no user prompts, no toggles, no dialogs. Either the signing key matches or it doesn't.

---

## Part 6 — Special Permissions (AppOps)

**[Show on screen: a slide labeled "Special — The Settings Toggle" with examples like SYSTEM_ALERT_WINDOW, MANAGE_EXTERNAL_STORAGE]**

The last category is the odd one — special permissions, sometimes called AppOps because they're managed by the internal AppOpsManager.

These are the most sensitive permissions Android has. They're so sensitive that a simple in-app dialog isn't enough — the user has to physically go to Settings and toggle a switch. Google doesn't want a fast tap on "Allow" to hand these away.

**Concrete examples.**

- `SYSTEM_ALERT_WINDOW` — draw over other apps. Used by things like Facebook Messenger chat heads. Also heavily abused by malware, which is exactly why it's gated so strictly.
- `MANAGE_EXTERNAL_STORAGE` — full unrestricted access to the shared storage. Modern Android really wants you to use scoped storage instead, but if you're building a file manager you might genuinely need this.
- `WRITE_SETTINGS` — modify system settings.
- `REQUEST_INSTALL_PACKAGES` — install other APKs. Used by app stores and updater apps.
- `PACKAGE_USAGE_STATS` — see which apps the user has been using and when. Used by parental-control and digital-wellbeing apps.
- `BIND_ACCESSIBILITY_SERVICE` — implement an accessibility service. Extremely powerful, extremely abusable.

**The flow.** You still declare these in the manifest with `<uses-permission>`. But to actually request them, you don't launch a permission dialog — you launch an Intent to a specific Settings screen. For `SYSTEM_ALERT_WINDOW`, that's `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`. For `MANAGE_EXTERNAL_STORAGE`, it's `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`. Android takes the user to Settings, they toggle the switch for your app, and they come back. You then re-check whether the permission is granted using a special API — not `checkSelfPermission`, but a permission-specific check like `Settings.canDrawOverlays(context)`.

Special permissions are annoying to work with, and that's by design. Google is making friction on purpose so users think twice.

---

## Part 7 — Category Comparison at a Glance

**[Show on screen: a comparison table]**

Let me summarize everything into one table.

**Normal.** Manifest only. Auto-granted at install. Example: `ACCESS_NETWORK_STATE`, `INTERNET`, `VIBRATE`.

**Dangerous.** Manifest + runtime request + user dialog + result callback. Can be denied, can be revoked in Settings. Example: `CAMERA`, `RECORD_AUDIO`, `CALL_PHONE`, `ACCESS_FINE_LOCATION`.

**Signature.** Manifest only. Granted if signing keys match, otherwise refused. No user interaction. Example: signature permissions between two apps by the same developer, or internal system-app permissions.

**Special / AppOps.** Manifest + open a Settings screen + toggle. Cannot be granted by an in-app dialog. Example: `SYSTEM_ALERT_WINDOW`, `MANAGE_EXTERNAL_STORAGE`, `REQUEST_INSTALL_PACKAGES`.

The key question when you're picking a permission is "which category is this in?" That determines everything about the developer flow. Google's official documentation lists the category for every permission — always check it before you start writing code.

---

## Part 8 — One-Time and Auto-Reset Permissions

**[Show on screen: a slide about Android 11+ auto-reset]**

Two related features worth mentioning because they change how dangerous permissions behave over time.

**One-time permissions.** As I mentioned earlier, since Android 11, dangerous permissions related to location, camera, and microphone can be granted "Only this time". These are granted for the current session only and revoked once your app has been in the background long enough. Your code must be ready to re-request whenever it needs the permission again — never assume permission persists forever.

**Auto-reset.** Also since Android 11, if the user hasn't opened your app in several months, Android automatically revokes all dangerous permissions the app was granted. The next time the user launches, they see a fresh state — no camera, no mic, no location. This is a privacy feature to protect users from apps they've forgotten about. Again, your code should not assume "I had permission last time, so I have it now" — always check.

These aren't things you code around; they're things you code *for*. Structure your app so the "no permission" path is normal, not exceptional.

---

## Part 9 — Best Practices for Handling Non-Normal Permissions

**[Show on screen: a slide with five bullet-point tips]**

Let me leave you with five practical rules that apply to every permission category above normal.

**One — check before every use.** Never cache "the user granted this permission" as a fact. The user could revoke it in Settings, the OS could auto-reset it, or the "only this time" grant could expire. Always call `checkSelfPermission` right before doing the sensitive thing.

**Two — degrade gracefully.** If the user denies, the app shouldn't break. It shouldn't crash. It shouldn't show a scary error. Show a clear explanation of what's missing and let them continue using the parts of your app that don't need that permission. A weather app without location permission should still let you type a city name.

**Three — explain the "why" before you ask.** If the reason your app needs a permission isn't obvious from the button they just tapped, show a rationale dialog first. Users are much more likely to say Allow if they understand what they'll get in return.

**Four — send them to Settings when you must.** If the user picked "don't ask again", you literally cannot show the system dialog anymore. The only way to get the permission is for them to open Settings and toggle it manually. Detect this case, show a message like "You'll need to enable microphone access in Settings", and give them a button that opens Settings directly for your app using `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.

**Five — request minimum scope.** If you only need coarse location, don't ask for fine location. If you only need to read contacts, don't ask for write. Users notice permission scope. So does Google Play — dangerous permissions increase your app's review risk.

---

## Outro

**[Show: the finished Phone Dialer app running through its permission flow one more time]**

That's the full non-normal permission landscape. Dangerous permissions require a runtime dialog and result handling. Signature permissions require matching signing keys and are invisible to users. Special permissions require sending the user to a Settings screen. Each category exists because Android is trying to strike a balance between letting apps be useful and protecting user data.

Every time you add a permission to your app, ask yourself which category it's in, plan the developer flow around that category, and follow the best practices to keep the user experience calm and predictable.

Thanks for watching. If this helped you understand the shape of the permission system, please leave a like and subscribe, and drop any questions in the comments — I'll do my best to answer them. See you in the next series.
