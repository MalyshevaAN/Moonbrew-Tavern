package com.example.moonbrewtavern.ui.brewing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme

private data class BrewTone(
  val label: String,
  val color: Color,
)

@Composable
fun BrewingScreen(
  scenario: GameScenario,
  onServe: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tones =
    listOf(
      BrewTone("Лунный синий", Color(0xFF6F8DFF)),
      BrewTone("Травяной зеленый", Color(0xFF6FAF7C)),
      BrewTone("Ягодный розовый", Color(0xFFC972A9)),
      BrewTone("Янтарный", Color(0xFFCC8C49)),
    )
  var ingredientsAdded by rememberSaveable { mutableStateOf(false) }
  var stirCount by rememberSaveable { mutableIntStateOf(0) }
  var selectedToneIndex by rememberSaveable { mutableIntStateOf(-1) }

  val selectedTone = tones.getOrNull(selectedToneIndex)
  val isReady = ingredientsAdded && stirCount > 0 && selectedTone != null
  val progressText =
    when {
      !ingredientsAdded -> "Сначала засыпаем готовые ингредиенты из выбранного рецепта."
      stirCount == 0 -> "Основа уже в котле. Теперь нужно хотя бы немного перемешать."
      selectedTone == null -> "Смесь ожила. Осталось выбрать оттенок напитка."
      else -> "Напиток собран. Можно подавать."
    }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0xFF201512),
              Color(0xFF2A1B17),
              Color(0xFF160F0D),
            ),
          ),
        )
        .padding(22.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      BrewingHeader(scenario = scenario)

      Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        RecipeReminderCard(
          scenario = scenario,
          ingredientsAdded = ingredientsAdded,
          modifier = Modifier.weight(0.92f),
        )
        CauldronSceneCard(
          selectedTone = selectedTone,
          stirCount = stirCount,
          ingredientsAdded = ingredientsAdded,
          modifier = Modifier.weight(1.12f),
        )
        BrewingControlsCard(
          tones = tones,
          selectedToneIndex = selectedToneIndex,
          ingredientsAdded = ingredientsAdded,
          stirCount = stirCount,
          progressText = progressText,
          onAddIngredients = { ingredientsAdded = true },
          onStir = { if (ingredientsAdded && stirCount < 3) stirCount += 1 },
          onSelectTone = { selectedToneIndex = it },
          modifier = Modifier.weight(0.96f),
        )
      }

      ServeActionBar(
        isReady = isReady,
        selectedTone = selectedTone?.label,
        stirCount = stirCount,
        onServe = onServe,
      )
    }
  }
}

@Composable
private fun BrewingHeader(scenario: GameScenario) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFF4B352C),
      ) {
        Text(
          text = "Приготовление",
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          color = Color(0xFFF5E6D2),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
        )
      }
      Text(
        text = scenario.recipe.name,
        style = MaterialTheme.typography.displaySmall,
        color = Color(0xFFF5E6D2),
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "На этом экране мы уже не ищем рецепт, а собираем готовую смесь: добавляем ингредиенты, мешаем и выбираем оттенок напитка.",
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFFD5BFAF),
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      StatTile(label = "Ночь", value = scenario.initialState.day.toString())
      StatTile(label = "Золото", value = scenario.initialState.gold.toString())
      StatTile(label = "Реп", value = scenario.initialState.reputation.toString())
    }
  }
}

@Composable
private fun RecipeReminderCard(
  scenario: GameScenario,
  ingredientsAdded: Boolean,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(30.dp),
    color = Color(0xFF261A17),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = "Что идет в котел",
        style = MaterialTheme.typography.titleLarge,
        color = Color(0xFFF5E6D2),
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = scenario.recipe.description,
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFFD5BFAF),
      )

      scenario.recipe.requiredIngredients.forEachIndexed { index, ingredient ->
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color(0xFF33231E),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = "${index + 1}. ${ingredient.name}",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFF5E6D2),
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = ingredient.flavorNote,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD5BFAF),
              )
            }
            Text(
              text = "x${ingredient.stockCount}",
              style = MaterialTheme.typography.titleMedium,
              color = if (ingredient.stockCount > 0) Color(0xFFB9D7A8) else Color(0xFFD98686),
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (ingredientsAdded) Color(0xFF33503E) else Color(0xFF3A2A25),
      ) {
        Text(
          text =
            if (ingredientsAdded) {
              "Все нужные ингредиенты уже отправлены в котел."
            } else {
              "Ингредиенты пока только на полке. Добавь их одной командой справа."
            },
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFFF5E6D2),
        )
      }
    }
  }
}

