package com.example.moonbrewtavern.ui.brewing

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
import com.example.moonbrewtavern.ui.common.IngredientBadge
import com.example.moonbrewtavern.ui.common.SectionTitle

@Composable
fun BrewingScreen(
  scenario: GameScenario,
  onServe: () -> Unit,
  modifier: Modifier = Modifier,
) {
  GameStageLayout(
    phaseLabel = "Brewing",
    title = scenario.recipe.name,
    subtitle = scenario.recipe.description,
    state = scenario.initialState.copy(phase = GamePhase.Brewing),
    modifier = modifier,
    actionLabel = "Serve the drink",
    actionNote = "Later this screen can become the real mini-game. Right now it teaches the shape of the action.",
    onAction = onServe,
    sceneContent = {
      SectionTitle("Ingredient tray")
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        scenario.recipe.requiredIngredients.forEach { ingredient ->
          IngredientBadge(name = ingredient.name, note = ingredient.flavorNote)
        }
      }
      AccentBlock {
        Text(
          text = "Brewer's note",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = scenario.brewingHint,
          style = MaterialTheme.typography.bodyLarge,
        )
      }
    },
    detailContent = {
      SectionTitle("Preparation intent")
      InfoLine(label = "Base impression", value = "Cool and clear")
      InfoLine(label = "Finish", value = "Warm and brave")
      InfoLine(label = "Foam", value = "Soft silver shimmer")
      SectionTitle("Current simplification")
      Text(
        text = "The recipe is fixed, the ingredients are fixed, and success is fixed. That is okay for this PR because the goal is a believable flow, not system depth yet.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
  )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun BrewingScreenPreview() {
  MoonbrewTavernTheme {
    BrewingScreen(
      scenario = DefaultDataRepository().scenario,
      onServe = {},
    )
  }
}
