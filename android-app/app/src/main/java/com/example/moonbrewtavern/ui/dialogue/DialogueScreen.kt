package com.example.moonbrewtavern.ui.dialogue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme
import com.example.moonbrewtavern.ui.common.AccentBlock
import com.example.moonbrewtavern.ui.common.AmbientScenePanel
import com.example.moonbrewtavern.ui.common.GameStageLayout
import com.example.moonbrewtavern.ui.common.InfoLine
import com.example.moonbrewtavern.ui.common.SectionTitle

@Composable
fun DialogueScreen(
  scenario: GameScenario,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  GameStageLayout(
    phaseLabel = "Dialogue",
    title = scenario.visitor.name,
    subtitle = "${scenario.visitor.title} leans in over the counter and sizes up whether this tavern deserves a second visit.",
    state = scenario.initialState.copy(phase = GamePhase.Dialogue),
    modifier = modifier,
    actionLabel = "Take the order",
    actionNote = "The order is still linear, but the emotional tone is already there.",
    onAction = onContinue,
    sceneContent = {
      AccentBlock {
        Text(
          text = "\"${scenario.visitor.requestLine}\"",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = "Desired profile: ${scenario.visitor.favoriteFlavor}",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      AmbientScenePanel(
        title = "Counter conversation",
        subtitle = "She keeps one glove on, watches your shelves carefully, and speaks like someone used to bad roadside ale.",
      )
      SectionTitle("Read on the guest")
      Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
        InfoLine(label = "Mood", value = scenario.visitor.mood.name)
        InfoLine(label = "Trust", value = "Unproven")
        InfoLine(label = "Potential", value = "Recurring NPC")
      }
      Text(
        text = "This scene is where later we can add choices, tone, and relationship changes. For now it establishes the visitor clearly before the player touches ingredients.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontStyle = FontStyle.Italic,
      )
    },
    detailContent = {
      SectionTitle("Order breakdown")
      InfoLine(label = "Requested drink", value = scenario.recipe.name)
      InfoLine(label = "Need", value = "Calm focus with a warm afterglow")
      InfoLine(label = "Risk", value = "Too much heat will ruin the balance")
      SectionTitle("Why this screen matters")
      Text(
        text = "The guest request gives narrative meaning to the brewing step. Without this beat, the recipe screen is just a menu.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
  )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun DialogueScreenPreview() {
  MoonbrewTavernTheme {
    DialogueScreen(
      scenario = DefaultDataRepository().scenario,
      onContinue = {},
    )
  }
}
