# Tic-Tac-Toe Android App — Detailed Video Transcript

A line-by-line walkthrough for a video, organized into six stages.

---

## Intro

Hi everyone, welcome back. In this video I'm going to build a complete Tic-Tac-Toe game for Android using Kotlin and I'll explain every single line so you understand not just what the code does but why it's written that way. I've broken the video into six stages. In the first stage we'll design the layout in XML. In the second stage we'll map that layout into our Kotlin code. In the third stage we'll handle button clicks and make moves happen. In the fourth stage we'll add the game draw logic and the turn indicator. In the fifth stage we'll implement the winner detection for rows, columns, and diagonals. And in the last stage we'll finalize everything and walk through the full flow together. Let's get into it.

---

## Stage 1 — Creating the Layout

Open the file `res/layout/activity_main.xml`. This is the file that defines what the user sees on the screen.

The very first line is the XML declaration, `<?xml version="1.0" encoding="utf-8"?>`. Every Android layout file needs this at the top. It just tells the system which XML version and text encoding we're using. You never really touch it.

Right after that we have our root element, which is a `ConstraintLayout`. I'm using `androidx.constraintlayout.widget.ConstraintLayout` because it's the modern flexible container Android Studio gives us by default. It lets us pin children to the edges of the screen or to each other. On this root I've declared three XML namespaces — `android`, `app`, and `tools`. The `android` namespace is required for all standard attributes like `android:layout_width`. The `app` namespace is where ConstraintLayout attributes live, like `app:layout_constraintTop_toTopOf`. And the `tools` namespace is only used at design time in Android Studio, it doesn't affect the running app.

I've given the ConstraintLayout an id of `main`, and set both its width and height to `match_parent` so it fills the entire screen. `tools:context` just tells Android Studio which activity this layout belongs to, so autocomplete works better.

Inside the ConstraintLayout, I've added a vertical `LinearLayout` with the id `board`. This LinearLayout is going to be the container for everything visible: the turn text, the grid of buttons, and the reset button. I set the width to `match_parent` and the height to `wrap_content` so it takes the full screen width but only as much vertical space as its children need. I added `android:padding="16dp"` so there's a nice gap between the game and the edge of the screen. The four `layout_constraint` attributes pin this LinearLayout to all four edges of the parent, which effectively centers it vertically on the screen.

The first child of `board` is a `TextView` with the id `textTurn`. This is the turn indicator that shows "Player X turn" at the start. I made it centered using `android:gravity="center"`, gave it a text size of `24sp` and bold styling so it's easy to read, and added a `16dp` bottom margin so it doesn't sit right on top of the buttons.

Below the TextView, I have three horizontal LinearLayouts, one for each row of the tic-tac-toe grid. Each of these row LinearLayouts has width `match_parent` and height `wrap_content`, with orientation `horizontal` so its children — the three buttons in that row — sit side by side.

Inside each row I have three Buttons. Each button has an id from `button1` through `button9`. The critical trick here is that I set `android:layout_width="0dp"` and `android:layout_weight="1"`. When you combine a width of zero with a weight, Android tells the LinearLayout to distribute the available horizontal space equally between all children with weight. That's why the three buttons in a row always end up the same width no matter the screen size. The height is `100dp`, which is a fixed size that gives us big tappable cells. Text size is `32sp` so the X and O are clearly visible.

I also added `android:layout_margin="4dp"` to each button. This gives every cell a small margin on all four sides so the buttons don't touch each other and the grid looks like a proper board.

Finally, below the three row LinearLayouts, I added the Reset button with the id `buttonReset`. Its width is `match_parent` so it spans the entire width of the board, and I gave it a `24dp` top margin so it's clearly separated from the grid. The text is just "Reset".

And that's the entire layout. When you preview it in Android Studio you should see the turn message at the top, a 3x3 grid of empty buttons in the middle, and a full-width reset button at the bottom.

---

## Stage 2 — Mapping the Code with the Design

Now we jump into `MainActivity.kt`. This is the Kotlin file where all the logic lives.

At the top we have the `package` declaration, `package com.example.tictactoe`. This has to match the folder structure and it's how Android identifies your class.

