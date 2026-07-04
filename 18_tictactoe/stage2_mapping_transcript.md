# Stage 2 — Mapping the Code with the Design (Video Transcript)

A detailed line-by-line transcript for the initial `MainActivity.kt` file. Each section shows the exact code to display on screen followed by the narration.

---

## Intro

**[Show: `MainActivity.kt` opened in Android Studio, mostly empty]**

Welcome to Stage 2. In the last stage we built the entire visual layout of our Tic-Tac-Toe app in XML. We have a beautiful 3x3 grid of buttons and a Reset button, but if you tap any of them right now, nothing happens. That's because XML only describes what you see — it doesn't do anything. The behavior lives in Kotlin.

So in this stage we're going to open `MainActivity.kt` and do what I like to call "mapping the code with the design". That means we take every view we defined in the XML and pull it into Kotlin as a variable we can control. We'll also lay the foundation for handling clicks. We're not writing game logic yet — no X's, no O's, no win detection. We're just building the plumbing. Let's go.

---

## The Package Declaration

**[Show on screen:]**

```kotlin
package com.example.tictactoe
```

The very first line at the top of the file is the package declaration. Every Kotlin file starts with one. It has to match the folder structure the file lives in, and it uniquely identifies the class inside your app. When you referenced `.MainActivity` in the XML with `tools:context=".MainActivity"`, this is the package Android looked up. If you change this line without moving the file, the app will stop compiling.

---

## The Imports

**[Show on screen:]**

```kotlin
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
```

Below the package we have five import statements. Imports tell the compiler where to find the classes we're going to use in this file. Without them, we'd have to write out the full package path every time.

`android.os.Bundle` — Bundle is the container Android uses to pass data between activity lifecycle callbacks. Our `onCreate` method takes a Bundle parameter, so we need this import.

`android.view.View` — the View class is the base class of every UI element in Android. Buttons, TextViews, LinearLayouts, everything is ultimately a View. Our `onClick` method receives a View parameter, and we're implementing the `View.OnClickListener` interface, so we need this.

`android.widget.Button` — this is the Button widget class. Since we're going to create Button variables, we need this import.

`androidx.activity.enableEdgeToEdge` — this is a modern extension function that makes our app draw behind the system status bar and navigation bar for a nicer full-screen look. It comes from the AndroidX activity library.

`androidx.appcompat.app.AppCompatActivity` — this is the base class every activity extends. It provides backward compatibility with older Android versions and gives you access to the modern ActionBar and theming APIs. Our class is going to extend this.

---

## The Class Declaration

**[Show on screen:]**

```kotlin
class MainActivity : AppCompatActivity(), View.OnClickListener {
```

Now the class itself. Let's break this down carefully because there's a lot happening in a single line.

`class MainActivity` declares a new class named `MainActivity`. This name must match the file name.

The colon `:` in Kotlin means two different things depending on what follows. When followed by a class, it means "extends". When followed by an interface, it means "implements". Kotlin uses the same colon for both because a class can only extend one parent but can implement many interfaces, and the compiler can figure out which is which.

`AppCompatActivity()` is the parent class. Notice the parentheses — in Kotlin you actually call the constructor of the parent right in the class declaration. This gives us all the standard Activity lifecycle behavior.

Then a comma, then `View.OnClickListener`. This is the interface we're implementing. `View.OnClickListener` is a very simple interface — it has one method, `onClick`. By implementing this interface, our Activity itself becomes a valid click listener. This is a really important choice. It means later, when we want to handle nine button clicks, we don't need nine separate lambdas — we just pass `this` to each button's `setOnClickListener`, and every click will funnel into a single `onClick` method inside our class. It's cleaner and easier to maintain.

The opening curly brace `{` starts the class body.

---

## The `lateinit` Properties

**[Show on screen:]**

```kotlin
lateinit var board: Array<Array<Button>>
lateinit var buttonReset: Button
```

At the top of the class body we have two property declarations. Let me unpack the first one piece by piece.

`lateinit` is a special Kotlin keyword that means "I promise to initialize this property before I use it, so don't force me to give it a value right now." Normally, Kotlin is strict — if you declare a non-null property, you must give it a value immediately. But we can't give `board` a value at this point because the buttons don't exist yet. The buttons only come into existence after `setContentView` runs inside `onCreate`. So `lateinit` lets us defer the initialization.

`var` means it's a mutable variable — we can reassign it later.

