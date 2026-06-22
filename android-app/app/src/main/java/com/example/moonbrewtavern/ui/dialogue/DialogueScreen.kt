package com.example.moonbrewtavern.ui.dialogue

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.moonbrewtavern.ui.common.rememberFloatingOffset

private data class DialogueChoiceEntry(
  val prompt: String,
  val response: String,
)

/** Dialogue scene shown before the player opens the recipe book for a guest. */
@Composable
fun DialogueScreen(
  scenario: GameScenario,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val choices =
    remember(scenario.visitor.id) {
      listOf(
        DialogueChoiceEntry(
          prompt = "Что привело тебя сюда?",
          response = "${scenario.visitor.name} опускает плечи и кивает на дорогу за окнами. \"Долгий тракт. Нужен вечер потише и напиток почище мыслей.\"",
        ),
        DialogueChoiceEntry(
          prompt = "Расскажи о себе.",
          response = "\"Я ${scenario.visitor.title.lowercase()}. Слишком долго живу в пути, чтобы не ценить места, где умеют слушать.\"",
        ),
        DialogueChoiceEntry(
          prompt = "Что тебе сейчас нужно?",
          response = scenario.visitor.requestLine,
        ),
        DialogueChoiceEntry(
          prompt = "Тебе здесь рады.",
          response = "${scenario.visitor.name} едва заметно улыбается. \"Тогда посмотрим, умеет ли эта таверна запоминаться вкусом, а не только теплом.\"",
        ),
      )
    }
  var selectedChoiceIndex by rememberSaveable(scenario.visitor.id) { mutableIntStateOf(-1) }
  val bubbleText = choices.getOrNull(selectedChoiceIndex)?.response ?: scenario.visitor.openingLine

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
      bubbleText = bubbleText,
      choices = choices,
      selectedChoiceIndex = selectedChoiceIndex,
      onChoiceSelected = { selectedChoiceIndex = it },
      onContinue = onContinue,
      modifier = Modifier.fillMaxSize().padding(18.dp),
    )
  }
}

@Composable
private fun DialogueSceneOverlay(
  scenario: GameScenario,
  bubbleText: String,
  choices: List<DialogueChoiceEntry>,
  selectedChoiceIndex: Int,
  onChoiceSelected: (Int) -> Unit,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    // Compact status controls built natively so no export background can leak through.
    Surface(
      modifier = Modifier.align(Alignment.TopEnd).offset(x = (-58).dp, y = 6.dp),
      shape = RoundedCornerShape(12.dp),
      color = Color(0xE8332926),
    ) {
      Text(
        text = "Ночь ${scenario.initialState.day}  ☾",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        color = Color(0xFFF2E1C6),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
      )
    }
    Surface(
      modifier = Modifier.size(44.dp).align(Alignment.TopEnd).offset(x = (-4).dp, y = 6.dp),
      shape = RoundedCornerShape(12.dp),
      color = Color(0xE8332926),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text("≡", color = Color(0xFFF0C88C), style = MaterialTheme.typography.titleLarge)
      }
    }

    // Dialogue bubble above the characters.
    Image(
      painter = painterResource(R.drawable.dialogue_bubble),
      contentDescription = null,
      modifier = Modifier.width(270.dp).align(Alignment.TopCenter).offset(x = (-76).dp, y = 1.dp),
      contentScale = ContentScale.FillWidth,
    )
    // Active line shown inside the bubble.
    BubbleText(
      text = bubbleText,
      modifier = Modifier.align(Alignment.TopCenter).offset(x = (-94).dp, y = 18.dp),
    )

    // Main character stage with the guest and bartender.
    CharacterStage(
      visitorId = scenario.visitor.id,
      modifier = Modifier.align(Alignment.Center).offset(x = (-8).dp, y = (-24).dp),
    )

    // Profile card for the current guest.
    ProfilePanel(
      visitorId = scenario.visitor.id,
      visitorName = scenario.visitor.name,
      visitorTitle = scenario.visitor.title,
      requestLine = scenario.visitor.requestLine,
      modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-6).dp, y = 18.dp),
    )

    // Bottom response grid that advances to brewing.
    DialogueChoices(
      choices = choices,
      selectedChoiceIndex = selectedChoiceIndex,
      onChoiceSelected = onChoiceSelected,
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
  val visitorFloat = rememberFloatingOffset(amplitude = 5f, durationMs = 2300)
  val bartenderFloat = rememberFloatingOffset(amplitude = 4f, durationMs = 3100)
  Box(
    modifier = modifier.width(446.dp).height(236.dp),
  ) {
    Image(
      painter = painterResource(visitorPortraitRes),
      contentDescription = null,
      modifier = Modifier.width(146.dp).align(Alignment.BottomStart).offset(x = 20.dp, y = visitorFloat.dp),
      contentScale = ContentScale.Fit,
    )
    Image(
      painter = painterResource(R.drawable.dialogue_bartender),
      contentDescription = null,
      modifier = Modifier.width(164.dp).align(Alignment.BottomEnd).offset(x = (-44).dp, y = (-24).dp + bartenderFloat.dp),
      contentScale = ContentScale.FillWidth,
    )
  }
}

