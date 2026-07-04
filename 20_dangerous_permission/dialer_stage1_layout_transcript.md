# Phone Dialer App — Stage 1: Adding the Input Field and Dial Button (Video Transcript)

A detailed line-by-line transcript for the first stage of the Phone Dialer app, where we replace the default "Hello World!" TextView with a label, a phone-number EditText, and a Dial button. Each section shows the exact code to display on screen followed by the narration.

---

## Intro

**[Show: a freshly created Android Studio project with the default "Hello World!" layout on screen]**

Hi everyone. Welcome to a brand new mini-project. This time we're building a small phone-dialer app. The user types a phone number into an input field, taps a Dial button, and the app hands off to the actual phone app to place the call. Simple concept, but it's the perfect excuse to talk about one of the most important topics in Android — **dangerous permissions** and how to request them at runtime. That's what the whole series will be about, but we're building the visible pieces first.

In this stage we're just going to build the layout. Three things live on the screen — a label telling the user what to do, an EditText where they enter the number, and a Button labeled "Dial" that triggers the call. We're not writing any Kotlin logic yet. Let's open `res/layout/activity_main.xml`.

---

## The Starting Point

**[Show on screen — the file as Android Studio left it:]**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Here's what Android Studio hands us — a ConstraintLayout that fills the screen and a single centered TextView saying "Hello World!". That's a fine starting shell, but we need something totally different — a label, an input field, and a button, stacked vertically and centered. Let's transform this.

---

## Adding the Label TextView

**[Show on screen — the new label TextView:]**

```xml
<TextView
    android:id="@+id/textLabel"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Enter a phone number to dial"
    android:textSize="18sp"
    android:layout_marginBottom="16dp"
    app:layout_constraintBottom_toTopOf="@+id/editPhoneNumber"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintVertical_chainStyle="packed" />
```

The first view is a TextView acting as a label above the input field. Let me walk through it.

`android:id="@+id/textLabel"` gives it an id. Even though we're probably not going to change this text from Kotlin, giving every view a descriptive id is good habit. The `@+id/` syntax creates a new entry in the R class.

`android:layout_width="wrap_content"` and `android:layout_height="wrap_content"` — the label is just as big as its text.

`android:text="Enter a phone number to dial"` is the actual instruction the user reads. Clear UI writing is important — the user should know what to do the moment they open the app.

`android:textSize="18sp"` — 18 scale-independent pixels. Slightly larger than the default, so it reads as a heading without being loud.

`android:layout_marginBottom="16dp"` adds 16 density-independent pixels of space below the label. Margin is outside the view, so this creates a visual gap between the label and the input field below it.

Then we have the constraints. `constraintTop_toTopOf="parent"` pins the label's top edge to the top of the parent. `constraintBottom_toTopOf="@+id/editPhoneNumber"` pins the label's bottom edge to the top of the EditText we're about to add. And `constraintStart_toStartOf="parent"` and `constraintEnd_toEndOf="parent"` center it horizontally.

The last attribute, `app:layout_constraintVertical_chainStyle="packed"`, is the interesting one. When you connect several views in a vertical chain — top-to-bottom-to-top-to-bottom — ConstraintLayout treats them as a group and asks "how do you want the extra vertical space distributed?" The default is `spread`, which pushes them apart. `packed` keeps them close together and centers the whole group as a block. That's exactly what we want here — the label, the input, and the button should look like one visually connected form, centered on the screen.

You only set `chainStyle` on the first view in the chain — that's why it's here on the label and not on the others.

---

## Adding the EditText for the Phone Number

**[Show on screen — the EditText:]**

```xml
<EditText
    android:id="@+id/editPhoneNumber"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:hint="e.g. +1 555 123 4567"
    android:inputType="phone"
    android:textSize="20sp"
    android:layout_marginHorizontal="32dp"
    android:layout_marginBottom="16dp"
    app:layout_constraintBottom_toTopOf="@+id/buttonDial"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/textLabel" />
```

Now the input field. This is our first EditText in these transcripts, so let me spend some time on it.

`android:id="@+id/editPhoneNumber"` — descriptive id we'll use to read the entered text from Kotlin.

`android:layout_width="0dp"` — this looks strange, but with ConstraintLayout, `0dp` means "match the constraints". Combined with the start-to-parent and end-to-parent constraints below, this makes the EditText stretch horizontally between them minus any margins. Without `0dp`, `wrap_content` would make the field only as wide as the placeholder text, which looks awkward for a phone-number input.

`android:layout_height="wrap_content"` — the field's height is only as tall as needed for one line of text plus its padding.

`android:hint="e.g. +1 555 123 4567"` — this is the placeholder text shown in grey when the field is empty. It gives the user an example of what kind of input we're expecting. Unlike `android:text`, the hint disappears as soon as the user starts typing.

`android:inputType="phone"` is the key attribute here. This tells the Android keyboard "the user is entering a phone number, please show the phone-optimized keypad." So instead of the usual QWERTY layout, the user sees a numeric dial-pad with `+`, `*`, `#`, and digits. It's a small touch that makes the app feel much more polished and prevents accidentally-typed letters.

