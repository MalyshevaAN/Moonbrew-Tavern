package com.example.moonbrewtavern.ui.dialogue

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.data.content.ContentCatalog
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme

/** Dialogue scene shown before the player opens the recipe book for a guest. */
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
    // Top status badge and menu affordance.
    Image(
      painter = painterResource(R.drawable.dialogue_status_bar),
      contentDescription = null,
      modifier = Modifier.width(130.dp).align(Alignment.TopEnd).offset(x = (-58).dp, y = 6.dp),
      contentScale = ContentScale.FillWidth,
    )
    Image(
      painter = painterResource(R.drawable.dialogue_menu_button),
      contentDescription = null,
      modifier = Modifier.width(46.dp).align(Alignment.TopEnd).offset(x = (-6).dp, y = 8.dp),
      contentScale = ContentScale.FillWidth,
    )

    // Dialogue bubble above the characters.
    Image(
      painter = painterResource(R.drawable.dialogue_bubble),
      contentDescription = null,
      modifier = Modifier.width(270.dp).align(Alignment.TopCenter).offset(x = (-76).dp, y = 1.dp),
      contentScale = ContentScale.FillWidth,
    )
    // Active line shown inside the bubble.
    BubbleText(
      text = scenario.visitor.openingLine,
      modifier = Modifier.align(Alignment.TopCenter).offset(x = (-94).dp, y = 18.dp),
    )

    // Main character stage with the guest and bartender.
    CharacterStage(
      visitorId = scenario.visitor.id,
      modifier = Modifier.align(Alignment.Center).offset(x = (-8).dp, y = 34.dp),
    )

    // Profile card for the current guest.
    ProfilePanel(
      visitorName = scenario.visitor.name,
      visitorTitle = scenario.visitor.title,
      requestLine = scenario.visitor.requestLine,
      modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-6).dp, y = 18.dp),
    )

    // Bottom response grid that advances to brewing.
    DialogueChoices(
      onContinue = onContinue,
      modifier = Modifier.align(Alignment.BottomStart).offset(x = 14.dp, y = 10.dp),
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
    modifier = modifier.width(200.dp),
    color = Color(0xFF1F1A17),
    style = MaterialTheme.typography.titleMedium.copy(
      fontSize = 11.sp,
      lineHeight = 14.sp,
    ),
    fontWeight = FontWeight.Medium,
    maxLines = 3,
    overflow = TextOverflow.Ellipsis,
  )
}

@Composable
private fun CharacterStage(
  visitorId: String,
  modifier: Modifier = Modifier,
) {
  val visitorPortraitRes = ContentCatalog.visitorDefinitionsById[visitorId]?.assets?.dialoguePortraitRes ?: R.drawable.dialogue_visitor_lyra
  Box(
    modifier = modifier.width(446.dp).height(262.dp),
  ) {
    Image(
      painter = painterResource(visitorPortraitRes),
      contentDescription = null,
      modifier = Modifier.width(154.dp).align(Alignment.BottomStart).offset(x = 14.dp),
      contentScale = ContentScale.FillWidth,
    )
    Image(
      painter = painterResource(R.drawable.dialogue_bartender),
      contentDescription = null,
      modifier = Modifier.width(164.dp).align(Alignment.BottomEnd).offset(x = (-44).dp, y = -24.dp),
      contentScale = ContentScale.FillWidth,
    )
  }
}

@Composable
private fun ProfilePanel(
  visitorName: String,
  visitorTitle: String,
  requestLine: String,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.width(248.dp)) {
    Image(
      painter = painterResource(R.drawable.dialogue_profile_card),
      contentDescription = null,
      modifier = Modifier.fillMaxWidth(),
      contentScale = ContentScale.FillWidth,
    )
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 22.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(visitorName, color = Color(0xFFF6E8D7), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Text(visitorTitle, color = Color(0xFFDABEA1), style = MaterialTheme.typography.bodySmall)
      Text(requestLine, color = Color(0xFFE9D7C2), style = MaterialTheme.typography.bodySmall, maxLines = 5, overflow = TextOverflow.Ellipsis)
    }
  }
}

@Composable
private fun DialogueChoices(
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.width(448.dp),
    verticalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      ChoiceCard(text = "1. Что привело тебя сюда?", modifier = Modifier.weight(1f), onClick = onContinue)
      ChoiceCard(text = "2. Расскажи о себе.", modifier = Modifier.weight(1f), onClick = onContinue)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      ChoiceCard(text = "3. Хочешь чего-нибудь особенного?", modifier = Modifier.weight(1f), onClick = onContinue)
      ChoiceCard(text = "4. Приятного вечера.", modifier = Modifier.weight(1f), onClick = onContinue)
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
    modifier = modifier.height(46.dp).clickable(onClick = onClick),
  ) {
    Image(
      painter = painterResource(R.drawable.dialogue_choice_panel),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.FillBounds,
    )
    ChoiceMask()
    Text(
      text = text,
      modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 18.dp, vertical = 8.dp),
      color = Color(0xFFF1E4DB),
      style = MaterialTheme.typography.titleSmall.copy(
        fontSize = 10.sp,
        lineHeight = 10.sp,
      ),
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Start,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun BoxScope.ChoiceMask() {
  Box(
    modifier = Modifier
      .align(Alignment.Center)
      .fillMaxSize()
      .padding(horizontal = 18.dp, vertical = 10.dp)
      .background(
        color = Color(0xFF2A2030).copy(alpha = 0.94f),
        shape = RoundedCornerShape(10.dp),
      ),
  )
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
