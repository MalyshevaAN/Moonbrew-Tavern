package com.example.moonbrewtavern.ui.brewing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.Ingredient
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme

private data class BrewTone(
  val label: String,
  val color: Color,
)

@Composable
fun BrewingScreen(
  scenario: GameScenario,
  onServe: (Set<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectedIds = remember { mutableStateListOf<String>() }
  var stirCount by rememberSaveable { mutableIntStateOf(0) }
  var selectedToneIndex by rememberSaveable { mutableIntStateOf(-1) }

  val tones =
    listOf(
      BrewTone("Синий", Color(0xFF6678F1)),
      BrewTone("Мятный", Color(0xFF7CBDA0)),
      BrewTone("Розовый", Color(0xFFC779B2)),
      BrewTone("Фиалковый", Color(0xFF9B76D6)),
    )
  val selectedTone = tones.getOrNull(selectedToneIndex)
  val canAddMore = selectedIds.size < scenario.recipe.requiredIngredients.size
  val canHeat = selectedIds.isNotEmpty()
  val canServe = selectedIds.size == scenario.recipe.requiredIngredients.size && stirCount > 0 && selectedTone != null
  val selectedIngredients = scenario.availableIngredients.filter { it.id in selectedIds }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0xFF1A100D),
              Color(0xFF221412),
              Color(0xFF140C0B),
            ),
          ),
        )
        .padding(18.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      BrewingTopBar()

      Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        IngredientRack(
          scenario = scenario,
          selectedIds = selectedIds,
          onIngredientClick = { ingredient ->
            if (ingredient.id in selectedIds) {
              selectedIds.remove(ingredient.id)
            } else if (canAddMore) {
              selectedIds.add(ingredient.id)
            }
          },
          modifier = Modifier.width(170.dp).fillMaxHeight(),
        )

        CauldronStage(
          selectedIngredients = selectedIngredients,
          stirCount = stirCount,
          selectedTone = selectedTone,
          modifier = Modifier.weight(1f).fillMaxHeight(),
        )

        BrewingStepsPanel(
          selectedCount = selectedIds.size,
          stirCount = stirCount,
          selectedTone = selectedTone,
          canHeat = canHeat,
          onStir = { if (canHeat && stirCount < 3) stirCount += 1 },
          onServe = { onServe(selectedIds.toSet()) },
          canServe = canServe,
          modifier = Modifier.width(290.dp).fillMaxHeight(),
        )
      }

      ColorRail(
        tones = tones,
        selectedToneIndex = selectedToneIndex,
        onSelectTone = { selectedToneIndex = it },
      )
    }
  }
}

@Composable
private fun BrewingTopBar() {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
      TopControlBox(text = "<-")
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF2C2230),
      ) {
        Text(
          text = "Приготовление",
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
          style = MaterialTheme.typography.titleLarge,
          color = Color(0xFFF2E5D5),
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
    TopControlBox(text = "?")
  }
}