Then we have the imports. `android.os.Bundle` is needed because `onCreate` takes a `Bundle` parameter. `android.view.View` is needed because our click handler receives a View. `android.widget.Button` and `android.widget.TextView` are the widget classes we're referencing. `androidx.activity.enableEdgeToEdge` gives us the modern edge-to-edge UI. And `androidx.appcompat.app.AppCompatActivity` is the base class every activity extends to get compatibility with older Android versions.

Now the class declaration. I wrote `class MainActivity : AppCompatActivity(), View.OnClickListener`. The colon means "extends" for the class and "implements" for the interface. So `MainActivity` extends `AppCompatActivity` and implements `View.OnClickListener`. Implementing `View.OnClickListener` is important because it means the Activity itself becomes a valid click handler. Later we'll pass `this` to `setOnClickListener(...)` for all nine buttons, and every click will route through a single `onClick` method. That's much cleaner than writing nine separate lambdas.

Below the class declaration we have the state variables. `var PLAYER = true` is a boolean where `true` represents X's turn and `false` represents O's turn. It flips every time someone makes a move.

`var TURN_COUNT = 0` counts how many moves have been made. We use this to detect a draw — when it reaches 9, the board is full.

`var boardStatus = Array(3) { IntArray(3) }` creates a 3x3 integer matrix. The syntax `Array(3) { IntArray(3) }` says "create an array of size 3 where each element is an IntArray of size 3". I use `-1` to mean an empty cell, `1` for X, and `0` for O. Keeping this data structure separate from the actual Button views is important — win detection is much easier when you work with plain numbers instead of parsing button text.

`lateinit var board: Array<Array<Button>>` declares a 2D array of Buttons but doesn't initialize it yet. `lateinit` is Kotlin's way of saying "I promise to assign this before I use it". We can't initialize it now because the buttons don't exist until `setContentView` runs.

`lateinit var buttonReset: Button` and `lateinit var textTurn: TextView` are the reset button and the turn text, held as fields so we can access them from anywhere in the class.

Inside `onCreate`, the first line is `super.onCreate(savedInstanceState)`. This must be called first — it lets the parent AppCompatActivity do its own setup. Then `enableEdgeToEdge()` makes the app draw behind the system bars for a modern look. `setContentView(R.layout.activity_main)` inflates our XML file and attaches it to the screen. From this point on, all our views actually exist and we can look them up.

Now we call `findViewById<Button>(R.id.button1)` for each of the nine cells and for the reset button, and `findViewById(R.id.textTurn)` for the turn text. `findViewById` is the function that takes an id from R.java and returns the actual View object. The angle-bracket type tells Kotlin what type we expect back. Without `findViewById`, we have no way to interact with the XML views from Kotlin.

After we have all the button references, we assemble the 2D `board` array. `board = arrayOf(arrayOf(button1, button2, button3), arrayOf(button4, button5, button6), arrayOf(button7, button8, button9))`. This mirrors the visual grid — `board[0]` is the top row, `board[1]` the middle, `board[2]` the bottom. `board[row][col]` gives us the exact button at that position. This makes loops much cleaner later.

That's the mapping stage. Every XML view now has a matching Kotlin variable we can work with.

---

## Stage 3 — Handling Button Clicks

Right after we build the board, we call `initializeBoardStatus()`. That method resets every cell to empty. I'll cover it in detail in Stage 6.

Next, we attach the click listeners. We use two nested `for` loops:

```
for (row in board) {
    for (button in row) {
        button.setOnClickListener(this)
    }
}
```

The outer loop walks through the three rows. The inner loop walks through the three buttons in each row. On each button we call `setOnClickListener(this)`. Passing `this` works because the Activity implements `View.OnClickListener`. So we've registered the Activity as the listener for all nine cells in just six lines.

The reset button is different, so it gets its own dedicated lambda: `buttonReset.setOnClickListener { PLAYER = true; TURN_COUNT = 0; initializeBoardStatus() }`. When reset is tapped, we flip PLAYER back to X, zero out the turn counter, and re-initialize the board.