@Composable
private fun ProfilePanel(
  visitorId: String,
  visitorName: String,
  visitorTitle: String,
  requestLine: String,
  modifier: Modifier = Modifier,
) {
  val portraitRes = ContentCatalog.visitorDefinitionsById[visitorId]?.assets?.dialoguePortraitRes ?: R.drawable.dialogue_visitor_lyra
  Surface(
    modifier = modifier.width(270.dp),
    shape = RoundedCornerShape(22.dp),
    color = Color(0xD9261C19),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier
              .size(72.dp)
              .background(Color(0xFF3C2A25), RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center,
        ) {
          Image(
            painter = painterResource(portraitRes),
            contentDescription = visitorName,
            modifier = Modifier.size(62.dp),
            contentScale = ContentScale.Fit,
          )
        }
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(visitorName, color = Color(0xFFF6E8D7), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(visitorTitle, color = Color(0xFFDABEA1), style = MaterialTheme.typography.bodySmall)
        }
      }
      Text(
        text = "Заказ",
        color = Color(0xFFF0C88C),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
      )
      Text(requestLine, color = Color(0xFFE9D7C2), style = MaterialTheme.typography.bodySmall, maxLines = 5, overflow = TextOverflow.Ellipsis)
    }
  }
}

@Composable
private fun DialogueChoices(
  choices: List<DialogueChoiceEntry>,
  selectedChoiceIndex: Int,
  onChoiceSelected: (Int) -> Unit,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.width(448.dp),
    verticalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      ChoiceCard(
        text = "1. ${choices[0].prompt}",
        selected = selectedChoiceIndex == 0,
        modifier = Modifier.weight(1f),
        onClick = { onChoiceSelected(0) }
      )
      ChoiceCard(
        text = "2. ${choices[1].prompt}",
        selected = selectedChoiceIndex == 1,
        modifier = Modifier.weight(1f),
        onClick = { onChoiceSelected(1) }
      )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      ChoiceCard(
        text = "3. ${choices[2].prompt}",
        selected = selectedChoiceIndex == 2,
        modifier = Modifier.weight(1f),
        onClick = { onChoiceSelected(2) }
      )
      ChoiceCard(
        text = "4. ${choices[3].prompt}",
        selected = selectedChoiceIndex == 3,
        modifier = Modifier.weight(1f),
        onClick = { onChoiceSelected(3) }
      )
    }
    ChoiceCard(
      text = if (selectedChoiceIndex >= 0) "Открыть книгу рецептов" else "Сначала выбери реплику",
      selected = selectedChoiceIndex >= 0,
      modifier = Modifier.fillMaxWidth(),
      onClick = {
        if (selectedChoiceIndex >= 0) {
          onContinue()
        }
      }
    )
  }
}

@Composable
private fun ChoiceCard(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.height(46.dp).clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    color = if (selected) Color(0xFF4A3548) else Color(0xF02A2030),
    border =
      androidx.compose.foundation.BorderStroke(
        width = if (selected) 2.dp else 1.dp,
        color = if (selected) Color(0xFFD5A86E) else Color(0xFF76536E),
      ),
  ) {
    Text(
      text = text,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
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
