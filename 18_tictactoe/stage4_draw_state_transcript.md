# Stage 4 — Implementing the Game Draw State (Video Transcript)

A detailed line-by-line transcript for the stage where we add turn switching, the turn indicator TextView, and draw detection. Each section shows the exact code to display on screen followed by the narration.

---

## Intro

**[Show: the running app from Stage 3 — tap 5 cells, all show "X" because turns don't switch]**

Welcome to Stage 4. Take a look at what happens when we run the Stage 3 app. I tap the first cell, I see an X. I tap a second cell — another X. A third — still X. That's because our code applies a move but never actually flips the player. There's also no message on the screen telling the user whose turn it is or when the board is full.

So in this stage we're going to fix all of that. We'll add a TextView at the top of the screen that always shows the current game state. We'll flip the player after every move. And we'll detect a draw when all nine cells have been played without a winner. This is what I'm calling the "game draw state" — the framework for narrating the game to the player.

Let's walk through every new piece.

---

## The New Import

**[Show on screen — highlight the new import line:]**

```kotlin
import android.widget.TextView
```

We start by importing the `TextView` class. In Kotlin, if you use a class you have to import it. We're about to declare a `TextView` property for our turn indicator, so we bring the import in. It comes from the `android.widget` package, the same package that `Button` comes from.

---

## The New `textTurn` Property

**[Show on screen — highlight the new property added to the class:]**

```kotlin
// TextView at the top of the screen that shows whose turn it is
// (or the game outcome). Held as a field so updateDisplay() can reach it.
lateinit var textTurn: TextView
```

Below the `buttonReset` property, we've added a new one — `textTurn`. This is the TextView at the top of the screen that we defined in the XML with the id `textTurn`. It's going to show the current state of the game — "Player X Turn", "Player O Turn", or "Game Draw" — depending on what's happening.

Same as our other view properties, it's `lateinit var` because we can only hook it up after `setContentView` runs. And I'm making it a class-level property, not a local variable inside `onCreate`, so that any other method in the class can reach it. Specifically, our new `updateDisplay` helper is going to write to it.

---

## Wiring Up the TextView

**[Show on screen — highlight the new `findViewById` line inside `onCreate`:]**

```kotlin
buttonReset = findViewById(R.id.buttonReset)
textTurn = findViewById(R.id.textTurn)   // turn-indicator TextView
```

Right after we hook up `buttonReset`, we add one more `findViewById` line for the TextView. `R.id.textTurn` matches the id we set in the XML. Kotlin infers the type from the property declaration, so we don't need to write `<TextView>` in angle brackets.

At this point our new TextView is fully connected to Kotlin and we can read from or write to it.

---

## Turn Switching Inside `onClick`

**[Show on screen — the `onClick` method, highlight the new lines after the `when` block:]**

```kotlin
override fun onClick(view: View?) {
    when (view?.id) {
        R.id.button1 -> { upadateValue(row = 0, col = 0, player = PLAYER) }
        R.id.button2 -> { upadateValue(row = 0, col = 1, player = PLAYER) }
        R.id.button3 -> { upadateValue(row = 0, col = 2, player = PLAYER) }
        R.id.button4 -> { upadateValue(row = 1, col = 0, player = PLAYER) }
        R.id.button5 -> { upadateValue(row = 1, col = 1, player = PLAYER) }
        R.id.button6 -> { upadateValue(row = 1, col = 2, player = PLAYER) }
        R.id.button7 -> { upadateValue(row = 2, col = 0, player = PLAYER) }
        R.id.button8 -> { upadateValue(row = 2, col = 1, player = PLAYER) }
        R.id.button9 -> { upadateValue(row = 2, col = 2, player = PLAYER) }
    }

    TURN_COUNT++          // one more move has been made
    PLAYER = !PLAYER      // switch turn: X -> O or O -> X
```

The top of `onClick` is exactly the same as in Stage 3 — the `when` block that translates each button id into a row and column and calls `upadateValue`. That part hasn't changed.

But right after the `when` block, we've added two new lines that are the core of this stage.

`TURN_COUNT++` uses the increment operator to add one to `TURN_COUNT`. Every time a move is placed, this counter goes up by one. So after the first tap, `TURN_COUNT` is `1`. After the second, it's `2`. And so on. It's cumulative and only grows during a game — it only resets when the Reset button is tapped.

`PLAYER = !PLAYER` uses the boolean-not operator `!` to flip the current player. If `PLAYER` was `true` (X's turn), `!PLAYER` becomes `false` (O's turn). And vice versa. This is the fix for the bug we saw at the beginning of the video where every tap placed an X. Now the very next tap will use the other player's symbol.

The order here matters. We call `upadateValue` first, which uses the current value of `PLAYER` to place the mark. Only after the move is placed do we flip `PLAYER`. If we flipped `PLAYER` before the `when` block, we'd be placing the wrong symbol.

---

## Updating the Turn Indicator

**[Show on screen — the if/else block for updating the top TextView:]**

```kotlin
// Reflect the new turn in the top TextView.
if (PLAYER) {
    updateDisplay("Player X Turn")
} else {
    updateDisplay("Player O Turn")
}
```

Now that the turn has flipped, we need to tell the user whose turn is up next. That's exactly what these lines do.

`if (PLAYER)` checks the boolean. If it's `true`, it means it's now X's turn — so we call `updateDisplay("Player X Turn")`. Otherwise, it's O's turn and we call `updateDisplay("Player O Turn")`.

Notice we're using a separate helper method `updateDisplay` instead of writing `textTurn.text = "Player X Turn"` directly. There are two reasons for that. First, it centralizes all screen-text updates in one place, which is cleaner. Second — and this becomes important in later stages — the helper can do extra work like disabling the board when someone wins, without us having to remember to do that at every call site.

---

## Draw Detection

**[Show on screen — the draw check at the end of `onClick`:]**

```kotlin
// After 9 moves with no winner, the game is a draw.
if (TURN_COUNT == 9) {
    updateDisplay("Game Draw")
}
```

The last piece inside `onClick` is the draw check. A Tic-Tac-Toe board has exactly nine cells. If we've played nine moves without a winner, the game is over and it's a draw.

`if (TURN_COUNT == 9)` uses the equality operator `==` to compare the counter against 9. On the ninth move — and only the ninth — this condition is true, and we call `updateDisplay("Game Draw")`. That overwrites whatever "Player X Turn" or "Player O Turn" message we just set, and shows "Game Draw" on the top TextView instead.

This is a slightly simplified draw detection. Technically it doesn't verify that no one won on the ninth move, so if somebody actually did win on the final move, the "Game Draw" message would incorrectly overwrite the "Player X wins" message. We'll fix that ordering in the next stage when we add winner detection. For now, this is enough to show the mechanic.

---

## The `updateDisplay` Helper

**[Show on screen — the new private helper method:]**

```kotlin
// Small helper so all screen-text updates go through one place.
private fun updateDisplay(text: String) {
    textTurn.text = text
}
```

Finally, we've added a new private method called `updateDisplay`.

`private` because it's only used inside this class. `fun updateDisplay(text: String)` takes one parameter — the message we want to show.

The body is one line — `textTurn.text = text`. This sets the text on our TextView, which changes what the user sees at the top of the screen.

Right now it's a very simple method with just one job. But we designed it as a helper on purpose. In the next stage, when we implement winner detection, we're going to add extra behavior here — like disabling the board when someone wins. Because every screen-text update flows through this one method, we only have to add that logic in one place, not every time we want to show a message.

---

## The Untouched Pieces

**[Show on screen — briefly scroll through `initializeBoardStatus` and `upadateValue`:]**

```kotlin
private fun initializeBoardStatus() {
    for (i in 0..2) {
        for (j in 0..2) {
            boardStatus[i][j] = -1
            board[i][j].isEnabled = true
            board[i][j].text = ""
        }
    }
}

private fun upadateValue(row: Int, col: Int, player: Boolean) {
    val text: String = if (player) "X" else "O"
    val value: Int = if (player) 1 else 0
    board[row][col].apply {
        isEnabled = false
        setText(text)
    }
    boardStatus[row][col] = value
}
```

Just for orientation, `initializeBoardStatus` and `upadateValue` are unchanged from Stage 3. `initializeBoardStatus` still clears the board, and `upadateValue` still applies a move. The changes in this stage were all about the flow around them — flipping the player and showing messages — not the mechanics of a move itself.

One thing worth pointing out is that the Reset button lambda still doesn't touch `textTurn`. So after pressing Reset, the message on the top TextView will still show whatever it showed at the end of the last game. If you want it to always say "Player X Turn" after Reset, you'd add `updateDisplay("Player X Turn")` inside the Reset lambda. I've left that out on purpose in this stage — we'll come back to polish this up later.

---

## Outro

**[Show: running the app — tap several cells, watch turns switch between X and O, watch the top TextView update, fill the board and see "Game Draw"]**

That's Stage 4. Let's zoom out and see what changed. Before this stage, the game was silent — no message on screen, and every tap placed an X. Now, we have a top TextView that always shows the current state. Each tap places the correct symbol, then flips the player. After nine moves the game announces "Game Draw" so both players know the round is over.

We're really close to a fully working game. But right now, the app doesn't know when someone actually wins. If X gets three in a row on the fifth move, the game just says "Player O Turn" like nothing happened. That's what we'll fix in Stage 5, where we implement the winner detection logic — checking rows, columns, and both diagonals for three in a row. See you there.
