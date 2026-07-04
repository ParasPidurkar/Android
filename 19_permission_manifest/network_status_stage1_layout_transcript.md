# Network Status App — Stage 1: Adding a Button and a Status Text (Video Transcript)

A detailed line-by-line transcript for the first stage of the Network Status app, where we replace the default "Hello World!" TextView with a status text and add a button to trigger the check. Each section shows the exact code to display on screen followed by the narration.

---

## Intro

**[Show: a freshly created Android Studio project with the default layout on screen]**

Hi everyone. Welcome to a brand new mini-project. We're going to build a small Android app that has one job — when the user taps a button, the app checks the current network status and displays the result on the screen. Simple concept, but it's a great excuse to learn about Android's connectivity APIs and how buttons and text views work together in practice.

In this first stage we're just going to build the layout. Two things live on the screen — a TextView that will show the network status, and a Button that triggers the check. We're not writing any Kotlin logic yet. We're just laying out the visual pieces on the screen so we know where everything goes. Let's open `res/layout/activity_main.xml`.

---

## The Starting Point

**[Show on screen — the file as it currently exists:]**

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

Here's what Android Studio gave us when we created the project. The root is a ConstraintLayout that fills the screen, and inside it there's a single TextView that says "Hello World!" pinned to all four edges of the parent, which centers it on the screen.

That's a fine starting point but it's not what we want. We need two things — a status text that we can update dynamically, and a button that triggers the check. Let's transform this.

---

## Updating the Status TextView

**[Show on screen — the modified TextView:]**

```xml
<TextView
    android:id="@+id/textNetworkStatus"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Tap the button to check network status"
    android:textSize="20sp"
    android:gravity="center"
    android:layout_marginBottom="24dp"
    app:layout_constraintBottom_toTopOf="@+id/buttonCheckNetwork"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

Let me walk through what changed compared to the default TextView.

`android:id="@+id/textNetworkStatus"` — this is brand new. The default TextView had no id at all, because it was just a placeholder. But we need to reference this view from Kotlin to update its text later, and the only way to do that is with an id. The `@+id/` syntax creates a new id in the R class named `textNetworkStatus`. I picked a descriptive name because your future self reading this in six months will thank you.

`android:layout_width="wrap_content"` and `android:layout_height="wrap_content"` — these stay the same. The TextView is only as big as its content.

`android:text="Tap the button to check network status"` — I changed the text from "Hello World!" to a real instruction. When the user first opens the app, they'll see this message telling them what to do. Later, when they tap the button, our Kotlin code will replace this text with the actual network status.

`android:textSize="20sp"` — I added a text size of 20 scale-independent pixels. The default is small — around 14sp — which is too subtle for a status message that we want the user to notice. 20sp is big and readable but not shouty.

`android:gravity="center"` — this centers the text inside the TextView's own bounding box. It's useful if the text ever wraps to multiple lines — you want each line centered horizontally.

`android:layout_marginBottom="24dp"` — this adds a 24 density-independent pixel margin below the TextView. Margin is outside the view, so it creates a visual gap between the status text and the button that will sit below it. Without this, they'd touch each other and the layout would look crowded.

Now the four constraint attributes. Three of them are the same as before — start, end, and top all constrained to the parent. But the bottom constraint has changed.

`app:layout_constraintBottom_toTopOf="@+id/buttonCheckNetwork"` — this is different. Instead of pinning the bottom to the parent, we're pinning it to the top of the button. This is how ConstraintLayout lets views position themselves relative to each other, not just to the parent. Combined with the top-to-parent constraint at the other end, this gives us a "stack" — the TextView sits above the button, and the two together are centered vertically on the screen.

Notice the id `@+id/buttonCheckNetwork` — that's the button we're about to add. It's fine to reference an id before it's declared as long as the id is defined somewhere in the same file.

---

## Adding the Button

**[Show on screen — the new button element:]**

```xml
<Button
    android:id="@+id/buttonCheckNetwork"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Check Network Status"
    android:paddingHorizontal="24dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/textNetworkStatus" />
