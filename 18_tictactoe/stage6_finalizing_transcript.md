# Stage 6 — Finalizing All Stages (Video Transcript)

A detailed line-by-line transcript for the final stage where we extend winner detection to columns and both diagonals, extract the `announceWinner` helper, and walk through the complete game flow from start to finish. Each section shows the exact code to display on screen followed by the narration.

---

## Intro

**[Show: the running app from Stage 5 — play three X's down the first column, notice the game doesn't announce a win]**

Welcome to the final stage — Stage 6. Take one more look at the Stage 5 build. I play three X's straight down the first column. That should be a win, but the game just says "Player O Turn". Same story if I try a diagonal. Our current `checkWinner` only scans rows.

In this stage we're going to close all those gaps. We'll extend the winner detection to cover every column and both diagonals. We'll also extract a small helper called `announceWinner` so we don't repeat the same X-versus-O logic four times. And at the end I'll walk you through the complete flow of a full game so you can see how every method we've built connects together.

Let's finalize this app.

---

## The Expanded `checkWinner` — Rows Section

**[Show on screen — the top of the new `checkWinner` method:]**

```kotlin
// Scans the board for a three-in-a-row: every row, every column, and both
// diagonals. If any winning line is found we announce the winner and stop
// (returning) so we don't overwrite the message with a later check.
// The `!= -1` guard makes sure three empty cells don't count as a win.
private fun checkWinner() {

    // --- Rows ---
    // Row i wins if boardStatus[i][0] == [i][1] == [i][2] and non-empty.
    for (i in 0..2) {
        if (boardStatus[i][0] != -1 &&
            boardStatus[i][0] == boardStatus[i][1] &&
            boardStatus[i][0] == boardStatus[i][2]) {
            announceWinner(boardStatus[i][0])
            return
        }
    }
```

The row-check section looks similar to what we had in Stage 5, but with two important upgrades.

First, notice the new condition at the start — `boardStatus[i][0] != -1`. This is an explicit guard against empty cells. In Stage 5 we handled this indirectly by only checking against `1` and `0` inside the block. Now we handle it upfront, which makes the code shorter and easier to read.

Second, when we find a winner, instead of writing separate `if` branches for X and O, we call a single helper — `announceWinner(boardStatus[i][0])` — and pass it the value. That helper is a new method we'll look at in a moment, and it handles the "is this X or O" logic in one place.

And notice the last line — `return` instead of `break`. Why the change? Because `checkWinner` now has more code below the row loop — the column loop and the two diagonal checks. If we used `break`, we'd only exit the row loop and would still run all the checks below. If a winning row already announced X's victory, we don't want a later check accidentally overwriting the message. `return` exits the entire method, so once we've announced a winner, no other check runs.

---

## The `checkWinner` — Columns Section

**[Show on screen — the columns block:]**

```kotlin
    // --- Columns ---
    // Column j wins if boardStatus[0][j] == [1][j] == [2][j] and non-empty.
    for (j in 0..2) {
        if (boardStatus[0][j] != -1 &&
            boardStatus[0][j] == boardStatus[1][j] &&
            boardStatus[0][j] == boardStatus[2][j]) {
            announceWinner(boardStatus[0][j])
            return
        }
    }
```

Next, the column checks. This block is structurally almost identical to the row block, but the indices are flipped.

`for (j in 0..2)` loops over the three columns. Column 0 is the leftmost, column 2 is the rightmost.

Inside, we're comparing three cells that all share the same column but different rows. `boardStatus[0][j]` is the top cell of column j, `boardStatus[1][j]` is the middle cell, `boardStatus[2][j]` is the bottom. If all three are equal and none are `-1`, column j is a winning line.

We call `announceWinner(boardStatus[0][j])` and `return` — same pattern as the row block.

Compare this to the row block — the array indices swap positions. In the row block, the row index `i` is fixed and the column index varies. In the column block, the column index `j` is fixed and the row index varies. That's the whole difference.

---

## The `checkWinner` — Diagonals Section

**[Show on screen — the two diagonal checks:]**

