# Stage 1 — Creating the Layout (Video Transcript)

A detailed line-by-line transcript for the `activity_main.xml` layout file. Each section shows the exact code to display on screen followed by the narration.

---

## Intro

**[Show: blank Android Studio, then open `res/layout/activity_main.xml`]**

Hi everyone, welcome to the first stage of building our Tic-Tac-Toe app. In this stage we're going to design the entire visual layout of the game — the 3x3 grid of buttons and the reset button — using nothing but XML. We're not writing any Kotlin logic yet, we're just building what the user will see on the screen. I'll walk you through every single line of this layout file and explain exactly what it does and why it's there. Let's open `res/layout/activity_main.xml`.

---

## The XML Declaration

**[Show on screen:]**

```xml
<?xml version="1.0" encoding="utf-8"?>
```

The very first line at the top of the file is the XML declaration. Every proper XML file starts with it. It tells the system two things — the XML version we're using, which is 1.0, and the character encoding, which is UTF-8. UTF-8 means the file can safely contain any character in any language. You don't ever touch this line — Android Studio adds it for you automatically when you create a layout file.

---

## The Root Element and Namespaces

**[Show on screen:]**

```xml
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">
```

Right after the declaration, we open our root element. I'm using ConstraintLayout as the root container because it's the modern, flexible layout system Android recommends. It lets us pin children to the edges of the screen or to each other using constraints. That fully-qualified name — `androidx.constraintlayout.widget.ConstraintLayout` — is the actual class path. We use the AndroidX version because it's the maintained, backward-compatible library.

Inside the opening tag we have three namespace declarations. The first one, `xmlns:android`, is the standard Android namespace. It's required in every layout file. Any attribute that starts with `android:` — like `android:layout_width` or `android:id` — comes from this namespace. Without it, none of the built-in attributes would work.

The second one, `xmlns:app`, is the AndroidX namespace. Attributes that come from libraries like ConstraintLayout live here. That's why we'll later write things like `app:layout_constraintTop_toTopOf`. The `res-auto` part tells the build system to automatically find the right library.

The third one, `xmlns:tools`, is a design-time-only namespace. Attributes with `tools:` only affect what you see in Android Studio's preview — they don't do anything in the actual running app. We use it for hints to the IDE.

Then we have four attributes on the ConstraintLayout itself. `android:id="@+id/main"` gives our root layout the id `main`. The `@+id/` syntax means we're creating a new id. Even though we don't use this id in code, giving your root a name is good practice. `android:layout_width="match_parent"` and `android:layout_height="match_parent"` both mean "fill the entire parent". Since this is the root, it fills the entire screen. And `tools:context=".MainActivity"` tells Android Studio which Activity this layout is used with. Notice the `tools:` prefix — this attribute is only for the IDE, not for the running app.

---

## The Main Board Container

**[Show on screen:]**

```xml
<LinearLayout
    android:id="@+id/board"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent">
```

Inside the ConstraintLayout, we have our first child — a vertical LinearLayout that acts as the game board container. I'm using a LinearLayout here because our content is stacked in a straight vertical line — three rows of buttons, then a reset button. LinearLayout is perfect for that.

`android:id="@+id/board"` gives it the id `board` so we can reference it later if needed. `android:layout_width="match_parent"` makes it fill the full width of the parent ConstraintLayout, which is the full screen width. `android:layout_height="wrap_content"` means the height is only as tall as needed to fit all the children.

`android:orientation="vertical"` is critical for LinearLayout. It tells the layout to stack children top to bottom instead of left to right. Without this attribute, the default is horizontal, and everything would try to sit side by side.

`android:padding="16dp"` adds 16 density-independent pixels of padding on all four sides. Padding is inside the layout, so it creates breathing room between the edge of the board and the buttons inside it. `dp` means density-independent pixels — this makes sure the padding looks the same on phones with different screen densities.

