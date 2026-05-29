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
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme
import com.example.moonbrewtavern.ui.common.AccentBlock
import com.example.moonbrewtavern.ui.common.GameStageLayout
import com.example.moonbrewtavern.ui.common.InfoLine
import com.example.moonbrewtavern.ui.common.SectionTitle

@Composable
fun ResultScreen(
  scenario: GameScenario,
  onReturnToTavern: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val finalGold = scenario.initialState.gold + scenario.outcome.goldReward
  val finalReputation = scenario.initialState.reputation + scenario.outcome.reputationReward

  GameStageLayout(
    phaseLabel = "Result",
    title = scenario.outcome.title,
    subtitle = scenario.outcome.summary,
    state = scenario.initialState.copy(phase = GamePhase.Result, gold = finalGold, reputation = finalReputation),
    modifier = modifier,
    actionLabel = "Return to tavern",
    actionNote = "For now this loops back to the start. Later it can move into upgrades, events, or the next guest.",
    onAction = onReturnToTavern,
    sceneContent = {
      AccentBlock {
        Text(
          text = "\"${scenario.outcome.reactionLine}\"",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = "Lyra folds the napkin map, taps the rim of the glass, and actually smiles.",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      SectionTitle("What changed")
      Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
        InfoLine(label = "Gold gained", value = "+${scenario.outcome.goldReward}")
        InfoLine(label = "Rep gained", value = "+${scenario.outcome.reputationReward}")
        InfoLine(label = "Outcome", value = "Successful first service")
      }
    },
    detailContent = {
      SectionTitle("Prototype takeaway")
      Text(
        text = "This closes the first emotional loop: a guest arrives uncertain, receives exactly the right drink, and leaves the tavern more alive than before.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      SectionTitle("Obvious next steps")
      InfoLine(label = "System depth", value = "Actual recipe checks and branching results")
      InfoLine(label = "Narrative depth", value = "Dialogue choices and relationship tracking")
      InfoLine(label = "Meta loop", value = "Night summary, upgrades, and saving")
    },
  )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ResultScreenPreview() {
  MoonbrewTavernTheme {
    ResultScreen(
      scenario = DefaultDataRepository().scenario,
      onReturnToTavern = {},
    )
  }
}