```kotlin
    // --- Diagonal: top-left -> bottom-right ---
    // Cells (0,0), (1,1), (2,2) must all match and be non-empty.
    if (boardStatus[0][0] != -1 &&
        boardStatus[0][0] == boardStatus[1][1] &&
        boardStatus[0][0] == boardStatus[2][2]) {
        announceWinner(boardStatus[0][0])
        return
    }

    // --- Diagonal: top-right -> bottom-left ---
    // Cells (0,2), (1,1), (2,0) must all match and be non-empty.
    if (boardStatus[0][2] != -1 &&
        boardStatus[0][2] == boardStatus[1][1] &&
        boardStatus[0][2] == boardStatus[2][0]) {
        announceWinner(boardStatus[0][2])
        return
    }
}
```

Now the two diagonal checks. Unlike rows and columns, we only have two diagonals total — one going from top-left to bottom-right, and one going from top-right to bottom-left. Because there are only two, there's no reason for a loop. We just write each check explicitly.

The first diagonal goes through cells `(0,0)`, `(1,1)`, and `(2,2)`. That's the top-left corner, the center, and the bottom-right corner. Same pattern — check that the top-left isn't empty, then check that it matches the center and the bottom-right. If yes, announce the winner and return.

The second diagonal goes through cells `(0,2)`, `(1,1)`, and `(2,0)`. That's the top-right corner, the center, and the bottom-left corner. Same guard, same match check, same announce-and-return.

Both diagonals pass through the center cell `(1,1)`, which is why the middle of the board is such a powerful position in Tic-Tac-Toe — it's the only cell that's part of four winning lines (a row, a column, and both diagonals).

The closing brace ends the `checkWinner` method. We now have complete coverage — three rows, three columns, two diagonals, eight possible winning lines in total.

---

## The New `announceWinner` Helper

**[Show on screen — the new small helper method:]**

```kotlin
// Small helper: turn the numeric marker into a win message.
// Called from every winning-line branch in checkWinner() so we don't
// repeat the same if/else four times.
private fun announceWinner(value: Int) {
    if (value == 1) {
        updateDisplay("Player X wins")
    } else if (value == 0) {
        updateDisplay("Player O wins")
    }
}
```

Here's the small helper I referenced earlier.

`private fun announceWinner(value: Int)` takes the numeric marker from `boardStatus` as a parameter.

Inside, it's a simple if-else if. If the value is `1`, that's X, so we call `updateDisplay("Player X wins")`. If the value is `0`, that's O, so we call `updateDisplay("Player O wins")`.

Why extract this? Because otherwise, every single one of our eight winning-line checks would need this if-else block inline. That's a lot of duplicated code. By putting the branching in one place, our `checkWinner` method stays short and focused on detecting lines. If we ever want to change the win message — say to "🏆 Player X wins the round" — we'd only touch one line.

This is a really common refactoring pattern. Whenever you see the same three or four lines of code repeated in multiple places, ask yourself if they belong in a helper method. In this case, extracting `announceWinner` shrinks `checkWinner` significantly and makes it easier to read.

Notice we don't have a branch for `-1` — the empty marker. That's fine because we only ever call `announceWinner` from inside a block that already confirmed the value is not `-1`. So if `value` reaches this method, it's guaranteed to be either `1` or `0`.

---

## Unchanged Pieces

**[Show on screen — briefly scroll through the pieces that stayed the same:]**

```kotlin
private fun updateDisplay(text: String) {
    textTurn.text = text
    if (text.contains("wins")) {
        disableButton()
    }
}

private fun disableButton() {
    for (row in board) {
        for (button in row) {
            button.isEnabled = false
        }
    }
}
```

For orientation, the rest of the class is the same as Stage 5. `updateDisplay` still writes to `textTurn` and calls `disableButton` when it sees a "wins" message. `disableButton` still loops over every cell and locks it. `initializeBoardStatus`, `onCreate`, `onClick`, `upadateValue`, and the Reset button lambda are all unchanged. All the new work in this stage is contained in `checkWinner` and the new `announceWinner` helper.

---

## Walkthrough — A Complete Game From Start to Finish

**[Show: run the app and play through a full game while narrating]**

Now let me walk you through what happens end to end when you play a full game with this final code. Follow along in your head as I describe each step.

**Startup.** You launch the app. Android calls `onCreate`. Inside `onCreate` we call `super.onCreate`, then `enableEdgeToEdge`, then `setContentView` which inflates the XML layout. We call `findViewById` nine times for the cells, once for the reset button, and once for the turn TextView. We build the 2D `board` array. We call `initializeBoardStatus`, which sets every cell in `boardStatus` to `-1`, re-enables every button, and clears the button text. Then we loop over the board and register `this` as the click listener for every cell. And we register the Reset button's lambda. The top TextView shows "Player X turn" from the XML default. `PLAYER` is `true`, `TURN_COUNT` is `0`.

