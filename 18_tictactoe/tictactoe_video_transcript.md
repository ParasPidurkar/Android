# Tic-Tac-Toe Android App — Video Transcript

A walkthrough of building a Tic-Tac-Toe app in Kotlin + XML, explained in six stages.

---

## Intro

Hi everyone. In this video I'm going to walk you through how I built a Tic-Tac-Toe game for Android using Kotlin and an XML layout. I'll take you through the code in six stages, from the empty layout all the way to a fully working game with win detection. For each stage, I'll show you what we're adding, why we're adding it, and how it connects to the rest of the app. Let's get started.

---

## Stage 1 — Building the XML Layout

We start with the visual side of the app: the file `activity_main.xml`. The root of the screen is a `ConstraintLayout`, which is the standard container Android Studio gives you when you create a new activity.

Inside that, I've added a vertical `LinearLayout` with the id `board`. This LinearLayout is going to hold the entire game area, and I've constrained it to all four edges of the screen so it stays centered.

Inside `board`, we have three more horizontal `LinearLayout`s, one for each row of the tic-tac-toe grid. Each row contains three `Button`s. The important attribute on each button is `android:layout_width="0dp"` combined with `android:layout_weight="1"`. This is the classic Android trick to make the three buttons in a row share the horizontal space equally, no matter the screen size. The height is fixed at `100dp` so we get nice square-ish cells, and the text size is `32sp` so the X and O will be clearly visible.

Below the three rows, there's a Reset button that spans the full width. That's the button players will tap to start a new game.

At this stage, all the buttons are blank. We haven't wired anything up yet, we're just laying out the visual pieces on the screen.

---

## Stage 2 — Adding the Turn Indicator and Button Spacing

The board looked okay, but there was no way for a player to tell whose turn it was. So I added a `TextView` at the top of the board layout with the id `textTurn`. It starts out saying "Player X turn", it's centered, bold, and has a text size of 24sp so it stands out.

I also added a bottom margin to that TextView so it doesn't sit right on top of the first row of buttons.

The other visual issue was that the buttons in each row were touching each other. That made the grid look cramped. The fix was to add `android:layout_margin="4dp"` to each cell button. That gives every button a small gap on all four sides, so you see a proper grid instead of one big block.

That's the entire visual layout — a title at the top, a 3x3 grid in the middle, and a reset button at the bottom.

---

## Stage 3 — Setting Up the Activity and Click Handling

Now we jump into the Kotlin code, `MainActivity.kt`. This is the file that actually makes the app do something.

The first thing to notice is the class declaration. I wrote `class MainActivity : AppCompatActivity(), View.OnClickListener`. The important part here is `View.OnClickListener`. Implementing this interface means the Activity itself can act as the click listener for any View. This lets me pass `this` to `setOnClickListener(...)` on all nine buttons, and every single click will land in one method — `onClick(view: View?)` — where I use the view's id to figure out which button was pressed. It's a cleaner alternative to writing nine separate lambdas.

Inside `onCreate`, I call `enableEdgeToEdge()` for a modern edge-to-edge UI, and then `setContentView` to inflate the XML layout I just described.

Next, I use `findViewById` on each of the nine cell buttons and on the reset button. `findViewById` is what links the XML view — identified by its `android:id` — to a Kotlin variable I can actually control from code.

Then I group the nine buttons into a 2D array called `board`. So `board[0]` is the top row, `board[1]` is the middle row, and `board[2]` is the bottom row. Using a 2D structure makes the game logic much easier later on, because I can loop over rows and columns instead of hardcoding button references.

Finally, I loop over the board and call `setOnClickListener(this)` on every cell. Because the class implements `View.OnClickListener`, passing `this` is valid — the framework will call our `onClick` method whenever a cell is tapped.

The reset button gets its own separate lambda, since its behavior is unique.

---

## Stage 4 — Adding Game State