Now the actual click handler, `override fun onClick(view: View?)`. This method is called every time any of our nine cells is tapped. The parameter `view` is the specific Button that was clicked. We use a `when (view?.id)` block to decide what to do based on the button's id. The `?.` is the safe-call operator — it prevents a crash if `view` happens to be null.

For each of the nine cases, we call `upadateValue` with the correct row and column. For example, `R.id.button1 -> { upadateValue(row = 0, col = 0, player = PLAYER) }` says: if the tapped button was `button1`, apply a move at position `(0, 0)` for the current player.

The `upadateValue` function is where the move actually gets applied. Its signature is `private fun upadateValue(row: Int, col: Int, player: Boolean)`. Inside:

`val text: String = if (player) "X" else "O"` computes the symbol we're going to display — X for player true, O for player false.

`val value: Int = if (player) 1 else 0` computes the numeric code we'll store in `boardStatus` — 1 for X, 0 for O.

`board[row][col].apply { isEnabled = false; setText(text) }` uses the `apply` scope function to make two changes to the target button at once. `isEnabled = false` disables the button so the same cell can't be tapped twice. `setText(text)` writes X or O onto the button.

`boardStatus[row][col] = value` records the move in our data model. This is what the winner logic will read later.

At this point we can tap cells and they show X or O, but the turn never switches. That's next.

---

## Stage 4 — Implementing the Game Draw State

After the `when` block in `onClick`, we advance the turn.

`TURN_COUNT++` increments the move counter by one.

`PLAYER = !PLAYER` flips the current player. So if X just moved, `!true` is `false`, and now it's O's turn.

Then we update the top TextView so the player knows whose turn it is now:

```
if (PLAYER) {
    updateDisplay("Player X Turn")
} else {
    updateDisplay("Player O Turn")
}
```

`updateDisplay` is a small helper method: `private fun updateDisplay(text: String) { textTurn.text = text; ... }`. It sets the text on our turn TextView. Keeping this in a single helper means if I ever want to animate the text or change its color, I only touch one place.

After the turn indicator is updated, we check for a draw: `if (TURN_COUNT == 9) { updateDisplay("Game Draw") }`. Nine is the maximum number of moves on a 3x3 board. If we reach nine without a winner, the game is a draw and we update the display to say so.

At this stage, the game plays properly: taps place marks, turns switch, and after nine moves we get a draw message. But we still don't detect when someone actually wins.

---

## Stage 5 — Implementing the Winner Logic

The last thing we call inside `onClick` is `checkWinner()`. This is where the actual win detection lives.

`checkWinner()` looks at four kinds of winning lines: the three rows, the three columns, and the two diagonals.

For rows we loop: `for (i in 0..2)`. Inside the loop we check `if (boardStatus[i][0] != -1 && boardStatus[i][0] == boardStatus[i][1] && boardStatus[i][0] == boardStatus[i][2])`. Let me unpack this. The `!= -1` part is critical — it prevents three empty cells from falsely counting as a match. The next two comparisons check that all three cells in row `i` hold the same value. If all three checks pass, we have a winning row and we call `announceWinner(boardStatus[i][0])` and immediately `return` to exit the whole method.

For columns we do the same idea but along the vertical axis. `for (j in 0..2)`, then check `boardStatus[0][j] != -1 && boardStatus[0][j] == boardStatus[1][j] && boardStatus[0][j] == boardStatus[2][j]`. Same pattern — non-empty and all three equal — then announce and return.

For the diagonals we don't need a loop, we just check both diagonals explicitly.

The top-left to bottom-right diagonal is cells `(0,0)`, `(1,1)`, `(2,2)`. So we write `if (boardStatus[0][0] != -1 && boardStatus[0][0] == boardStatus[1][1] && boardStatus[0][0] == boardStatus[2][2])`, announce, and return.

The top-right to bottom-left diagonal is cells `(0,2)`, `(1,1)`, `(2,0)`. Same shape: `if (boardStatus[0][2] != -1 && boardStatus[0][2] == boardStatus[1][1] && boardStatus[0][2] == boardStatus[2][0])`, announce, and return.

I used `return` instead of `break` on purpose. `return` exits the entire method, so once we've announced a winner, none of the later checks can run and accidentally overwrite the message.

