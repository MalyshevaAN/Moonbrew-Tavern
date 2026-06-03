package com.example.moonbrewtavern.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.data.content.firstNightOutcome
import com.example.moonbrewtavern.data.content.firstNightScenario
import com.example.moonbrewtavern.domain.model.BrewResult
import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme
import com.example.moonbrewtavern.ui.common.AccentBlock
import com.example.moonbrewtavern.ui.common.GameStageLayout
import com.example.moonbrewtavern.ui.common.InfoLine
import com.example.moonbrewtavern.ui.common.SectionTitle

/** Result screen that summarizes the guest reaction and payout for the last brew. */
@Composable
fun ResultScreen(
  scenario: GameScenario,
  gameState: GameState,
  brewResult: BrewResult,
  onReturnToTavern: () -> Unit,
  modifier: Modifier = Modifier,
) {
  GameStageLayout(
    phaseLabel = "Result",
    title = brewResult.outcome.title,
    subtitle = brewResult.outcome.summary,
    state = gameState.copy(phase = GamePhase.Result),
    modifier = modifier,
    actionLabel = "Return to tavern",
    actionNote = "This loop is now choice-driven: the tray you built on the brewing screen directly shaped this reaction.",
    onAction = onReturnToTavern,
    sceneContent = {
      AccentBlock {
        Text(
          text = "\"${brewResult.outcome.reactionLine}\"",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text =
            if (brewResult.isExactMatch) {
              "Lyra folds the napkin map, taps the rim of the glass, and actually smiles."
            } else {
              "Lyra turns the cup in her hands for a moment before answering, measuring the tavern as much as the drink."
            },
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      SectionTitle("What you actually served")
      if (brewResult.selectedIngredients.isEmpty()) {
        Text(
          text = "No ingredients were selected.",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        brewResult.selectedIngredients.forEach { ingredient ->
          Text(
            text = "${ingredient.name} - ${ingredient.flavorNote}",
            style = MaterialTheme.typography.bodyLarge,
          )
        }
      }
    },
    detailContent = {
      SectionTitle("Outcome summary")
      Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
        InfoLine(label = "Gold gained", value = "+${brewResult.outcome.goldReward}")
        InfoLine(label = "Rep gained", value = "+${brewResult.outcome.reputationReward}")
        InfoLine(label = "Match", value = "${brewResult.matchedIngredients}/${scenario.recipe.requiredIngredients.size}")
      }
      AccentBlock(accent = MaterialTheme.colorScheme.tertiaryContainer) {
        Text(
          text =
            when {
              brewResult.isExactMatch -> "Exact recipe. The drink landed exactly on the note the guest asked for."
              brewResult.matchedIngredients >= 2 -> "Partial hit. The structure was recognizable, but one choice pulled the drink sideways."
              else -> "Loose interpretation. The tavern's voice came through, but not the guest's request."
            },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
      }
      SectionTitle("Next useful layer")
      InfoLine(label = "Mechanical", value = "Turn this into true recipe validation and scoring")
      InfoLine(label = "Narrative", value = "Let this outcome alter relationship state and future dialogue")
    },
  )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ResultScreenPreview() {
  MoonbrewTavernTheme {
    ResultScreen(
      scenario = firstNightScenario,
      gameState = firstNightScenario.initialState.copy(
        phase = GamePhase.Result,
        gold = firstNightScenario.initialState.gold + firstNightOutcome.goldReward,
        reputation = firstNightScenario.initialState.reputation + firstNightOutcome.reputationReward,
      ),
      brewResult =
        BrewResult(
          selectedIngredients = firstNightScenario.recipe.requiredIngredients,
          matchedIngredients = firstNightScenario.recipe.requiredIngredients.size,
          isExactMatch = true,
          outcome = firstNightOutcome,
        ),
      onReturnToTavern = {},
    )
  }
}