An app that just responds to clicks isn't a game yet — we need to track what's happening on the board. So I added three state variables at the top of the class.

`PLAYER` is a boolean: `true` means it's X's turn, `false` means it's O's turn. Every time someone makes a move, we flip it.

`TURN_COUNT` is an integer that counts how many moves have been made. Once it hits 9 with no winner, we know the game is a draw.

`boardStatus` is a 3x3 integer array that mirrors the grid. I use `-1` to mean the cell is empty, `1` for X, and `0` for O. I keep this separate from the actual Button views because it's much easier to check for a winner using pure data instead of parsing the text on each button.

I also added an `initializeBoardStatus()` method. It runs at startup and whenever the reset button is pressed. It loops over every cell, sets `boardStatus[i][j]` to `-1`, re-enables the button so it can be tapped again, and clears any leftover X or O text.

Then there's `upadateValue()`, which is the method that actually applies a move. Given a row, a column, and which player is moving, it figures out the right symbol — X or O — sets the button's text, disables that button so you can't tap the same cell twice, and records the move in `boardStatus` as either 1 or 0.

Inside `onClick`, I use a `when` block on `view?.id` to translate the button that was clicked into a row and column, then call `upadateValue` with those coordinates.

---

## Stage 5 — Turn Switching and Display Updates

Now the game can register moves, but it doesn't yet advance the turn or tell the player anything. So at the end of `onClick`, after the move is applied, I do three things.

First, I increment `TURN_COUNT`. Second, I flip `PLAYER` using `PLAYER = !PLAYER`. Third, I update the on-screen TextView by calling `updateDisplay()` with either "Player X Turn" or "Player O Turn" depending on the new value of `PLAYER`.

`updateDisplay(text: String)` is a tiny helper method whose only job is to write text into the `textTurn` TextView. Keeping this in one place means that later, if I want to change the styling or animate the message, I only touch one method.

I also added a check: if `TURN_COUNT` equals 9, we call `updateDisplay("Game Draw")`, because the board is full.

To make this work, I declared `textTurn` as a `lateinit var TextView` at the top of the class and hooked it up with `findViewById(R.id.textTurn)` inside `onCreate`.

---

## Stage 6 — Winner Detection and Locking the Board

The final piece is detecting a winner. At the very end of `onClick`, I call `checkWinner()`.

`checkWinner()` scans four kinds of winning lines. First, it loops through all three rows and checks if all three cells in that row hold the same value and are not empty. Then it does the same thing for all three columns. Then it checks the top-left to bottom-right diagonal, which is cells `(0,0)`, `(1,1)`, and `(2,2)`. And finally the top-right to bottom-left diagonal, which is `(0,2)`, `(1,1)`, and `(2,0)`.

In every case, the important guard is `!= -1`, because otherwise three empty cells would trivially match each other and falsely count as a win.

When a winning line is found, I call `announceWinner(value)`, a small helper that converts the numeric marker into a message — "Player X wins" for `1`, "Player O wins" for `0`. I extracted this helper so I don't repeat the same if/else four times across all the win checks.

Notice that `checkWinner` uses `return` instead of `break` after announcing a winner. That's intentional. `return` exits the whole method, so once we've declared a winner, none of the later checks can accidentally overwrite the message.

`updateDisplay` has one more trick in it. If the text it's about to show contains the word "wins", it also calls `disableButton()`. That method loops over the entire board and sets `isEnabled = false` on every cell, so once the game is over, players can't keep tapping and messing up the winning display. The reset button stays enabled, so a new game is always just one tap away.

---

## Outro

And that's the whole app. We started with an empty ConstraintLayout, added a grid of buttons with proper spacing, wired everything up in Kotlin using a single click listener, added state to track the game, made the turn indicator responsive, and finished with full row, column, and diagonal winner detection. From here you could add color highlights for the winning line, a score counter across games, or even a simple AI opponent. Thanks for watching.
