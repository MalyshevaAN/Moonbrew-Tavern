package com.example.moonbrewtavern.ui.recipebook

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private data class RecipeBookEntry(
  val id: String,
  val title: String,
  val description: String,
  val effects: List<String>,
  val ingredients: List<RecipeNeed>,
  val locked: Boolean = false,
)

private data class RecipeNeed(
  val name: String,
  val available: Int,
  val required: Int,
)

/** Recipe book screen used to inspect the active recipe before brewing. */
@Composable
fun RecipeBookScreen(
  scenario: GameScenario,
  onStartBrewing: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val recipes =
    remember(scenario.recipe.id) {
      listOf(
        RecipeBookEntry(
          id = scenario.recipe.id,
          title = "Звездный тоник",
          description = "Легкий напиток для тех, кому нужно сохранить ясную голову и мягко вернуть себе внутреннее равновесие.",
          effects = listOf("+ Спокойствие +10", "+ Фокус +5"),
          ingredients =
            scenario.recipe.requiredIngredients.map { ingredient ->
              RecipeNeed(
                name = ingredient.name,
                available = ingredient.stockCount,
                required = 1,
              )
            },
        ),
        RecipeBookEntry(
          id = "herbal-mix",
          title = "Травяной сбор",
          description = "Нежный вечерний настой с сухими травами и успокаивающим послевкусием.",
          effects = listOf("+ Уют +8", "+ Доверие +3"),
          ingredients = listOf(
            RecipeNeed("Frost Thyme", 2, 1),
            RecipeNeed("Moonmint", 6, 1),
            RecipeNeed("Dusk Syrup", 4, 1),
          ),
        ),
        RecipeBookEntry(
          id = "ginger-grog",
          title = "Имбирный грог",
          description = "Плотный согревающий напиток для холодной дороги и тяжелых разговоров.",
          effects = listOf("+ Тепло +12", "+ Смелость +4"),
          ingredients = listOf(
            RecipeNeed("Ember Zest", 5, 1),
            RecipeNeed("Cinderbloom", 1, 1),
            RecipeNeed("Dusk Syrup", 4, 1),
          ),
        ),
        RecipeBookEntry(
          id = "moon-ale",
          title = "Лунный эль",
          description = "Редкий пенящийся напиток с холодным сиянием и чуть сладковатой дымкой.",
          effects = listOf("+ Магия +7", "+ Настроение +6"),
          ingredients = listOf(
            RecipeNeed("Silverfoam", 3, 1),
            RecipeNeed("Moonmint", 6, 1),
            RecipeNeed("Ember Zest", 5, 1),
          ),
        ),
        RecipeBookEntry(
          id = "locked",
          title = "???",
          description = "Этот рецепт пока спрятан в следующих главах книги.",
          effects = emptyList(),
          ingredients = emptyList(),
          locked = true,
        ),
      )
    }
  var selectedRecipeId by remember { mutableStateOf(recipes.first().id) }
  val selectedRecipe = recipes.first { it.id == selectedRecipeId }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0xFF181110),
              Color(0xFF201614),
              Color(0xFF130E0D),
            ),
          ),
        )
        .padding(18.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      RecipeListPanel(
        recipes = recipes,
        selectedRecipeId = selectedRecipeId,
        onSelect = { recipe ->
          if (!recipe.locked) {
            selectedRecipeId = recipe.id
          }
        },
        modifier = Modifier.width(280.dp).fillMaxHeight(),
      )

      RecipeDetailPanel(
        recipe = selectedRecipe,
        onStartBrewing = onStartBrewing,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }
  }
}