**First tap.** You tap the center cell, `button5`. Android calls our `onClick` method with `view` pointing at `button5`. The `when` block matches `R.id.button5` and calls `upadateValue(1, 1, true)`. Inside `upadateValue`, `text` becomes "X" and `value` becomes `1`. The `apply` block sets `button5`'s text to "X" and disables it. Then we set `boardStatus[1][1] = 1`.

Back in `onClick`, we hit `TURN_COUNT++` — the counter is now `1`. We hit `PLAYER = !PLAYER` — `PLAYER` is now `false`. The if-else sees `PLAYER` is `false`, so it calls `updateDisplay("Player O Turn")`. The top TextView changes to "Player O Turn". `TURN_COUNT` isn't `9`, so no draw. Then `checkWinner` runs. It loops rows — nothing matches, only the center has a value. Loops columns — same. Checks both diagonals — same. Returns without announcing anything.

**A few more moves.** O taps the top-left corner. Same flow. X taps the top-right. O taps the bottom-right. X taps the top-middle. Each tap advances turn count, flips player, updates the display, checks for a winner.

**The winning move.** Suppose X plays center, top-middle, and now bottom-middle — that's three X's down the middle column. When X taps the last cell, `upadateValue` writes "X" to it and sets `boardStatus[2][1] = 1`. `TURN_COUNT` becomes 5, `PLAYER` flips to O, `updateDisplay("Player O Turn")` runs. Then `checkWinner` starts.

The row loop checks each row — none matches. The column loop starts. When `j = 1`, it checks `boardStatus[0][1]`, `boardStatus[1][1]`, `boardStatus[2][1]`. All three equal `1`, and `1` is not `-1`. Match. It calls `announceWinner(1)`, which calls `updateDisplay("Player X wins")`.

Inside `updateDisplay`, we set the top TextView text to "Player X wins", overwriting the "Player O Turn" we set a moment ago. Then the `contains("wins")` check is true, so we call `disableButton`, which loops over the board and disables every button. `updateDisplay` returns. `announceWinner` returns. Back in `checkWinner`, we hit `return`, which exits the method immediately — no diagonal checks needed.

**End state.** The screen now shows "Player X wins", the board is completely locked, and only the Reset button responds to taps.

**Reset.** The player taps Reset. The lambda sets `PLAYER = true`, `TURN_COUNT = 0`, and calls `initializeBoardStatus`. That loop sets every cell in `boardStatus` back to `-1`, re-enables every button, and clears the visible text. The board is fresh. The top TextView still shows "Player X wins" though, because Reset doesn't touch it. If you wanted it to also reset, you'd add `updateDisplay("Player X Turn")` inside the Reset lambda.

**Draw scenario.** If neither player wins and the board fills up, `TURN_COUNT` reaches `9`, and inside `onClick` we call `updateDisplay("Game Draw")`. `checkWinner` still runs after that but finds no winning line, so the draw message stays. All nine buttons are already disabled from the previous moves.

---

## Outro

**[Show: final polished game, play a full round, hit reset, play another]**

And that's the entire Tic-Tac-Toe app, complete from empty file to full game in six stages.

To recap the journey — in Stage 1 we designed the visual layout in XML with a ConstraintLayout, a vertical LinearLayout, three rows of buttons using layout weights, and a Reset button. In Stage 2 we wired those views into Kotlin by implementing `View.OnClickListener`, using `findViewById`, and building a 2D `board` array. In Stage 3 we handled button clicks by adding game state variables, an `initializeBoardStatus` method, and an `upadateValue` method that applies moves and disables tapped cells. In Stage 4 we added the game draw state with turn switching, a top TextView, an `updateDisplay` helper, and a nine-move draw check. In Stage 5 we added row-based winner detection and the `disableButton` method that locks the board on a win. And in Stage 6 we finalized everything with full row, column, and diagonal detection, plus an `announceWinner` helper to keep the code clean.

From here, you can take this app in many directions. Add color to the winning line so it's visually obvious which cells won. Add a scoreboard that persists X-versus-O wins across multiple rounds. Add sound effects for taps and wins. Or even build a simple computer opponent using a minimax algorithm.

Thanks so much for following along through this series. If it helped you, please leave a like and subscribe. Drop any questions in the comments and I'll do my best to answer them. See you in the next series.