@Composable
private fun TopControlBox(text: String) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color(0xFF2E221D),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5A4337)),
  ) {
    Box(
      modifier = Modifier.size(54.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = text,
        color = Color(0xFFF2E5D5),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
private fun IngredientRack(
  scenario: GameScenario,
  selectedIds: List<String>,
  onIngredientClick: (Ingredient) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    scenario.availableIngredients.take(4).forEach { ingredient ->
      val selected = ingredient.id in selectedIds
      Surface(
        modifier =
          Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onIngredientClick(ingredient) }
            .border(
              width = if (selected) 2.dp else 1.dp,
              color = if (selected) Color(0xFFF0C98B) else Color(0xFF5D463B),
              shape = RoundedCornerShape(20.dp),
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFF4D382E) else Color(0xFF251A17),
      ) {
        Column(
          modifier = Modifier.fillMaxSize().padding(12.dp),
          verticalArrangement = Arrangement.SpaceBetween,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier =
              Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ingredientPlaceholderBrush(ingredient.name)),
          )
          Text(
            text = ingredient.name,
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFFF2E5D5),
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = ingredient.stockCount.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFF2E5D5),
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

@Composable
private fun CauldronStage(
  selectedIngredients: List<Ingredient>,
  stirCount: Int,
  selectedTone: BrewTone?,
  modifier: Modifier = Modifier,
) {
  val liquidColor = selectedTone?.color ?: Color(0xFF4B6888)
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(28.dp),
    color = Color(0xFF1C1412),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF46322B)),
  ) {
    Box(
      modifier = Modifier.fillMaxSize().padding(18.dp),
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier =
            Modifier
              .size(340.dp)
              .clip(CircleShape)
              .background(Color(0xFF281C18))
              .border(18.dp, Color(0xFF47332C), CircleShape),
        ) {
          Box(
            modifier =
              Modifier
                .align(Alignment.Center)
                .size(250.dp)
                .clip(CircleShape)
                .background(
                  Brush.radialGradient(
                    listOf(
                      liquidColor.copy(alpha = 0.98f),
                      liquidColor.copy(alpha = 0.75f),
                      Color(0xFF2A3850),
                    ),
                  ),
                ),
          )

          selectedIngredients.forEachIndexed { index, ingredient ->
            Box(
              modifier =
                Modifier
                  .align(
                    when (index) {
                      0 -> Alignment.TopStart
                      1 -> Alignment.TopEnd
                      else -> Alignment.BottomCenter
                    },
                  )
                  .padding(
                    start = if (index == 0) 84.dp else 0.dp,
                    end = if (index == 1) 84.dp else 0.dp,
                    top = if (index < 2) 88.dp else 0.dp,
                    bottom = if (index == 2) 86.dp else 0.dp,
                  )
                  .size(34.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(ingredientPlaceholderBrush(ingredient.name)),
            )
          }

          Text(
            text = if (selectedIngredients.isEmpty()) "Котел пуст" else "Помешано: $stirCount/3",
            modifier = Modifier.align(Alignment.Center),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

@Composable
private fun BrewingStepsPanel(
  selectedCount: Int,
  stirCount: Int,
  selectedTone: BrewTone?,
  canHeat: Boolean,
  onStir: () -> Unit,
  onServe: () -> Unit,
  canServe: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    BrewStepCard(
      title = "1. Добавь ингредиент",
      subtitle = "$selectedCount/3 в котле",
      accent = Color(0xFF8A6650),
      modifier = Modifier.weight(1f),
    )
    BrewStepCard(
      title = "2. Перемешай",
      subtitle = if (canHeat) "Нажми, чтобы перемешать" else "Сначала кинь ингредиенты",
      accent = Color(0xFF7E5E4A),
      modifier = Modifier.weight(1f),
      onClick = onStir,
      enabled = canHeat,
    )
    BrewStepCard(
      title = "3. Выбери цвет",
      subtitle = selectedTone?.label ?: "Ни один оттенок не выбран",
      accent = selectedTone?.color ?: Color(0xFF835F4B),
      modifier = Modifier.weight(1f),
    )
    Surface(
      modifier = Modifier.fillMaxWidth().weight(1f),
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFF2A1D19),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5A4337)),
    ) {
      Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "4. Подай напиток",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFF2E5D5),
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = if (canServe) "Смесь готова к подаче." else "Нужно 3 ингредиента, перемешивание и выбранный цвет.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFD0B7A3),
          )
        }
        Button(
          onClick = onServe,
          enabled = canServe,
          modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
          Text(
            text = "Подать",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}

@Composable
private fun BrewStepCard(
  title: String,
  subtitle: String,
  accent: Color,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  enabled: Boolean = true,
) {
  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier),
    shape = RoundedCornerShape(20.dp),
    color = if (enabled) Color(0xFF2A1D19) else Color(0xFF221816),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5A4337)),
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accent),
      )
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = Color(0xFFF2E5D5),
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = if (enabled) Color(0xFFD0B7A3) else Color(0xFF8A7366),
        )
      }
    }
  }
}

@Composable
private fun ColorRail(
  tones: List<BrewTone>,
  selectedToneIndex: Int,
  onSelectTone: (Int) -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    color = Color(0xFF241815),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5A4337)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Spacer(Modifier.width(80.dp))
      tones.forEachIndexed { index, tone ->
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(tone.color)
              .border(
                width = if (selectedToneIndex == index) 3.dp else 1.dp,
                color = if (selectedToneIndex == index) Color(0xFFF2D28F) else Color(0xFF3C2D28),
                shape = RoundedCornerShape(10.dp),
              )
              .clickable { onSelectTone(index) },
        )
      }
      Spacer(Modifier.width(80.dp))
    }
  }
}

private fun ingredientPlaceholderBrush(name: String): Brush =
  Brush.linearGradient(
    when (name.lowercase()) {
      "moonmint" -> listOf(Color(0xFF6AB1A2), Color(0xFF416B62))
      "ember zest" -> listOf(Color(0xFFD59A63), Color(0xFF7F4E2E))
      "silverfoam" -> listOf(Color(0xFFACC2FF), Color(0xFF667BB0))
      "dusk syrup" -> listOf(Color(0xFF8C664F), Color(0xFF503728))
      "frost thyme" -> listOf(Color(0xFFA2C37F), Color(0xFF5A6F45))
      "cinderbloom" -> listOf(Color(0xFFCB7FB3), Color(0xFF74486A))
      else -> listOf(Color(0xFF8F705A), Color(0xFF4C372D))
    },
  )

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun BrewingScreenPreview() {
  MoonbrewTavernTheme {
    BrewingScreen(
      scenario = DefaultDataRepository().scenario,
      onServe = {},
    )
  }
}