@Composable
private fun RecipeListPanel(
  recipes: List<RecipeBookEntry>,
  selectedRecipeId: String,
  onSelect: (RecipeBookEntry) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(26.dp),
    color = Color(0xFF1E1614),
    tonalElevation = 2.dp,
  ) {
    val scrollState = rememberScrollState()
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF2C2230),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "Рецепты",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFFF3E3CF),
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "4/5 доступны",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCDB49A),
          )
        }
      }

      recipes.forEach { recipe ->
        val isSelected = recipe.id == selectedRecipeId
        val container =
          when {
            recipe.locked -> Color(0xFF241B18)
            isSelected -> Color(0xFF4C372B)
            else -> Color(0xFF2A201D)
          }
        val borderColor =
          when {
            isSelected -> Color(0xFFD2A56E)
            else -> Color(0xFF43312B)
          }

        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(18.dp))
              .clickable(enabled = !recipe.locked) { onSelect(recipe) }
              .border(1.dp, borderColor, RoundedCornerShape(18.dp)),
          shape = RoundedCornerShape(18.dp),
          color = container,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier =
                Modifier
                  .size(46.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (recipe.locked) Color(0xFF3A2C27) else Color(0xFF71523C)),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = if (recipe.locked) "?" else recipe.title.take(1),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFF8EBD8),
                fontWeight = FontWeight.Bold,
              )
            }

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
              Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (recipe.locked) Color(0xFF9D8A7C) else Color(0xFFF3E3CF),
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = if (recipe.locked) "Рецепт закрыт" else "Открыть описание",
                style = MaterialTheme.typography.bodySmall,
                color = if (recipe.locked) Color(0xFF6C5B50) else Color(0xFFCDB49A),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RecipeDetailPanel(
  recipe: RecipeBookEntry,
  onStartBrewing: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(28.dp),
    color = Color(0xFF1B1412),
    tonalElevation = 2.dp,
  ) {
    val scrollState = rememberScrollState()
    Column(
      modifier = Modifier.fillMaxSize().padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          TopTab("Кружка", active = true)
          TopTab("Травы")
          TopTab("Избранное")
        }
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF2B211E),
        ) {
          Text(
            text = "Книга рецептов",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = Color(0xFFF3E3CF),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      Surface(
        modifier = Modifier.fillMaxWidth().weight(1f),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF231A17),
      ) {
        Column(
          modifier =
            Modifier
              .fillMaxSize()
              .verticalScroll(scrollState)
              .padding(22.dp),
          verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
          Text(
            text = recipe.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFF3E3CF),
            fontWeight = FontWeight.Bold,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
          ) {
            Box(
              modifier =
                Modifier
                  .size(170.dp)
                  .clip(RoundedCornerShape(22.dp))
                  .background(Color(0xFF312623)),
                  contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "Иллюстрация\nрецепта",
                color = Color(0xFFCDB49A),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
            }

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE3D3C0),
              )

              Text(
                text = "Эффекты:",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFF0C88C),
                fontWeight = FontWeight.SemiBold,
              )
              if (recipe.effects.isEmpty()) {
                Text(
                  text = "Пока нет данных.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFFCDB49A),
                )
              } else {
                recipe.effects.forEach { effect ->
                  Text(
                    text = effect,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF9DCE8D),
                    fontWeight = FontWeight.Medium,
                  )
                }
              }
            }
          }

          Text(
            text = "Ингредиенты:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFF3E3CF),
            fontWeight = FontWeight.SemiBold,
          )

          if (recipe.ingredients.isEmpty()) {
            Surface(
              shape = RoundedCornerShape(18.dp),
              color = Color(0xFF2D221F),
            ) {
              Text(
                text = "Этот рецепт пока скрыт.",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = Color(0xFFCDB49A),
                style = MaterialTheme.typography.bodyMedium,
              )
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              recipe.ingredients.forEachIndexed { index, ingredient ->
                RecipeIngredientCard(
                  ingredient = ingredient,
                  isLast = index == recipe.ingredients.lastIndex,
                  modifier = Modifier.weight(1f),
                )
              }
            }
          }

          Spacer(Modifier.height(8.dp))
        }
      }

      Button(
        onClick = onStartBrewing,
        enabled = !recipe.locked,
        modifier = Modifier.width(160.dp).height(38.dp).align(Alignment.CenterHorizontally),
      ) {
        Text(
          text = "Готовить",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Composable
private fun TopTab(
  label: String,
  active: Boolean = false,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (active) Color(0xFF5A3F31) else Color(0xFF2A201D),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Color(0xFFD2A56E) else Color(0xFF44332D)),
  ) {
    Text(
      text = label,
      modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
      color = Color(0xFFF3E3CF),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

@Composable
private fun RecipeIngredientCard(
  ingredient: RecipeNeed,
  isLast: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      color = Color(0xFF2D221F),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color(0xFF7A5A43)),
        )
        Text(
          text = ingredient.name,
          style = MaterialTheme.typography.titleSmall,
          color = Color(0xFFF3E3CF),
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = "${ingredient.available}/${ingredient.required}",
          style = MaterialTheme.typography.headlineSmall,
          color = if (ingredient.available >= ingredient.required) Color(0xFFF0D28F) else Color(0xFFD98B8B),
          fontWeight = FontWeight.Bold,
        )
      }
    }
    if (!isLast) {
      Text(
        text = "->",
        style = MaterialTheme.typography.titleLarge,
        color = Color(0xFF8D6B54),
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun RecipeBookScreenPreview() {
  MoonbrewTavernTheme {
    RecipeBookScreen(
      scenario = DefaultDataRepository().scenario,
      onStartBrewing = {},
    )
  }
}