`board:` gives it the name `board`, and `Array<Array<Button>>` is its type — a 2D array of Buttons. Read the type as "an array whose elements are arrays of buttons". So `board[0]` gives you the top row (which is itself an array), and `board[0][0]` gives you the top-left button. This 2D structure mirrors the visual grid perfectly, which will make our game logic much cleaner.

The second line, `lateinit var buttonReset: Button`, is the same idea but for the Reset button. We hold it as a class property so any method inside the class can reach it.

---

## The `onCreate` Method

**[Show on screen:]**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_main)
```

Next we override `onCreate`, which is the entry point for every Activity.

The `override` keyword tells the compiler "I'm intentionally replacing a method from my parent class." If you leave it out, the code won't compile — Kotlin is strict about this because accidental overrides are a common source of bugs.

`fun` is Kotlin's keyword for a function. `onCreate` is the function name — this specific name is what the Android framework calls when your Activity is being set up.

`savedInstanceState: Bundle?` is the parameter. The `?` at the end means the Bundle can be null. When your Activity is created for the very first time, the framework passes null. When it's being recreated — for example after a screen rotation — the framework passes a Bundle containing the previous saved state.

Inside the method, `super.onCreate(savedInstanceState)` must be the first line. It calls the parent AppCompatActivity's `onCreate` so the framework can do all its own initialization. If you forget this call, Android will throw an exception at runtime.

`enableEdgeToEdge()` activates edge-to-edge mode, where the app draws behind the system bars. It's a purely visual improvement that makes the app feel more modern.

`setContentView(R.layout.activity_main)` is the line that connects our XML file to this Activity. `R.layout.activity_main` is a reference to the layout file we built in Stage 1. Android generates an `R` class automatically at build time that contains ids for every resource — layouts, drawables, strings, views, everything. When `setContentView` runs, it takes the XML file, inflates it into actual view objects in memory, and attaches them to the Activity's window. From this line onward, all the views described in the XML actually exist and can be looked up.

---

## Wiring Up the Buttons with `findViewById`

**[Show on screen:]**

```kotlin
val button1 = findViewById<Button>(R.id.button1)
val button2 = findViewById<Button>(R.id.button2)
val button3 = findViewById<Button>(R.id.button3)
val button4 = findViewById<Button>(R.id.button4)
val button5 = findViewById<Button>(R.id.button5)
val button6 = findViewById<Button>(R.id.button6)
val button7 = findViewById<Button>(R.id.button7)
val button8 = findViewById<Button>(R.id.button8)
val button9 = findViewById<Button>(R.id.button9)
buttonReset = findViewById(R.id.buttonReset)
```

Now we have nine lines that all look similar. Let me explain the first one carefully because the other eight follow the exact same pattern.

`val` means "immutable variable" — once we assign it, we won't change it. Since we're just grabbing a reference to a button that already exists, we don't need to reassign it later, so `val` is appropriate.

`button1` is the variable name. It's a local variable inside `onCreate` because we only need it long enough to put it into our `board` array.

`findViewById<Button>` is the framework method that looks up a view by its id. The `<Button>` in angle brackets is a generic type argument — it tells `findViewById` "I expect this to return a Button". Without it, the return type would be a generic View that we'd have to cast.

`R.id.button1` is the id we're looking up. Remember in the XML we wrote `android:id="@+id/button1"` — the `@+id/` syntax told the build system to generate a constant named `R.id.button1` in the generated R class. So this line is saying "find the view whose id is `button1` in the current layout and return it as a Button".

Lines like this repeat for buttons 2 through 9 — nine cell buttons in total, one for each square of our tic-tac-toe grid.

The last line, `buttonReset = findViewById(R.id.buttonReset)`, is a little different. First, no `val` — that's because `buttonReset` was already declared as a class property with `lateinit var` at the top. We're assigning to the existing property, not creating a new local variable. Second, no `<Button>` generic parameter — Kotlin can infer the type from the property's declared type, so it's optional here. Either style works.

At this point, our Kotlin variables now point to the actual button objects on screen. The mapping between XML and code is complete.

---

## Building the 2D Board Array

**[Show on screen:]**

```kotlin
board = arrayOf(
    arrayOf(button1, button2, button3),
    arrayOf(button4, button5, button6),
    arrayOf(button7, button8, button9)
)
```

Now that we have all nine button references, we assemble them into a grid.

`arrayOf` is a Kotlin standard-library function that creates an array from its arguments. So the outer `arrayOf` creates an array of three elements — each of those elements is itself an array of three buttons.

The result is a 2D structure where the first index is the row and the second index is the column. `board[0][0]` is the top-left button. `board[1][1]` is the center. `board[2][2]` is the bottom-right. This mirrors the visual grid exactly.

Why do this? Because when we write game logic, we want to loop over rows and columns like a grid, not reference nine separate variables by name. It'll make the win-detection code and the reset logic dramatically cleaner.

---

## Attaching Click Listeners to Every Cell

**[Show on screen:]**

```kotlin
for (row in board) {
    for (button in row) {
        button.setOnClickListener(this)
    }
}
```

Now we hook up click behavior for the nine cells using two nested loops.

The outer `for (row in board)` walks through the three rows of the board. On each iteration, `row` is one of the three inner arrays.

The inner `for (button in row)` walks through the three buttons in that row. On each iteration, `button` is one specific Button.

Inside the loop we call `button.setOnClickListener(this)`. `setOnClickListener` is the standard Android method to register a click handler. Normally you pass it an anonymous listener or a lambda, but here we're passing `this`.

`this` refers to the current instance of MainActivity. And because MainActivity implements `View.OnClickListener`, it's a valid argument. So we're telling every button "when you get tapped, call the onClick method on my Activity."

In just four lines, we've registered the Activity as the click listener for all nine cells. Compare that to writing nine separate calls to `setOnClickListener` with nine separate lambdas — this is much cleaner.

---

## The Reset Button Listener

**[Show on screen:]**

```kotlin
buttonReset.setOnClickListener {
    // reset logic here
}
```

The Reset button gets its own handler. Here I'm using a lambda instead of `this`. Why? Because reset is a totally different action from placing a mark — it doesn't share logic with the cells. Using a separate lambda for reset keeps the two responsibilities cleanly divided.

For now the lambda is empty — I've just written a comment saying "reset logic here" as a placeholder. We'll fill this in during a later stage when we add game state.

The closing curly brace after this ends the `onCreate` method.

---

## The `onClick` Method

**[Show on screen:]**

```kotlin
override fun onClick(view: View?) {
    when (view?.id) {
        R.id.button1 -> { }
        R.id.button2 -> { }
        R.id.button3 -> { }
        R.id.button4 -> { }
        R.id.button5 -> { }
        R.id.button6 -> { }
        R.id.button7 -> { }
        R.id.button8 -> { }
        R.id.button9 -> { }
    }
}
```

The last thing in the file is the `onClick` method. This is the method required by the `View.OnClickListener` interface. Because we implemented that interface at the top of the class, we're contractually required to provide this method. The `override` keyword confirms we're satisfying the interface's contract.

`view: View?` is the parameter. The framework passes the specific View that was just clicked. It's declared nullable — hence the `?` — as a defensive measure, though in practice the framework will always give us a real View.

Inside the method, `when (view?.id)` is Kotlin's much more powerful version of a switch statement. It compares one value against multiple possible matches. We're checking `view?.id`. The `?.` is Kotlin's safe-call operator. It means "if view is not null, get its id; if view is null, evaluate to null". This prevents a `NullPointerException` in the rare case that view is null.

Then we have nine branches, one for each button id. The syntax is `matchValue -> { code to run }`. So `R.id.button1 -> { }` means "if the tapped view's id is `button1`, run this code block". Right now the code blocks are empty — that's on purpose. This is the skeleton. In later stages we'll fill each block with the logic to place an X or an O at the correct grid position.

The closing braces close the `when` block, the `onClick` method, and finally the entire class.

---

## Outro

**[Show: running the app on an emulator — buttons visible but no response to taps]**

And that wraps up Stage 2. Let's zoom out and look at what we accomplished. We started with an XML layout that had views but no behavior. Now we have a Kotlin file that opens the layout, finds every button in it, organizes them into a 2D array, and registers the Activity itself as the click listener for all nine cells. We also have an empty `onClick` method that already knows how to distinguish which button was tapped — it just doesn't do anything yet.

If you run the app at this point, tapping the buttons still won't visibly change anything, but under the hood every tap is now flowing through your `onClick` method. You've built the plumbing.

In the next stage we'll actually make the buttons respond — we'll place an X or an O, switch turns between players, and prevent players from tapping the same cell twice. See you in Stage 3.