The `announceWinner(value: Int)` helper is a small utility. `if (value == 1) updateDisplay("Player X wins") else if (value == 0) updateDisplay("Player O wins")`. It converts the numeric marker to a human-readable message. I extracted it because otherwise every one of those eight winning-line checks would need its own X/O branching.

Now the last piece: once someone wins, we need to lock the board. That happens inside `updateDisplay`: `if (text.contains("wins")) { disableButton() }`. If the message being displayed contains the word "wins", we call `disableButton()`, which loops over every cell and sets `isEnabled = false`. The reset button stays enabled, so the player can always start a new game.

---

## Stage 6 — Finalizing All Stages

Let me tie everything together by walking through the reset and initialization logic and then the full life of a move.

`initializeBoardStatus()` is the method that puts the board back to a clean state. It runs at startup and every time reset is tapped. Inside, we have nested loops:

```
for (i in 0..2) {
    for (j in 0..2) {
        boardStatus[i][j] = -1
        board[i][j].isEnabled = true
        board[i][j].text = ""
    }
}
```

For each cell we set `boardStatus[i][j] = -1` so it's marked empty in our data. We set `board[i][j].isEnabled = true` so the button can be tapped again — this is important because after a game, all buttons were disabled by `disableButton`. And we set `board[i][j].text = ""` so any leftover X or O disappears.

Now let's walk through a full move from start to finish:

Say the app just launched. `onCreate` runs, we find all the views, we build the board array, we initialize the state to all empty, and we hook up click listeners. The TextView on screen says "Player X turn" from the XML default. `PLAYER` is `true`, `TURN_COUNT` is `0`, `boardStatus` is all `-1`.

The user taps the top-left cell, `button1`. Because we passed `this` to `setOnClickListener`, our `onClick` method is called with `button1` as the view. The `when` block matches `R.id.button1` and calls `upadateValue(0, 0, PLAYER)`. Inside `upadateValue`, since `PLAYER` is `true`, we write "X" onto the button, disable the button, and set `boardStatus[0][0] = 1`.

Back in `onClick`, we increment `TURN_COUNT` to 1. We flip `PLAYER` to `false`. Because `PLAYER` is now `false`, we call `updateDisplay("Player O Turn")`, which changes the top TextView. `TURN_COUNT` is 1, not 9, so no draw. Then we call `checkWinner`. It loops through rows, columns, diagonals — nothing matches because only one cell has a value — so it returns without doing anything.

Now O taps `button5`, the center. Same flow. `upadateValue(1, 1, false)` writes "O", disables the button, sets `boardStatus[1][1] = 0`. `TURN_COUNT` becomes 2. `PLAYER` flips back to `true`. Display now says "Player X Turn". No winner, no draw.

Suppose after a few more moves, the top row is all X — cells `(0,0)`, `(0,1)`, `(0,2)` all hold `1`. When X taps the third cell in that row, we go through the same flow, but this time when `checkWinner` runs its row loop, `boardStatus[0][0] == boardStatus[0][1] == boardStatus[0][2] == 1` and it's not `-1`, so the condition passes. We call `announceWinner(1)`, which calls `updateDisplay("Player X wins")`. Inside `updateDisplay`, we set the TextView text, and because the message contains "wins", we call `disableButton()` and lock every remaining cell. Then `checkWinner` returns.

The player looks at the screen, sees "Player X wins", the board is frozen, and the only interactive control left is the Reset button. They tap Reset. The lambda sets `PLAYER` back to `true`, sets `TURN_COUNT` back to zero, and calls `initializeBoardStatus`, which clears every cell text, re-enables every cell, and marks every cell as empty in `boardStatus`. The game is ready to be played from scratch.

That's the complete flow. Six stages: designing the layout, mapping views to Kotlin, handling clicks, tracking turns and detecting draws, detecting winners on rows, columns, and diagonals, and finally tying it all together with reset and initialization. From here you could add color highlights for the winning line, a scoreboard across games, sounds when a move is played, or even a simple computer opponent. But even the version we have now is a fully playable Tic-Tac-Toe game.

Thanks so much for watching. If this helped you understand how a small Android app comes together, please leave a like and subscribe. I'll see you in the next one.