@Composable
private fun CauldronSceneCard(
  selectedTone: BrewTone?,
  stirCount: Int,
  ingredientsAdded: Boolean,
  modifier: Modifier = Modifier,
) {
  val liquidColor = selectedTone?.color ?: Color(0xFF5C6C8B)
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(32.dp),
    color = Color(0xFF1E1715),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(22.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Котел",
          style = MaterialTheme.typography.titleLarge,
          color = Color(0xFFF5E6D2),
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "Здесь мы просто разыгрываем сам ритуал: ингредиенты уже известны, осталось собрать подачу.",
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFFD5BFAF),
        )
      }

      Box(
        modifier = Modifier.fillMaxWidth().weight(1f),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Box(
            modifier =
              Modifier
                .size(260.dp)
                .clip(CircleShape)
                .background(Color(0xFF2C1D19))
                .border(14.dp, Color(0xFF5A3B2A), CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Box(
              modifier =
                Modifier
                  .size(198.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.radialGradient(
                      listOf(
                        liquidColor.copy(alpha = 0.95f),
                        liquidColor.copy(alpha = 0.72f),
                        Color(0xFF1B2333),
                      ),
                    ),
                  ),
            )
            Text(
              text =
                when {
                  !ingredientsAdded -> "Пусто"
                  stirCount == 0 -> "Основа готова"
                  else -> "Помешано: $stirCount/3"
                },
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.SemiBold,
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StepBadge(step = "1", label = if (ingredientsAdded) "Добавлено" else "Не добавлено")
            StepBadge(step = "2", label = "Мешаем x$stirCount")
            StepBadge(step = "3", label = selectedTone?.label ?: "Без оттенка")
          }
        }
      }

      Text(
        text = "Цвет пока влияет только на визуальный образ напитка. Позже сюда можно привязать вкус, настроение гостя или редкие эффекты.",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFFB79E8D),
      )
    }
  }
}

@Composable
private fun BrewingControlsCard(
  tones: List<BrewTone>,
  selectedToneIndex: Int,
  ingredientsAdded: Boolean,
  stirCount: Int,
  progressText: String,
  onAddIngredients: () -> Unit,
  onStir: () -> Unit,
  onSelectTone: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(30.dp),
    color = Color(0xFF261A17),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = "Шаги",
        style = MaterialTheme.typography.titleLarge,
        color = Color(0xFFF5E6D2),
        fontWeight = FontWeight.Bold,
      )

      BrewingActionTile(
        title = "1. Добавить ингредиенты",
        subtitle = if (ingredientsAdded) "Все обязательные ингредиенты уже в котле." else "Перенеси в котел готовый набор из рецепта.",
        active = ingredientsAdded,
        onClick = onAddIngredients,
      )
      BrewingActionTile(
        title = "2. Перемешать",
        subtitle = if (!ingredientsAdded) "Сначала добавь ингредиенты." else "Сейчас: $stirCount / 3 перемешиваний",
        active = stirCount > 0,
        enabled = ingredientsAdded,
        onClick = onStir,
      )

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "3. Выбери оттенок",
          style = MaterialTheme.typography.titleMedium,
          color = Color(0xFFF5E6D2),
          fontWeight = FontWeight.SemiBold,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          tones.forEachIndexed { index, tone ->
            Surface(
              modifier =
                Modifier
                  .weight(1f)
                  .height(60.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .clickable { onSelectTone(index) }
                  .border(
                    width = if (selectedToneIndex == index) 2.dp else 1.dp,
                    color = if (selectedToneIndex == index) Color(0xFFF2D28F) else Color(0xFF5F4638),
                    shape = RoundedCornerShape(16.dp),
                  ),
              shape = RoundedCornerShape(16.dp),
              color = tone.color.copy(alpha = 0.88f),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = tone.label,
                  style = MaterialTheme.typography.labelMedium,
                  color = Color.White,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF34231E),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Состояние напитка",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFFF5E6D2),
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = progressText,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD5BFAF),
          )
        }
      }
    }
  }
}

@Composable
private fun BrewingActionTile(
  title: String,
  subtitle: String,
  active: Boolean,
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  val container =
    when {
      active -> Color(0xFF3A543F)
      enabled -> Color(0xFF34231E)
      else -> Color(0xFF2A1D19)
    }
  Surface(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .clickable(enabled = enabled, onClick = onClick),
    shape = RoundedCornerShape(18.dp),
    color = container,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = Color(0xFFF5E6D2),
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = if (enabled) Color(0xFFD5BFAF) else Color(0xFF8D7366),
      )
    }
  }
}

@Composable
private fun StepBadge(
  step: String,
  label: String,
) {
  Surface(
    shape = RoundedCornerShape(999.dp),
    color = Color(0xFF34231E),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(Color(0xFF8B6548)),
        contentAlignment = Alignment.Center,
      ) {
        Text(text = step, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = Color(0xFFF5E6D2),
      )
    }
  }
}

@Composable
private fun ServeActionBar(
  isReady: Boolean,
  selectedTone: String?,
  stirCount: Int,
  onServe: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = Color(0xFF241A16),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = if (isReady) "Можно подавать напиток" else "Нужно закончить сборку напитка",
          style = MaterialTheme.typography.titleMedium,
          color = Color(0xFFF5E6D2),
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = "Перемешано: $stirCount/3 • Оттенок: ${selectedTone ?: "не выбран"}",
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFFD5BFAF),
        )
      }

      Button(
        onClick = onServe,
        enabled = isReady,
        modifier = Modifier.width(220.dp).height(54.dp),
      ) {
        Text(
          text = "Подать напиток",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Composable
private fun StatTile(label: String, value: String) {
  Surface(
    shape = RoundedCornerShape(22.dp),
    color = Color(0xFF34231E),
  ) {
    Column(
      modifier = Modifier.width(108.dp).padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFFD5BFAF),
      )
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        color = Color(0xFFF5E6D2),
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

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