Then we have four constraint attributes. `layout_constraintTop_toTopOf="parent"` pins the top of the board to the top of its parent. `layout_constraintBottom_toBottomOf="parent"` pins the bottom to the bottom. `layout_constraintStart_toStartOf="parent"` pins the start edge — which is the left side in left-to-right languages — to the start of the parent. And `layout_constraintEnd_toEndOf="parent"` pins the end edge to the end of the parent.

When you pin all four sides like this, ConstraintLayout centers the child on the screen. Since our LinearLayout height is `wrap_content`, it gets vertically centered, which is exactly what we want for a game board. Notice these all use the `app:` prefix because ConstraintLayout attributes come from the AndroidX library, not from the core Android framework.

---

## Row 1 — The First Row of Buttons

**[Show on screen:]**

```xml
<!-- Row 1 -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal">
```

Now inside the board LinearLayout, we start adding rows. Above the first row I've written an XML comment. Comments in XML start with `<!--` and end with `-->`. They don't affect the app at all, they're just there to help anyone reading the code. I've added a comment before each row so the structure is easy to follow.

The first row itself is another LinearLayout, but horizontal. The width is `match_parent` so this row takes the full width of its parent, which is the board. The height is `wrap_content` so it's only as tall as its tallest child. And `orientation="horizontal"` means the three buttons inside will sit side by side.

**[Show on screen:]**

```xml
<Button
    android:id="@+id/button1"
    android:layout_width="0dp"
    android:layout_height="100dp"
    android:layout_weight="1"
    android:textSize="32sp" />
<Button
    android:id="@+id/button2"
    android:layout_width="0dp"
    android:layout_height="100dp"
    android:layout_weight="1"
    android:textSize="32sp" />
<Button
    android:id="@+id/button3"
    android:layout_width="0dp"
    android:layout_height="100dp"
    android:layout_weight="1"
    android:textSize="32sp" />
</LinearLayout>
```

Inside this row we have three Button elements. Let me walk through the first one line by line, because all nine cell buttons follow the exact same pattern.

`android:id="@+id/button1"` creates a Button and gives it the id `button1`. We'll use this id in Kotlin later with `findViewById`.

`android:layout_width="0dp"` is the interesting one. We set the width to zero. That looks strange, but it's intentional. When you combine `layout_width="0dp"` with `layout_weight="1"` inside a horizontal LinearLayout, the system ignores the fixed width and distributes the available horizontal space based on weights instead. So this button doesn't want any specific width — its width will be determined by the weight system.

`android:layout_height="100dp"` sets the height to a fixed 100 density-independent pixels. That gives us big, tappable, roughly square cells that are easy to hit with a finger.

`android:layout_weight="1"` is the second half of the trick. When all three buttons in a row have weight 1, the LinearLayout says "add up all the weights — that's 3 — and give each button one-third of the available width". So no matter the screen size, the three buttons always share the width equally.

`android:textSize="32sp"` sets the text size to 32 scale-independent pixels. `sp` is like `dp` but it also respects the user's system-wide font size setting for accessibility. 32sp is nice and large so the X and O marks will be clearly visible.

The tag closes with `/>` because a Button has no children — it's self-closing. Buttons 2 and 3 in this row are identical except for their ids. Same width, height, weight, and text size. That's what gives us three equal-width cells in the row. The row LinearLayout then closes with `</LinearLayout>`.

---

## Rows 2 and 3

**[Show on screen:]**

```xml
<!-- Row 2 -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal">

    <Button
        android:id="@+id/button4"
        android:layout_width="0dp"
        android:layout_height="100dp"
        android:layout_weight="1"
        android:textSize="32sp" />
    <Button
        android:id="@+id/button5"
        android:layout_width="0dp"
        android:layout_height="100dp"
        android:layout_weight="1"
        android:textSize="32sp" />
    <Button
        android:id="@+id/button6"
        android:layout_width="0dp"
        android:layout_height="100dp"
        android:layout_weight="1"
        android:textSize="32sp" />
</LinearLayout>

<!-- Row 3 -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal">

    <Button
        android:id="@+id/button7"
        android:layout_width="0dp"
        android:layout_height="100dp"
        android:layout_weight="1"
        android:textSize="32sp" />
    <Button
        android:id="@+id/button8"
        android:layout_width="0dp"
        android:layout_height="100dp"
        android:layout_weight="1"
        android:textSize="32sp" />
    <Button
        android:id="@+id/button9"
        android:layout_width="0dp"
        android:layout_height="100dp"
        android:layout_weight="1"
        android:textSize="32sp" />
</LinearLayout>
```

