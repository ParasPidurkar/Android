package com.paras.valorantstate.rounds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val ROUNDS_TO_WIN = 13 // Simplified: first to 13, no overtime.

/** Immutable snapshot of the match. To change it, make a new copy. */
data class MatchScore(val attackers: Int = 0, val defenders: Int = 0) {
    val winner: String?
        get() = when {
            attackers >= ROUNDS_TO_WIN -> "Attackers"
            defenders >= ROUNDS_TO_WIN -> "Defenders"
            else -> null
        }

    val isMatchPoint: Boolean
        get() = winner == null && (attackers == ROUNDS_TO_WIN - 1 || defenders == ROUNDS_TO_WIN - 1)
}

/**
 * Game version: the game SERVER. The match score lives here, not on your screen.
 * If your screen is rebuilt (rotation), the server still has the score.
 */
class MatchViewModel : ViewModel() {
    // Private and writable: only the ViewModel can change the score.
    private val _score = MutableStateFlow(MatchScore())

    // Public and read-only: the UI can only watch it.
    val score: StateFlow<MatchScore> = _score.asStateFlow()

    fun attackersWinRound() {
        _score.update { if (it.winner != null) it else it.copy(attackers = it.attackers + 1) }
    }

    fun defendersWinRound() {
        _score.update { if (it.winner != null) it else it.copy(defenders = it.defenders + 1) }
    }

    fun newMatch() {
        _score.value = MatchScore()
    }
}

/**
 * ROUND 10: ViewModel + StateFlow.
 */
@Composable
fun Round10ViewModel(viewModel: MatchViewModel = viewModel()) {
    // Turns the StateFlow into Compose State. Collection pauses when the app is in the background.
    val score by viewModel.score.collectAsStateWithLifecycle()

    RoundCard(
        round = 10,
        title = "Match score (ViewModel)",
        code = "val score by viewModel.score.collectAsStateWithLifecycle()",
        lesson = "Win some rounds and rotate: the score stays, because the ViewModel lives " +
            "longer than the screen. The UI only shows the score and sends events " +
            "(attackersWinRound()); the ViewModel owns the rules. It does NOT survive Android " +
            "killing the app; use SavedStateHandle for that.",
    ) {
        Text(
            "ATK ${score.attackers}  :  ${score.defenders} DEF",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        val winner = score.winner
        when {
            winner != null -> Text("🏆 $winner win the match!", color = MaterialTheme.colorScheme.secondary)
            score.isMatchPoint -> Text("MATCH POINT", color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::attackersWinRound, enabled = winner == null) { Text("ATK win round") }
            Button(onClick = viewModel::defendersWinRound, enabled = winner == null) { Text("DEF win round") }
        }
        TextButton(onClick = viewModel::newMatch) { Text("New match") }
    }
}