`android:textSize="20sp"` — a bit larger than the label because the number should be very readable.

`android:layout_marginHorizontal="32dp"` — 32dp of space on the left and right sides. `layout_marginHorizontal` is a shorthand for setting both `marginStart` and `marginEnd` at once. This keeps the field from stretching all the way to the screen edges, which would look cramped.

`android:layout_marginBottom="16dp"` — another gap below, so the input doesn't touch the Dial button.

The constraints continue the vertical chain. `constraintTop_toBottomOf="@+id/textLabel"` pins the top of the EditText to the bottom of the label — that's the top link of the chain. `constraintBottom_toTopOf="@+id/buttonDial"` pins the bottom of the EditText to the top of the Dial button — the bottom link. And the start and end constraints anchor the horizontal `0dp` stretch.

---

## Adding the Dial Button

**[Show on screen — the Dial button:]**

```xml
<Button
    android:id="@+id/buttonDial"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Dial"
    android:paddingHorizontal="32dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/editPhoneNumber" />
```

Finally the Dial button.

`android:id="@+id/buttonDial"` — descriptive id we'll use from Kotlin to attach a click listener.

`android:layout_width="wrap_content"` and `android:layout_height="wrap_content"` — the button is only as big as its label plus padding.

`android:text="Dial"` — one word, exactly describing what tapping does.

`android:paddingHorizontal="32dp"` adds padding inside the button on the left and right. Padding is inside the view, so it makes the button wider than the "Dial" text alone would need. Without this, a one-word button would be tiny and hard to hit accurately with a finger. 32dp on each side gives it a proper tappable presence.

Now the constraints — this is the last link of the chain.

`constraintTop_toBottomOf="@+id/editPhoneNumber"` pins the top of the button to the bottom of the EditText. `constraintBottom_toBottomOf="parent"` pins the bottom of the button to the bottom of the parent. Together with the label's `constraintTop_toTopOf="parent"` up top, this defines the full chain — parent → label → EditText → button → parent — and because we set `chainStyle="packed"` on the label, ConstraintLayout treats the whole thing as one packed group and centers it vertically.

`constraintStart_toStartOf="parent"` and `constraintEnd_toEndOf="parent"` center the button horizontally. Since its width is `wrap_content`, pinning both edges to the parent centers it rather than stretching it.

---

## Closing Tag

**[Show on screen — the closing tag:]**

```xml
</androidx.constraintlayout.widget.ConstraintLayout>
```

Close the ConstraintLayout. Three children total — label, EditText, button — all inside the root.

---

## Previewing the Layout

**[Show: Android Studio's Design view with the three views packed and centered on the screen]**

Switch to Android Studio's Design view. You should see three elements stacked in the middle of the screen. The label reads "Enter a phone number to dial". Below it, an empty input field with the placeholder text "e.g. +1 555 123 4567" showing in grey. Below that, a Dial button. Because we used a packed chain with a chain style set to `packed`, the three views sit close together and the whole group is vertically centered.

Tap into the input field on the emulator and notice — the phone-numeric keypad appears instead of the regular keyboard. That's `inputType="phone"` doing its job.

Nothing happens when you tap Dial yet — that's next stage.

---

## Complete Layout Recap

**[Show on screen — the finished file:]**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/textLabel"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Enter a phone number to dial"
        android:textSize="18sp"
        android:layout_marginBottom="16dp"
        app:layout_constraintBottom_toTopOf="@+id/editPhoneNumber"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_chainStyle="packed" />

    <EditText
        android:id="@+id/editPhoneNumber"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:hint="e.g. +1 555 123 4567"
        android:inputType="phone"
        android:textSize="20sp"
        android:layout_marginHorizontal="32dp"
        android:layout_marginBottom="16dp"
        app:layout_constraintBottom_toTopOf="@+id/buttonDial"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/textLabel" />

    <Button
        android:id="@+id/buttonDial"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Dial"
        android:paddingHorizontal="32dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/editPhoneNumber" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

That's the whole layout — three views, all connected in a packed vertical chain, centered on the screen.

---

## Outro

**[Show: running the app on an emulator — user taps the input, phone keypad appears, they type digits, the Dial button sits there waiting]**

And that wraps up Stage 1 of our Phone Dialer app. We turned the "Hello World!" template into a real little form with a label, a phone-optimized input field, and a Dial button. It's not functional yet — tapping Dial does nothing — but the visual shell is ready.

In the next stage we'll open `MainActivity.kt`, wire up the views, and try to actually place the call. That's when things get interesting, because calling `Intent.ACTION_CALL` requires the `CALL_PHONE` permission — and unlike the network-state permission we saw in the last project, `CALL_PHONE` is a **dangerous permission**. That means declaring it in the manifest isn't enough. We have to ask the user at runtime with a dialog, handle "Allow", handle "Deny", and gracefully cope with "Deny and don't ask again". This is where dangerous permissions get real. See you in Stage 2.