The second and third rows are structurally identical to the first row. Each is a horizontal LinearLayout with `match_parent` width, `wrap_content` height, and three buttons inside — buttons 4, 5, 6 in the middle row, and buttons 7, 8, 9 in the bottom row. Every button uses the same `layout_width="0dp"`, `layout_height="100dp"`, `layout_weight="1"`, `textSize="32sp"` combination so the entire 3x3 grid is perfectly uniform.

I've labeled each row with an XML comment — Row 2 and Row 3 — so if you're reading the file top to bottom, you always know which row you're in.

---

## The Reset Button

**[Show on screen:]**

```xml
<!-- Reset button -->
<Button
    android:id="@+id/buttonReset"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="24dp"
    android:text="Reset" />
```

After the three rows, we have the reset button.

`android:id="@+id/buttonReset"` gives it the id `buttonReset`. We'll wire this up in Kotlin to reset the game.

`android:layout_width="match_parent"` makes the reset button span the full width of the board. That's intentional — it's a very important action, so we want it to be big and easy to hit.

`android:layout_height="wrap_content"` means the button is only as tall as it needs to be to fit its label. It's not a game cell, so we don't need it to be 100dp tall.

`android:layout_marginTop="24dp"` adds 24 density-independent pixels of space above the reset button. Margin is outside the view, so this creates a nice visual gap between the last row of the grid and the reset button. Without it, the reset button would sit directly touching the bottom of the grid, and the UI would look crowded.

`android:text="Reset"` sets the label that appears on the button.

---

## Closing Tags

**[Show on screen:]**

```xml
    </LinearLayout>
</androidx.constraintlayout.widget.ConstraintLayout>
```

Finally, we close the containers in reverse order. `</LinearLayout>` closes the board LinearLayout. And `</androidx.constraintlayout.widget.ConstraintLayout>` closes the root ConstraintLayout.

XML is strict about matching opening and closing tags — every element you opened must be closed in reverse order, so the last thing opened is the first thing closed.

---

## Outro

**[Show: full layout preview in Android Studio's Design view]**

And that's the complete visual layout of our Tic-Tac-Toe app. Just to recap what we built: at the root we have a ConstraintLayout that fills the screen. Inside it, a vertical LinearLayout centered on the screen. That LinearLayout contains three horizontal rows of three buttons each, giving us the 3x3 grid, plus a Reset button at the bottom. The buttons use `layout_weight` to share the row width equally, which is the standard Android trick for evenly distributed cells.

If you run the app right now, you'll see all the buttons on screen, but tapping them won't do anything yet. That's what we'll fix in the next stage, where we'll map every one of these XML views into our Kotlin code and start responding to clicks. See you in Stage 2.

---

## Bonus — How to Record This Video

You asked whether to show the finished code first and then explain, or to write the code live while explaining. Both work, but they suit different audiences.

**Write code live while explaining** works best for beginners. The viewer sees the empty file and each element appear as you talk about it. It matches the rhythm of learning — one concept at a time — and it feels less overwhelming. The downside is it's slower to record and edit, and one typo can derail the take.

**Show the finished code first, then walk through it** works best for intermediate viewers who just want to understand a pattern. You paste the whole file, then step through it section by section. It's faster to record and easier to edit, but a beginner can feel lost seeing 80 lines all at once.

A hybrid that works really well for tutorials: paste the finished layout once at the very start so viewers see the destination, then delete it and rebuild it live from an empty file while narrating each section using this transcript. That gives the audience the "big picture" up front and the step-by-step learning journey afterward. For a Tic-Tac-Toe series like yours, I'd go with the hybrid — it respects your viewers' time while still teaching thoroughly.