```

Now the new Button element, which sits below the TextView we just built.

`android:id="@+id/buttonCheckNetwork"` — I've given it the id `buttonCheckNetwork`. Again, descriptive names help. In Kotlin we'll use `findViewById(R.id.buttonCheckNetwork)` to look this up.

`android:layout_width="wrap_content"` and `android:layout_height="wrap_content"` — the button is only as big as its label plus padding. If I wanted a full-width button I'd write `match_parent` for the width, but for this simple layout `wrap_content` looks cleaner.

`android:text="Check Network Status"` — the label that appears on the button. This tells the user exactly what tapping the button will do. Good UI writing.

`android:paddingHorizontal="24dp"` — this adds 24dp of padding on the left and right sides of the button label. Padding is inside the view, so it makes the button wider than the text alone would need. Without this, the button might feel cramped, especially if the label is short. `paddingHorizontal` is a shorthand that sets both left and right at once — cleaner than writing `paddingLeft` and `paddingRight` separately.

Now the constraints. Four of them again.

`app:layout_constraintTop_toBottomOf="@+id/textNetworkStatus"` — the top of the button is pinned to the bottom of the TextView. This is the other half of the "stack" — the TextView says "my bottom is above your top", and now the button says "my top is below your bottom". Together they form a vertical chain.

`app:layout_constraintBottom_toBottomOf="parent"` — the bottom of the button is pinned to the bottom of the parent. Combined with the TextView's top-to-parent constraint, this centers the whole stack vertically. ConstraintLayout takes the total height needed by both views plus the margin between them, and centers that group on the screen.

`app:layout_constraintStart_toStartOf="parent"` and `app:layout_constraintEnd_toEndOf="parent"` — these center the button horizontally. Since the button uses `wrap_content` for width, pinning both edges of a wrap-content view to the parent centers it, rather than stretching it.

---

## Closing Tag

**[Show on screen — the closing of the ConstraintLayout:]**

```xml
</androidx.constraintlayout.widget.ConstraintLayout>
```

Finally we close the ConstraintLayout with `</androidx.constraintlayout.widget.ConstraintLayout>`. There are no other elements in this layout — just the TextView, the Button, and the root.

---

## Previewing the Layout

**[Show: Android Studio's Design view showing the two views centered on the screen]**

If you switch to Android Studio's Design view, you'll see the result. Two views stacked vertically in the middle of the screen. The TextView says "Tap the button to check network status" — that's our instruction to the user. Directly below it is the button labeled "Check Network Status". Both are centered horizontally, and the group as a whole is centered vertically.

The button doesn't do anything yet when you tap it, and the TextView will keep showing that instruction forever. That's fine for now — the point of this stage was just to build the visual shell. We'll wire everything up in the next stage.

---

## Complete Layout Recap

**[Show on screen — the full finished file:]**

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
        android:id="@+id/textNetworkStatus"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Tap the button to check network status"
        android:textSize="20sp"
        android:gravity="center"
        android:layout_marginBottom="24dp"
        app:layout_constraintBottom_toTopOf="@+id/buttonCheckNetwork"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <Button
        android:id="@+id/buttonCheckNetwork"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Check Network Status"
        android:paddingHorizontal="24dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/textNetworkStatus" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Here's the complete finished layout. Two elements inside the ConstraintLayout — the status TextView on top, the check button below it, and both wired together with constraints so they stack cleanly in the vertical center of the screen.

---

## Outro

**[Show: running the app on an emulator — user sees the instruction text and the button, but taps do nothing]**

And that wraps up Stage 1 of our Network Status app. We took the default "Hello World!" template and turned it into a proper little layout with a status message and a trigger button.

In the next stage we'll open `MainActivity.kt`, wire up both views with `findViewById`, hook up a click listener on the button, and start writing the actual network-status check. See you in Stage 2.
