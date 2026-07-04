# Stage 3 — Handling Button Clicks (Video Transcript)

A detailed line-by-line transcript for the stage where we make the buttons actually respond to taps and place X's and O's on the board. Each section shows the exact code to display on screen followed by the narration.

---

## Intro

**[Show: the finished-Stage-2 `MainActivity.kt` with empty `onClick` branches, then the running app where taps do nothing]**

Welcome back to Stage 3. In the last stage we mapped every XML view into Kotlin variables and hooked up an empty click handler. Tapping a button already flows through our `onClick` method, but the method itself does nothing — the game is silent. In this stage we're going to fix that. By the end of this video, you'll be able to tap any cell and see an X or an O appear on it, and you won't be able to tap the same cell twice. We'll also make the Reset button actually clear the board.

The changes in this stage are focused around three new pieces: game state variables at the top of the class, an initialization method that sets the board to empty, and an update method that applies a move. Let's go through them one at a time.

---

## New State Variables

**[Show on screen — highlight these three new lines added right below the class declaration:]**

```kotlin
// true = Player X's turn, false = Player O's turn. Flipped after each move.
var PLAYER = true

// Number of moves made so far. Used later to detect a draw (max 9).
var TURN_COUNT = 0

// 3x3 grid tracking the game state numerically:
//   -1 = empty cell, 1 = X, 0 = O.
// Kept separate from the Buttons so game logic (win checks, etc.) can work
// on plain data instead of parsing button text.
var boardStatus = Array(3) { IntArray(3) }
```

Right below the class declaration we now have three new properties. I've added comments above each one to document what they do.

The first one, `var PLAYER = true`, is a boolean that tracks whose turn it is. I chose `true` for X because X always goes first in Tic-Tac-Toe. Every time a move is completed, we'll flip this variable, which means the next tap will be treated as the other player. The initial value of `true` means X starts. Why `var` and not `val`? Because we intend to change it — `val` in Kotlin means immutable, and this variable clearly needs to change over time.

The second one, `var TURN_COUNT = 0`, is a move counter. A Tic-Tac-Toe board has nine cells. Once nine moves have been played without a winner, the game is a draw. So this counter starts at zero and will be incremented after every successful move. We don't use it in this stage yet, but we're declaring it here so it's ready for the draw logic in the next stage.

The third one, `var boardStatus = Array(3) { IntArray(3) }`, is a bit denser. Let me unpack it. `Array(3) { IntArray(3) }` is Kotlin's syntax for creating an array with an initializer lambda. It says "make an array of size 3, and for each of those three slots, run this lambda to produce a value". The lambda in braces returns `IntArray(3)`, which is a new integer array of size 3 filled with zeros by default. So the outer array has three elements, and each element is itself an integer array of size 3. That's a 3x3 grid.

Now, why keep this separate from the actual buttons? Because if we relied on the button text to check for winners, we'd have to compare strings like "X" and "O" and handle empty strings for empty cells. That's fragile. With `boardStatus`, we work with clean integer values — `-1`, `0`, or `1` — which are much easier and safer to compare.

Note that this array will start with all zeros by default from `IntArray(3)`. That's not what we want, because zero is our code for O. So we're going to manually initialize every cell to `-1` in a moment.

**[Show on screen — highlight these two lines that we already had from Stage 2:]**

```kotlin
lateinit var board: Array<Array<Button>>
lateinit var buttonReset: Button
```

The `board` and `buttonReset` properties below these are unchanged from Stage 2.

---

## The New Line in `onCreate`

**[Show on screen — highlight this new line right after building the `board` array:]**

```kotlin
// Set every cell to the "empty" state before the first move.
initializeBoardStatus()
```

Inside `onCreate`, most of the code is the same as before. `findViewById` calls, building the `board` array, hooking up click listeners — all the same. But there's one new line I want to point out.

This is a call to a new method we're about to write. We put it right after we build the `board` array so that both the buttons and the board array exist by the time it runs. Its job is to make sure every cell in `boardStatus` starts at `-1` and every button is in a clean, tappable state before the user makes the first move.

---

## The New Reset Button Lambda

**[Show on screen — the Reset button lambda, now with content:]**

```kotlin
// Reset button: restart from Player X, zero moves, and clear the board.
buttonReset.setOnClickListener {
    PLAYER = true
    TURN_COUNT = 0
    initializeBoardStatus()
}
```

Look at the Reset button's click listener. In Stage 2 this lambda was empty with just a placeholder comment. Now it actually does something.

`PLAYER = true` puts the game back to X's turn.

`TURN_COUNT = 0` resets the move counter.

`initializeBoardStatus()` clears every cell — both the underlying data and the visible buttons.

Those three lines together return the entire game to the exact state it was in when the app first launched.

---

## The `initializeBoardStatus` Method

**[Show on screen — the new private method:]**

```kotlin
// Sets every cell back to empty: -1 in boardStatus, re-enabled and blank
// on screen. Called at startup and whenever Reset is pressed.
private fun initializeBoardStatus() {
    for (i in 0..2) {
        for (j in 0..2) {
            boardStatus[i][j] = -1        // -1 marks the cell as empty
            board[i][j].isEnabled = true  // allow it to be tapped again
            board[i][j].text = ""         // clear any "X" or "O" text
        }
    }
}
```

