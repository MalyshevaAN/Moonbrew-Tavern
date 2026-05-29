package com.example.moonbrewtavern.ui.dialogue

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme

@Composable
fun DialogueScreen(
  scenario: GameScenario,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
  ) {
    Image(
      painter = painterResource(R.drawable.dialogue_background),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    DialogueSceneOverlay(
      scenario = scenario,
      onContinue = onContinue,
      modifier = Modifier.fillMaxSize().padding(18.dp),
    )
  }
}

@Composable
private fun DialogueSceneOverlay(
  scenario: GameScenario,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    Image(
      painter = painterResource(R.drawable.dialogue_status_bar),
      contentDescription = null,
      modifier = Modifier.width(260.dp).align(Alignment.TopEnd).offset(x = (-82).dp, y = 2.dp),
      contentScale = ContentScale.FillWidth,
    )
    Image(
      painter = painterResource(R.drawable.dialogue_menu_button),
      contentDescription = null,
      modifier = Modifier.width(52.dp).align(Alignment.TopEnd).offset(y = 4.dp),
      contentScale = ContentScale.FillWidth,
    )

    Image(
      painter = painterResource(R.drawable.dialogue_bubble),
      contentDescription = null,
      modifier = Modifier.width(470.dp).align(Alignment.TopCenter).offset(y = 62.dp),
      contentScale = ContentScale.FillWidth,
    )
    BubbleText(
      text = scenario.visitor.openingLine,
      modifier = Modifier.align(Alignment.TopCenter).offset(x = (-12).dp, y = 94.dp),
    )

    CharacterStage(modifier = Modifier.align(Alignment.CenterStart).offset(x = 118.dp, y = 40.dp))

    ProfilePanel(
      scenario = scenario,
      modifier = Modifier.align(Alignment.CenterEnd).offset(y = 40.dp),
    )

    DialogueChoices(
      onContinue = onContinue,
      modifier = Modifier.align(Alignment.BottomStart).offset(x = 38.dp, y = (-22).dp),
    )
  }
}

@Composable
private fun BubbleText(
  text: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = text,
    modifier = modifier.width(380.dp),
    color = Color(0xFF1F1A17),
    style = MaterialTheme.typography.headlineSmall,
    fontWeight = FontWeight.Medium,
  )
}

@Composable
private fun CharacterStage(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.width(520.dp).height(320.dp),
  ) {
    Image(
      painter = painterResource(R.drawable.dialogue_visitor_lyra),
      contentDescription = null,
      modifier = Modifier.width(210.dp).align(Alignment.BottomStart),
      contentScale = ContentScale.FillWidth,
    )
    Image(
      painter = painterResource(R.drawable.dialogue_bartender),
      contentDescription = null,
      modifier = Modifier.width(200.dp).align(Alignment.BottomEnd).offset(x = (-24).dp, y = 2.dp),
      contentScale = ContentScale.FillWidth,
    )
  }
}

@Composable
private fun ProfilePanel(
  scenario: GameScenario,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.width(300.dp)) {
    Image(
      painter = painterResource(R.drawable.dialogue_profile_card),
      contentDescription = null,
      modifier = Modifier.fillMaxWidth(),
      contentScale = ContentScale.FillWidth,
    )

    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 26.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = scenario.visitor.name,
          color = Color.White,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = scenario.visitor.title,
          color = Color(0xFFD9B36C),
          style = MaterialTheme.typography.titleMedium,
        )
      }

      StatLine(label = "Affinity", value = "35/100", accent = Color(0xFF89B26E))
      StatLine(label = "Favorite drink", value = "Herbal blend", accent = Color(0xFF89B26E))
      StatLine(label = "Prefers", value = "Light, sweet", accent = Color(0xFFD8C16E))
      StatLine(label = "Dislikes", value = "Bitterness", accent = Color(0xFFD56B6B))
    }
  }
}

@Composable
private fun StatLine(
  label: String,
  value: String,
  accent: Color,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = label,
      color = accent,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
    )
    Text(
      text = value,
      color = Color(0xFFE8DED1),
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}

@Composable
private fun DialogueChoices(
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.width(700.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      ChoiceCard(text = "1. What brought you here?", modifier = Modifier.weight(1f), onClick = onContinue)
      ChoiceCard(text = "2. Tell me about yourself.", modifier = Modifier.weight(1f), onClick = onContinue)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      ChoiceCard(text = "3. Want something special?", modifier = Modifier.weight(1f), onClick = onContinue)
      ChoiceCard(text = "4. Enjoy the evening.", modifier = Modifier.weight(1f), onClick = onContinue)
    }
  }
}

@Composable
private fun ChoiceCard(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.clickable(onClick = onClick),
  ) {
    Image(
      painter = painterResource(R.drawable.dialogue_choice_panel),
      contentDescription = null,
      modifier = Modifier.fillMaxWidth(),
      contentScale = ContentScale.FillWidth,
    )
    Text(
      text = text,
      modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 26.dp, vertical = 20.dp),
      color = Color(0xFFF1E4DB),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Start,
    )
  }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun DialogueScreenPreview() {
  MoonbrewTavernTheme {
    DialogueScreen(
      scenario = DefaultDataRepository().scenario,
      onContinue = {},
    )
  }
}
