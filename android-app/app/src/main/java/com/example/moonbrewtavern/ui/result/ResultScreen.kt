package com.example.moonbrewtavern.ui.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.data.content.ContentCatalog
import com.example.moonbrewtavern.data.content.firstNightOutcome
import com.example.moonbrewtavern.data.content.firstNightScenario
import com.example.moonbrewtavern.domain.model.BrewResult
import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme

/** Result screen that summarizes the guest reaction and payout for the last brew. */
@Composable
fun ResultScreen(
  scenario: GameScenario,
  gameState: GameState,
  brewResult: BrewResult,
  onReturnToTavern: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val portraitRes =
    ContentCatalog.visitorDefinitionsById[scenario.visitor.id]
      ?.assets
      ?.resultPortraitRes
      ?: R.drawable.portrait_lyra
  Box(modifier = modifier.fillMaxSize()) {
    Image(
      painter = painterResource(R.drawable.tavern_room_background),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.horizontalGradient(
              listOf(Color(0xE8150E0C), Color(0xB51B110E), Color(0xE8150E0C)),
            ),
          ),
    )

    Row(
      modifier = Modifier.fillMaxSize().padding(26.dp),
      horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      Surface(
        modifier = Modifier.weight(1.15f).fillMaxHeight(),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xEE251915),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF74513B)),
      ) {
        Column(
          modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Text(
            text = "Итог заказа",
            color = Color(0xFFF0C88C),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = localizedTitle(brewResult),
            color = Color(0xFFF5E6D3),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              modifier = Modifier.size(168.dp),
              shape = RoundedCornerShape(24.dp),
              color = Color(0xFF382620),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6C4B38)),
            ) {
              Image(
                painter = painterResource(portraitRes),
                contentDescription = scenario.visitor.name,
                modifier = Modifier.fillMaxSize().padding(10.dp),
                contentScale = ContentScale.Fit,
              )
            }
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                text = scenario.visitor.name,
                color = Color(0xFFF5E6D3),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = localizedSummary(scenario, brewResult),
                color = Color(0xFFD9C3AC),
                style = MaterialTheme.typography.bodyLarge,
              )
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF3B2A23),
              ) {
                Text(
                  text = "«${localizedReaction(brewResult)}»",
                  modifier = Modifier.padding(14.dp),
                  color = Color(0xFFF0D39D),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium,
                )
              }
            }
          }

          Text(
            text =
              if (brewResult.selectedIngredients.isEmpty()) {
                "Напиток подан без выбранных ингредиентов."
              } else {
                "В напитке: ${brewResult.selectedIngredients.joinToString { it.name }}"
              },
            color = Color(0xFFCDB49A),
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }

      Column(
        modifier = Modifier.width(310.dp).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          color = Color(0xEE2A1D18),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF74513B)),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Text(
              text = "Ночь ${gameState.day}",
              color = Color(0xFFF5E6D3),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            RewardLine("Золото", "+${brewResult.outcome.goldReward}")
            RewardLine(
              "Репутация",
              if (brewResult.outcome.reputationReward >= 0) {
                "+${brewResult.outcome.reputationReward}"
              } else {
                brewResult.outcome.reputationReward.toString()
              },
            )
            RewardLine(
              "Совпадение",
              "${brewResult.matchedIngredients}/${scenario.recipe.requiredIngredients.size}",
            )
          }
        }

        Surface(
          modifier = Modifier.fillMaxWidth().weight(1f),
          shape = RoundedCornerShape(22.dp),
          color = Color(0xE633241E),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF74513B)),
        ) {
          Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              text = "Оценка",
              color = Color(0xFFF0C88C),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text =
                when {
                  brewResult.isExactMatch -> "Точное попадание. Гость получил именно тот вкус, который просил."
                  brewResult.matchedIngredients >= 2 -> "Основа напитка удачная, но один ингредиент увёл вкус в сторону."
                  else -> "Заказ раскрыт слабо, но гость заметил старание."
                },
              color = Color(0xFFD9C3AC),
              style = MaterialTheme.typography.bodyLarge,
            )
          }
        }

        Button(
          onClick = onReturnToTavern,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "Вернуться в зал",
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}

@Composable
private fun RewardLine(
  label: String,
  value: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, color = Color(0xFFD9C3AC), style = MaterialTheme.typography.bodyLarge)
    Text(value, color = Color(0xFFF0D39D), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
  }
}

private fun localizedTitle(result: BrewResult): String =
  when {
    result.isExactMatch -> result.outcome.title
    result.matchedIngredients >= 2 -> "Почти идеальный напиток"
    else -> "Первый глоток вышел неровным"
  }

private fun localizedSummary(
  scenario: GameScenario,
  result: BrewResult,
): String =
  when {
    result.isExactMatch -> result.outcome.summary
    result.matchedIngredients >= 2 ->
      "${scenario.visitor.name} внимательно пробует напиток и кивает. Вкус близок к заказу, хотя послевкусие получилось немного насыщеннее."
    else ->
      "${scenario.visitor.name} выпивает лишь половину кружки. Старание замечено, но рецепт ещё стоит доработать."
  }

private fun localizedReaction(result: BrewResult): String =
  when {
    result.isExactMatch -> result.outcome.reactionLine
    result.matchedIngredients >= 2 -> "Не совсем то, что я представлял, но замысел чувствуется."
    else -> "В этом есть душа. Остальное придёт с опытом."
  }

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun ResultScreenPreview() {
  MoonbrewTavernTheme {
    ResultScreen(
      scenario = firstNightScenario,
      gameState =
        firstNightScenario.initialState.copy(
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