Let's look at the new private method that does all this initialization work.

`private` means this method can only be called from inside this class. That's good hygiene — it's an internal helper, not something outside code should call directly.

`fun` is Kotlin's function keyword. `initializeBoardStatus` is the name, and the empty parentheses mean it takes no parameters.

Inside, we have two nested for loops. `for (i in 0..2)` is Kotlin's syntax for "iterate `i` over the values 0, 1, and 2". `0..2` creates an inclusive range that includes both endpoints. So the outer loop runs three times for rows, and the inner loop runs three times for columns. In total, the body runs nine times — once for every cell.

Inside the innermost body we do three things:

`boardStatus[i][j] = -1` — set this cell's data to `-1`, our code for "empty".

`board[i][j].isEnabled = true` — re-enable the button. This matters because after a game ends, we're going to disable all buttons so no one can keep tapping. When we reset, we need to allow tapping again.

`board[i][j].text = ""` — clear any X or O text that might be on the button from a previous game. Empty quotes is an empty string.

Once both loops finish, all nine cells are back to a fresh state.

---

## The Filled-In `onClick` Method

**[Show on screen — the updated `onClick` with all branches filled:]**

```kotlin
// Single entry point for all cell taps. We translate the tapped button's
// id into its (row, col) position and forward to updateValue().
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
}
```

The `onClick` method used to have nine empty branches. Now every branch calls the new `upadateValue` function with the correct grid coordinates.

Look at the first branch. When `button1` is tapped, we call `upadateValue` with row 0, column 0, and the current player. Notice I'm using named arguments — `row = 0`, `col = 0`, `player = PLAYER`. Kotlin lets you write parameter names at the call site, and I like doing it for methods with multiple parameters because it makes the call self-documenting. Anyone reading the code instantly knows what each number means.

The other eight branches follow the same pattern, mapping each button id to its position in the 3x3 grid. `button1` through `button3` are row 0, `button4` through `button6` are row 1, and `button7` through `button9` are row 2. The column cycles 0, 1, 2 within each row.

Notice we're passing `PLAYER` — the current class-level variable — as the player argument. So whichever player's turn it is right now will be applied to the tapped cell.

---

## The `upadateValue` Method

**[Show on screen — the new method at the bottom of the class:]**

```kotlin
// Applies a move at (row, col): shows the mark on the button, disables the
// cell so it can't be tapped again, and records the move in boardStatus.
private fun upadateValue(row: Int, col: Int, player: Boolean) {
    val text: String = if (player) "X" else "O"   // symbol shown on the button
    val value: Int = if (player) 1 else 0         // numeric code stored in boardStatus
    board[row][col].apply {
        isEnabled = false   // cell can't be tapped again
        setText(text)       // show X or O
    }
    boardStatus[row][col] = value
}
```

Now the star of this stage — the method that actually applies a move.

Quick note — you'll see `upadateValue` is misspelled. It should be `updateValue`. That's a typo I introduced earlier and I've left it in for consistency. In a real project you'd rename it.

The parameters are the row and column of the cell being played, and the boolean for which player is playing. All three come from the `onClick` branches.

The first line inside, `val text: String = if (player) "X" else "O"`, uses a Kotlin `if` as an expression — the entire `if/else` returns a value. So `text` gets assigned "X" if `player` is true, and "O" otherwise. This is the string that will be shown on the button.

The next line, `val value: Int = if (player) 1 else 0`, does the same thing but for the numeric code we'll store in `boardStatus`. `1` for X, `0` for O. These match our earlier convention.

Then we have the button update using `apply`. `apply` is a Kotlin scope function. It calls the block on the receiver — in this case, the button at `board[row][col]` — and lets us reference the button implicitly using `this` inside the block. So `isEnabled = false` inside is really `board[row][col].isEnabled = false`, and `setText(text)` is really `board[row][col].setText(text)`.

`apply` is nice here because we're making multiple changes to the same object. Without it, we'd have to write `board[row][col]` on every single line, which is repetitive.

`isEnabled = false` disables the button so it can't be tapped again. This is how we prevent overwriting a move. Once a player has claimed a cell, that cell is locked.

`setText(text)` sets the button's visible text to either "X" or "O".

Finally, outside the `apply` block, `boardStatus[row][col] = value` records the move in our data model too. The button now shows "X" or "O" to the user, and `boardStatus` remembers it as a `1` or a `0` for the game logic.

---

## Outro

**[Show: running the app — tap several cells and see X's appearing; tap Reset and watch the board clear]**

That's Stage 3. Let's zoom out. Before this stage, tapping the buttons did nothing. Now, when a user taps any cell, the app looks at whose turn it is, writes the correct symbol on the button, disables the button so it can't be tapped again, and records the move in the underlying data grid. The Reset button now actually clears the board back to a fresh state.

There's one thing that's still missing though — did you catch it? The `PLAYER` variable never flips. So right now, if you tap five cells, all five will show "X" because we never switch turns after a move. And there's no message telling the player whose turn it is, no announcement when the board fills up.

That's exactly what we'll add in Stage 4, when we implement the game draw state, the turn indicator TextView, and the turn-switching logic. See you there.
