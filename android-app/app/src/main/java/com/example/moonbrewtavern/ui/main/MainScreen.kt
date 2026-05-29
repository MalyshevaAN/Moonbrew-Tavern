package com.example.moonbrewtavern.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.moonbrewtavern.Dialogue
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme
import com.example.moonbrewtavern.ui.common.AccentBlock
import com.example.moonbrewtavern.ui.common.GameStageLayout
import com.example.moonbrewtavern.ui.common.InfoLine
import com.example.moonbrewtavern.ui.common.SectionTitle

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(DefaultDataRepository()) },
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  when (state) {
    MainScreenUiState.Loading -> LoadingMainScreen(modifier = modifier)
    is MainScreenUiState.Success ->
      MainScreen(
        scenario = (state as MainScreenUiState.Success).scenario,
        onStartDialogue = { onItemClick(Dialogue) },
        modifier = modifier,
      )
    is MainScreenUiState.Error -> ErrorMainScreen(throwableMessage = (state as MainScreenUiState.Error).throwable.message, modifier = modifier)
  }
}

@Composable
internal fun MainScreen(
  scenario: GameScenario,
  onStartDialogue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  GameStageLayout(
    phaseLabel = "Tavern Floor",
    title = scenario.tavern.name,
    subtitle = "Night ${scenario.initialState.day}. The lamps are lit, the storm is soft, and your first real guest is already watching the bar.",
    state = scenario.initialState,
    modifier = modifier,
    actionLabel = "Greet ${scenario.visitor.name}",
    actionNote = "This first slice is hardcoded on purpose so we can shape the flow before building systems around it.",
    onAction = onStartDialogue,
    sceneContent = {
      SectionTitle("Tonight's atmosphere")
      Text(
        text = scenario.tavern.atmosphere,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      AccentBlock {
        Text(
          text = scenario.visitor.name,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = scenario.visitor.title,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "\"${scenario.visitor.openingLine}\"",
          style = MaterialTheme.typography.titleMedium,
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
        InfoLine(label = "Mood", value = scenario.visitor.mood.name)
        InfoLine(label = "Focus", value = scenario.visitor.favoriteFlavor)
      }
    },
    detailContent = {
      SectionTitle("What this PR already proves")
      InfoLine(label = "Playable loop", value = "Tavern -> dialogue -> brewing -> result")
      InfoLine(label = "Featured drink", value = scenario.recipe.name)
      InfoLine(label = "Design goal", value = "One guest, one order, one clear emotional payoff")
      Spacer(Modifier.height(8.dp))
      SectionTitle("Service notes")
      Text(
        text = "We are aiming for a wide, scene-first flow. The visitor stays emotionally present while the UI guides the player through one service sequence.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
  )
}

@Composable
private fun LoadingMainScreen(modifier: Modifier = Modifier) {
  GameStageLayout(
    phaseLabel = "Preparing",
    title = "Opening the tavern",
    subtitle = "Lighting lanterns, laying out cups, and warming up the first playable loop.",
    state = DefaultDataRepository().scenario.initialState,
    modifier = modifier,
    sceneContent = {
      CircularProgressIndicator()
      Text(
        text = "Building the first night...",
        style = MaterialTheme.typography.bodyLarge,
      )
    },
    detailContent = {
      SectionTitle("Status")
      Text(
        text = "Loading hardcoded scenario content for the first guest and first drink.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
  )
}

@Composable
private fun ErrorMainScreen(throwableMessage: String?, modifier: Modifier = Modifier) {
  GameStageLayout(
    phaseLabel = "Error",
    title = "The tavern lights flickered out",
    subtitle = "Something went wrong while preparing the first night.",
    state = DefaultDataRepository().scenario.initialState,
    modifier = modifier,
    sceneContent = {
      SectionTitle("Error details")
      Text(
        text = throwableMessage ?: "Unknown issue",
        style = MaterialTheme.typography.bodyLarge,
      )
    },
    detailContent = {
      SectionTitle("Next step")
      Text(
        text = "This should stay simple for now: one repository, one scenario, and one route through the game.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
  )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun MainScreenLandscapePreview() {
  MoonbrewTavernTheme {
    MainScreen(
      scenario = DefaultDataRepository().scenario,
      onStartDialogue = {},
    )
  }
}
