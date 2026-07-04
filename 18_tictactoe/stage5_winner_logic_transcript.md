# Stage 5 — Implementing the Winner Logic (Video Transcript)

A detailed line-by-line transcript for the stage where we detect a winning row and lock the board once a winner is announced. Each section shows the exact code to display on screen followed by the narration.

---

## Intro

**[Show: the running app from Stage 4 — play 3 X's in the top row, notice the game just says "Player O Turn" like nothing happened]**

Welcome to Stage 5. We're really close to a full Tic-Tac-Toe game. Watch what happens in the Stage 4 build. I place three X's across the top row — that's a winning move — but the game doesn't notice. It just says "Player O Turn" and lets the game continue. We haven't taught the app how to detect a winner yet.

In this stage we're going to add exactly that. We'll write a method that scans the board after every move and looks for three matching values in a row. When it finds one, it announces the winner and locks the whole board so no more taps register.

To keep the stage focused, we're going to start with row detection only. In the next stage we'll extend this to cover columns and both diagonals. Let's dive in.

---

## Calling `checkWinner` After Every Move

**[Show on screen — the end of the `onClick` method, highlight the new line:]**

```kotlin
// After 9 moves with no winner, the game is a draw.
if (TURN_COUNT == 9) {
    updateDisplay("Game Draw")
}

// Finally, see if the last move produced a three-in-a-row.
checkWinner()
```

At the very end of `onClick`, we've added one new line — `checkWinner()`. This is a call to a new private method we're about to write. It runs after every move.

The order here is important. First we apply the move in the `when` block. Then we increment `TURN_COUNT` and flip `PLAYER`. Then we update the top TextView to show the new turn. Then we check for a draw at nine moves. And finally, we check for a winner.

That last call happens after everything else, so if a winner is found, the message it displays overrides whatever text we just set for "Player X Turn" or "Player O Turn". This is exactly what we want — if someone just won, we don't want the screen still saying "Player O Turn".

---

## The `checkWinner` Method

**[Show on screen — the full new private method:]**

```kotlin
// Scans each row for three matching non-empty values. When found, announces
// the winner through updateDisplay() (which also disables the board).
private fun checkWinner() {
    for (i in 0..2) {
        // All three cells in row i must hold the same value...
        if (boardStatus[i][0] == boardStatus[i][1] &&
            boardStatus[i][0] == boardStatus[i][2]) {
            // ...and that value must be a real move (1 for X, 0 for O),
            // not the -1 empty marker.
            if (boardStatus[i][0] == 1) {
                updateDisplay("Player X wins")
                break
            }
            if (boardStatus[i][0] == 0) {
                updateDisplay("Player O wins")
                break
            }
        }
    }
}
```

Let's walk through this method line by line. It's short but there's a lot going on.

`private fun checkWinner()` declares a private method that takes no parameters and returns nothing. It reads from the `boardStatus` field to do its work, so it doesn't need arguments.

`for (i in 0..2)` starts a loop that runs three times — once for each row. On each iteration, `i` will be 0, then 1, then 2. Row 0 is the top row of the grid, row 1 is the middle, row 2 is the bottom.

Then we have the row-check condition:

`if (boardStatus[i][0] == boardStatus[i][1] && boardStatus[i][0] == boardStatus[i][2])`

Let me unpack this. `boardStatus[i][0]` is the first cell of row `i`. `boardStatus[i][1]` is the second cell. `boardStatus[i][2]` is the third cell. So this line asks: is the first cell equal to the second, and is the first cell also equal to the third? If both parts are true, all three cells in this row hold the same value.

But here's the catch — three empty cells also satisfy this condition. All three would be `-1`, and `-1 == -1 == -1` is true. That would falsely count as a winning row. So we need one more layer of checking.

That's what the next lines do:

`if (boardStatus[i][0] == 1) { updateDisplay("Player X wins"); break }`

If all three cells in the row match and they hold the value `1`, that's X's marker, so X wins. We call `updateDisplay("Player X wins")` to announce it, and then `break` to exit the `for` loop early. We don't need to keep checking the remaining rows once we've found a winner.

`if (boardStatus[i][0] == 0) { updateDisplay("Player O wins"); break }`

If all three cells match and they hold `0`, that's O's marker, so O wins. Same pattern — announce, then break.

Notice we don't have a branch for `-1`. That's intentional. If the row is all empty, none of these inner `if`s are true, so nothing happens — we just fall through to the next row.

There's a small quirk in this version of the code that I want to point out. The two inner `if`s check for `1` and `0` at the same nesting level as the outer row-match check. Because we use `break` inside them, we can safely be confident that if we hit the "X wins" branch, we won't also hit the "O wins" branch — a cell can only hold one value at a time. But structurally, this pattern only detects row wins. In the next stage we'll extend this method to also cover columns and both diagonals.

---

## The Updated `updateDisplay` Helper

**[Show on screen — highlight the two new lines inside `updateDisplay`:]**

```kotlin
// Small helper so all screen-text updates go through one place.
// If the message announces a win, lock the board so no more moves happen.
private fun updateDisplay(text: String) {
    textTurn.text = text
    if (text.contains("wins")) {
        disableButton()
    }
}
```

Remember `updateDisplay` from Stage 4? It used to be a one-liner that just set the TextView text. Now it does something extra.

The first line, `textTurn.text = text`, is the same as before — it updates the top TextView.

Then we've added an `if` check. `text.contains("wins")` uses Kotlin's built-in `contains` method on strings. It returns `true` if the string "wins" appears anywhere in `text`. Our winning messages are "Player X wins" and "Player O wins", so both of them contain "wins" and the condition is true. Every other message we display — "Player X Turn", "Player O Turn", "Game Draw" — does not contain "wins", so the condition is false.

When the condition is true, we call `disableButton()`, which locks the entire board so no more moves can be placed. We'll look at that method in a moment.

This is exactly why we centralized text updates into a helper method. Without `updateDisplay`, we'd have to remember to call `disableButton()` after every win-related text change everywhere in our code. By putting the check inside the helper, it happens automatically no matter which line triggers the win message.

---

## The `disableButton` Method

**[Show on screen — the new private method:]**

```kotlin
// Called once the game is over. Walks the 3x3 grid and disables every
// cell button so players can't keep tapping after a winner is declared.
// (The Reset button stays enabled so a new game can be started.)
private fun disableButton() {
    for (row in board) {
        for (button in row) {
            button.isEnabled = false
        }
    }
}
```

Now the method that actually locks the board.

`private fun disableButton()` — again, no parameters, no return value.

Inside we have the same nested-loop pattern we've seen a couple of times before. The outer loop walks through the three rows in `board`. The inner loop walks through the three buttons in each row.

For every button, we set `isEnabled = false`. That's the standard Android property that greys out a view and makes it stop responding to taps. So after this method runs, all nine cells are frozen. Even if the user taps them, nothing happens.

Notice we're only touching the nine grid buttons in `board`. We're not disabling `buttonReset` — that stays enabled on purpose. The player needs a way to start a new game after a win, and the Reset button is that way. If we disabled it too, the app would be stuck.

Here's a nice detail — when the Reset button is tapped, its lambda calls `initializeBoardStatus()`, which loops through every cell and sets `isEnabled = true` again. So the disable and the re-enable pair up cleanly across the reset cycle.

---

## The Untouched Pieces

**[Show on screen — briefly scroll through the unchanged methods:]**

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

For orientation, `initializeBoardStatus` and `upadateValue` are unchanged from earlier stages. The state variables at the top of the class, `onCreate`, and the Reset button lambda are also the same. Everything new in this stage is contained in three places — the one-line call to `checkWinner` at the bottom of `onClick`, the new `checkWinner` method itself, the extra `if` inside `updateDisplay`, and the new `disableButton` method.

---

## Outro

**[Show: running the app — play three X's in the top row, watch "Player X wins" appear, watch every remaining cell freeze so taps do nothing, then tap Reset to unfreeze everything]**

That's Stage 5. Let's zoom out. Before this stage, the game let you keep playing forever, even after someone had clearly won. Now, after every move, the app scans each row for three matching non-empty values. If it finds a winning row, it announces the winner on the top TextView. Because that announcement contains the word "wins", our `updateDisplay` helper automatically triggers `disableButton`, which locks the entire grid. The player sees the win message and can't accidentally place any more marks.

There are still two important limitations to fix. First, we only check rows — a win along a column or a diagonal is invisible to us. Second, the draw check in `onClick` runs before `checkWinner`, so on the ninth move if X wins with the last move, we might overwrite "Player X wins" with "Game Draw" briefly before `checkWinner` fixes it — actually, no, we're safe because `checkWinner` runs last. But we should still keep an eye on ordering.

In Stage 6 we'll finalize the app by extending `checkWinner` to cover columns and both diagonals, and we'll walk through the complete flow of a full game from tap to win. See you there.
