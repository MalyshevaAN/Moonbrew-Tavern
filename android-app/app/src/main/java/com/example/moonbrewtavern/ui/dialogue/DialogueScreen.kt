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
    // Верхняя плашка ночи/времени.
    // Кнопка меню справа сверху.
    Image(
      painter = painterResource(R.drawable.dialogue_status_bar),
      contentDescription = null,
      modifier = Modifier.width(246.dp).align(Alignment.TopEnd).offset(x = (-58).dp, y = 6.dp),
      contentScale = ContentScale.FillWidth,
    )
    Image(
      painter = painterResource(R.drawable.dialogue_menu_button),
      contentDescription = null,
      modifier = Modifier.width(46.dp).align(Alignment.TopEnd).offset(x = (-6).dp, y = 8.dp),
      contentScale = ContentScale.FillWidth,
    )

    // Само облачко над персонажами.
    Image(
      painter = painterResource(R.drawable.dialogue_bubble),
      contentDescription = null,
      modifier = Modifier.width(270.dp).align(Alignment.TopCenter).offset(x = (-76).dp, y = 1.dp),
      contentScale = ContentScale.FillWidth,
    )
    // Текст внутри облачка.
    BubbleText(
      text = scenario.visitor.openingLine,
      modifier = Modifier.align(Alignment.TopCenter).offset(x = (-94).dp, y = 18.dp),
    )

    // Центральная сцена с Лирой и барменом.
    CharacterStage(modifier = Modifier.align(Alignment.Center).offset(x = (-8).dp, y = 34.dp))

    // Правая карточка персонажа.
    ProfilePanel(
      modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-6).dp, y = 18.dp),
    )

    // Нижний блок с 4 вариантами ответа.
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
    modifier = modifier.width(200.dp), // Ширина текста в облачке.
    color = Color(0xFF1F1A17),
    style = MaterialTheme.typography.titleMedium.copy(
      fontSize = 11.sp, // Размер текста реплики.
      lineHeight = 14.sp, // Межстрочный интервал.
    ),
    fontWeight = FontWeight.Medium,
    maxLines = 3,
    overflow = TextOverflow.Ellipsis,
  )
}

@Composable
private fun CharacterStage(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.width(446.dp).height(262.dp), // Общая зона двух персонажей.
  ) {
    Image(
      painter = painterResource(R.drawable.dialogue_visitor_lyra),
      contentDescription = null,
      modifier = Modifier.width(154.dp).align(Alignment.BottomStart).offset(x = 14.dp), // Лира слева.
      contentScale = ContentScale.FillWidth,
    )
    Image(
      painter = painterResource(R.drawable.dialogue_bartender),
      contentDescription = null,
      modifier = Modifier.width(164.dp).align(Alignment.BottomEnd).offset(x = (-44).dp, y = -24.dp), // Бармен справа.
      contentScale = ContentScale.FillWidth,
    )
  }
}

@Composable
private fun ProfilePanel(
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.width(248.dp)) { // Размер правой карточки.
    Image(
      painter = painterResource(R.drawable.dialogue_profile_card),
      contentDescription = null,
      modifier = Modifier.fillMaxWidth(),
      contentScale = ContentScale.FillWidth,
    )
  }
}

@Composable
private fun DialogueChoices(
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.width(448.dp), // Общая ширина блока вопросов.
    verticalArrangement = Arrangement.spacedBy(5.dp), // Расстояние между верхним и нижним рядом.
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { // Расстояние между левой и правой кнопкой.
      ChoiceCard(text = "1. What brought you here?", modifier = Modifier.weight(1f), onClick = onContinue)
      ChoiceCard(text = "2. Tell me about yourself.", modifier = Modifier.weight(1f), onClick = onContinue)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
    modifier = modifier.height(46.dp).clickable(onClick = onClick), // Высота одной кнопки-вопроса.
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
      modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 18.dp, vertical = 8.dp), // Отступы текста внутри кнопки.
      color = Color(0xFFF1E4DB),
      style = MaterialTheme.typography.titleSmall.copy(
        fontSize = 10.sp, // Размер текста варианта.
        lineHeight = 10.sp, // Межстрочный интервал варианта.
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
      .padding(horizontal = 18.dp, vertical = 10.dp) // Маска поверх текста, вшитого в ассет.
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
