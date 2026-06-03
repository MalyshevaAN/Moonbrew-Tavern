package com.example.moonbrewtavern.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.TavernRoom
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme
import com.example.moonbrewtavern.ui.common.GameStageLayout
import com.example.moonbrewtavern.ui.common.SectionTitle

/** Legacy scenario-based landing screen kept for previews and view-model tests. */
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
        onStartDialogue = { onItemClick(TavernRoom) },
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
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(Color(0xFFBBD8EF)),
  ) {
    Image(
      painter = painterResource(R.drawable.tavern_bg),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(Color(0x11000000), Color(0x00000000), Color(0x22000000)),
            ),
          ),
    )

    TavernSceneUi(
      scenario = scenario,
      onStartDialogue = onStartDialogue,
      modifier = Modifier.fillMaxSize().padding(10.dp),
    )
  }
}

@Composable
private fun TavernSceneUi(
  scenario: GameScenario,
  onStartDialogue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    Image(
      painter = painterResource(R.drawable.ui_sign),
      contentDescription = null,
      modifier = Modifier.width(260.dp).align(Alignment.TopStart),
      contentScale = ContentScale.FillWidth,
    )

    Image(
      painter = painterResource(R.drawable.ui_day_panel),
      contentDescription = null,
      modifier = Modifier.width(118.dp).align(Alignment.TopStart).offset(x = 6.dp, y = 84.dp),
      contentScale = ContentScale.FillWidth,
    )

    Image(
      painter = painterResource(R.drawable.ui_resource_bar),
      contentDescription = null,
      modifier = Modifier.width(420.dp).align(Alignment.TopEnd).offset(x = (-74).dp, y = 6.dp),
      contentScale = ContentScale.FillWidth,
    )

    Image(
      painter = painterResource(R.drawable.ui_settings),
      contentDescription = null,
      modifier = Modifier.size(52.dp).align(Alignment.TopEnd).offset(y = 8.dp),
      contentScale = ContentScale.FillBounds,
    )

    Image(
      painter = painterResource(R.drawable.bartender_bar),
      contentDescription = null,
      modifier = Modifier.width(200.dp).align(Alignment.BottomEnd).offset(x = (-10).dp, y = (-40).dp),
      contentScale = ContentScale.FillWidth,
    )

    GuestQueue(
      onStartDialogue = onStartDialogue,
      modifier = Modifier.align(Alignment.BottomStart).offset(x = 16.dp, y = (-22).dp),
    )
  }
}

@Composable
private fun GuestQueue(
  onStartDialogue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxWidth(0.5f).height(150.dp)) {
    QueueSprite(R.drawable.npc_hood, "hooded guest", Modifier.align(Alignment.BottomStart).offset(x = 8.dp))
    QueueSprite(R.drawable.npc_witch, "witch", Modifier.align(Alignment.BottomStart).offset(x = 84.dp))
    QueueSprite(R.drawable.npc_elder, "elder", Modifier.align(Alignment.BottomStart).offset(x = 176.dp))
    QueueSprite(
      drawable = R.drawable.npc_beard,
      label = "next guest",
      modifier = Modifier.align(Alignment.BottomStart).offset(x = 252.dp),
      highlighted = true,
      onClick = onStartDialogue,
    )
    QueueSprite(R.drawable.npc_ghost, "ghost", Modifier.align(Alignment.BottomStart).offset(x = 342.dp, y = (-2).dp))
  }
}

@Composable
private fun QueueSprite(
  drawable: Int,
  label: String,
  modifier: Modifier = Modifier,
  highlighted: Boolean = false,
  onClick: (() -> Unit)? = null,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier =
        Modifier
          .clip(RoundedCornerShape(18.dp))
          .then(
            if (highlighted) {
              Modifier.border(3.dp, Color(0xFFF0D28B), RoundedCornerShape(18.dp))
            } else {
              Modifier
            },
          )
          .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
      Image(
        painter = painterResource(drawable),
        contentDescription = label,
        modifier = Modifier.height(if (highlighted) 118.dp else 106.dp),
        contentScale = ContentScale.FillHeight,
      )
    }
    if (highlighted) {
      Box(
        modifier =
          Modifier
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xCC2C241E))
            .padding(horizontal = 10.dp, vertical = 4.dp),
      ) {
        Text(
          text = "Next",
          color = Color(0xFFF1DEAE),
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
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

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun MainScreenLandscapePreview() {
  MoonbrewTavernTheme {
    MainScreen(
      scenario = DefaultDataRepository().scenario,
      onStartDialogue = {},
    )
  }
}
