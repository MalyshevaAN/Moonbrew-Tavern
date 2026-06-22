package com.example.moonbrewtavern.ui.recipebook

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.data.content.ContentCatalog
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.Recipe
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme

private data class RecipeBookEntry(
  val id: String,
  val title: String,
  val description: String,
  val effects: List<String>,
  val ingredients: List<RecipeNeed>,
  @param:DrawableRes val illustrationRes: Int,
  val locked: Boolean = false,
  val price: Int = 0,
)

private data class RecipeNeed(
  val name: String,
  val available: Int,
  val required: Int,
  @param:DrawableRes val iconRes: Int,
)

private data class IngredientShopEntry(
  val id: String,
  val name: String,
  val stock: Int,
  val unitPrice: Int,
  @param:DrawableRes val iconRes: Int,
)

/** Recipe book screen used to inspect the active recipe before brewing. */
@Composable
fun RecipeBookScreen(
  scenario: GameScenario,
  gameState: GameState,
  onBack: () -> Unit,
  onSelectRecipe: (String) -> Unit,
  onPurchaseRecipe: (String, Int) -> Boolean,
  onPurchaseIngredient: (String, Int, Int) -> Boolean,
  onStartBrewing: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val recipes =
    remember(gameState.unlockedRecipeIds, gameState.ingredientStock) {
      ContentCatalog.recipes.map { recipe ->
        recipe.toBookEntry(
          locked = recipe.id !in gameState.unlockedRecipeIds,
          price = recipePrice(recipe.id),
          stock = gameState.ingredientStock,
        )
      }
    }
  val shopEntries =
    remember(gameState.ingredientStock) {
      ContentCatalog.ingredients.map { ingredient ->
        IngredientShopEntry(
          id = ingredient.id,
          name = ingredient.name,
          stock = gameState.ingredientStock[ingredient.id] ?: 0,
          unitPrice = ingredientPrice(ingredient.id),
          iconRes = ingredientIconRes(ingredient.id),
        )
      }
    }
  var selectedRecipeId by remember(scenario.recipe.id) { mutableStateOf(scenario.recipe.id) }
  val selectedRecipe = recipes.first { it.id == selectedRecipeId }

  // Full-screen recipe book surface with a dark reading-room palette.
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
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      RecipeBookHeader(
        visitorName = scenario.visitor.name,
        requestLine = scenario.visitor.requestLine,
        gold = gameState.gold,
        onBack = onBack,
      )

      Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        RecipeListPanel(
          recipes = recipes,
          unlockedCount = gameState.unlockedRecipeIds.size,
          shopEntries = shopEntries,
          gold = gameState.gold,
          selectedRecipeId = selectedRecipeId,
          onSelect = { recipe ->
            selectedRecipeId = recipe.id
            if (!recipe.locked) onSelectRecipe(recipe.id)
          },
          onPurchaseIngredient = onPurchaseIngredient,
          modifier = Modifier.width(280.dp).fillMaxHeight(),
        )

        RecipeDetailPanel(
          recipe = selectedRecipe,
          quickRecipes = recipes.filterNot { it.locked },
          gold = gameState.gold,
          onSelectRecipe = {
            selectedRecipeId = it.id
            onSelectRecipe(it.id)
          },
          onPurchaseRecipe = {
            if (onPurchaseRecipe(it.id, it.price)) {
              selectedRecipeId = it.id
              onSelectRecipe(it.id)
            }
          },
          onStartBrewing = onStartBrewing,
          modifier = Modifier.weight(1f).fillMaxHeight(),
        )
      }
    }
  }
}

@Composable
private fun RecipeBookHeader(
  visitorName: String,
  requestLine: String,
  gold: Int,
  onBack: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = Color(0xE62A201D),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF513B32)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        modifier = Modifier.clickable(onClick = onBack),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF4A352C),
      ) {
        Text(
          text = "← Назад",
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
          color = Color(0xFFF0D39D),
          fontWeight = FontWeight.Bold,
        )
      }
      Text(
        text = "Книга рецептов",
        color = Color(0xFFF3E3CF),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "$visitorName: $requestLine",
        modifier = Modifier.weight(1f),
        color = Color(0xFFD7BFA5),
        style = MaterialTheme.typography.bodySmall,
        maxLines = 2,
      )
      Text(
        text = "Золото: $gold",
        color = Color(0xFFF0C88C),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
private fun RecipeListPanel(
  recipes: List<RecipeBookEntry>,
  unlockedCount: Int,
  shopEntries: List<IngredientShopEntry>,
  gold: Int,
  selectedRecipeId: String,
  onSelect: (RecipeBookEntry) -> Unit,
  onPurchaseIngredient: (String, Int, Int) -> Boolean,
  modifier: Modifier = Modifier,
) {
  // Left navigation panel that lists available and locked recipes.
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(26.dp),
    color = Color(0xFF1E1614),
    tonalElevation = 2.dp,
  ) {
    val scrollState = rememberScrollState()

    // Scrollable recipe list with a header card at the top.
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Header card for the recipe index.
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
            text = "$unlockedCount/${recipes.size} доступны",
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

        // Selectable recipe row with icon, title, and short description.
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(18.dp))
              .clickable { onSelect(recipe) }
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
                text = if (recipe.locked) "◈" else recipe.title.take(1),
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
                text = if (recipe.locked) "Купить за ${recipe.price} золота" else "Открыть описание",
                style = MaterialTheme.typography.bodySmall,
                color = if (recipe.locked) Color(0xFF6C5B50) else Color(0xFFCDB49A),
              )
            }
          }
        }
      }

      Text(
        text = "Лавка ингредиентов",
        style = MaterialTheme.typography.titleMedium,
        color = Color(0xFFF0C88C),
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Покупка по 1 единице",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFFCDB49A),
      )
      shopEntries.forEach { ingredient ->
        IngredientShopRow(
          ingredient = ingredient,
          canAfford = gold >= ingredient.unitPrice,
          onPurchase = {
            onPurchaseIngredient(ingredient.id, 1, ingredient.unitPrice)
          },
        )
      }
    }
  }
}

@Composable
private fun RecipeDetailPanel(
  recipe: RecipeBookEntry,
  quickRecipes: List<RecipeBookEntry>,
  gold: Int,
  onSelectRecipe: (RecipeBookEntry) -> Unit,
  onPurchaseRecipe: (RecipeBookEntry) -> Unit,
  onStartBrewing: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val hasEnoughIngredients = recipe.ingredients.all { it.available >= it.required }

  // Right detail panel for the currently selected recipe.
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
          quickRecipes.forEach { quickRecipe ->
            TopTab(
              label = quickRecipe.title.substringBefore(" "),
              active = quickRecipe.id == recipe.id,
              onClick = { onSelectRecipe(quickRecipe) },
            )
          }
        }
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF2B211E),
        ) {
          Text(
            text = "Быстрый выбор",
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
              Image(
                painter = painterResource(recipe.illustrationRes),
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxSize().padding(8.dp),
                contentScale = ContentScale.Fit,
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
              recipe.ingredients.forEach { ingredient ->
                RecipeIngredientCard(
                  ingredient = ingredient,
                  modifier = Modifier.weight(1f),
                )
              }
            }
          }

          Spacer(Modifier.height(8.dp))
        }
      }

      Button(
        onClick = {
          if (recipe.locked) onPurchaseRecipe(recipe) else onStartBrewing()
        },
        enabled =
          if (recipe.locked) {
            gold >= recipe.price
          } else {
            hasEnoughIngredients
          },
        modifier = Modifier.width(160.dp).height(38.dp).align(Alignment.CenterHorizontally),
      ) {
        Text(
          text =
            if (recipe.locked) {
              if (gold >= recipe.price) "Купить • ${recipe.price}" else "Нужно ${recipe.price}"
            } else if (!hasEnoughIngredients) {
              "Не хватает трав"
            } else {
              "Готовить"
            },
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
  onClick: () -> Unit,
) {
  Surface(
    modifier = Modifier.clickable(onClick = onClick),
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
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    color = Color(0xFF2D221F),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Image(
        painter = painterResource(ingredient.iconRes),
        contentDescription = ingredient.name,
        modifier = Modifier.size(48.dp),
        contentScale = ContentScale.Fit,
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
}

@Composable
private fun IngredientShopRow(
  ingredient: IngredientShopEntry,
  canAfford: Boolean,
  onPurchase: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF2A201D),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF43312B)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Image(
        painter = painterResource(ingredient.iconRes),
        contentDescription = ingredient.name,
        modifier = Modifier.size(36.dp),
        contentScale = ContentScale.Fit,
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = ingredient.name,
          color = Color(0xFFF3E3CF),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = "В запасе: ${ingredient.stock}",
          color = Color(0xFFCDB49A),
          style = MaterialTheme.typography.labelSmall,
        )
      }
      Button(
        onClick = onPurchase,
        enabled = canAfford,
        modifier = Modifier.height(36.dp),
      ) {
        Text("+1 • ${ingredient.unitPrice}")
      }
    }
  }
}

@DrawableRes
private fun ingredientIconRes(id: String): Int =
  when (id) {
    "silverfoam" -> R.drawable.brew_item_crystal
    "moonmint" -> R.drawable.brew_item_leaf_bundle
    "emberzest" -> R.drawable.brew_item_ginger_root
    "dusk-syrup" -> R.drawable.brew_item_honey_bottle
    "frost-thyme" -> R.drawable.brew_item_violet_flower
    "cinderbloom" -> R.drawable.brew_item_mushroom
    else -> R.drawable.brew_item_blackberry
  }

private fun Recipe.toBookEntry(
  locked: Boolean,
  price: Int,
  stock: Map<String, Int>,
): RecipeBookEntry =
  RecipeBookEntry(
    id = id,
    title = name,
    description = description,
    effects =
      when (id) {
        "starglow-tonic" -> listOf("+ Спокойствие +10", "+ Фокус +5")
        "herbal-mix" -> listOf("+ Уют +8", "+ Доверие +3")
        "ginger-grog" -> listOf("+ Тепло +12", "+ Смелость +4")
        else -> listOf("+ Магия +7", "+ Настроение +6")
      },
    ingredients =
      requiredIngredients.map {
        RecipeNeed(it.name, stock[it.id] ?: 0, 1, ingredientIconRes(it.id))
      },
    illustrationRes =
      when (id) {
        "herbal-mix" -> R.drawable.recipe_herbal_mix
        "ginger-grog" -> R.drawable.recipe_ginger_grog
        "moon-ale" -> R.drawable.recipe_moon_ale
        else -> R.drawable.recipe_starglow_tonic
      },
    locked = locked,
    price = price,
  )

private fun recipePrice(id: String): Int =
  when (id) {
    "herbal-mix" -> 10
    "ginger-grog" -> 14
    "moon-ale" -> 18
    else -> 0
  }

private fun ingredientPrice(id: String): Int =
  when (id) {
    "moonmint", "dusk-syrup" -> 2
    "emberzest", "frost-thyme" -> 3
    "silverfoam", "cinderbloom" -> 4
    else -> 2
  }

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun RecipeBookScreenPreview() {
  MoonbrewTavernTheme {
    RecipeBookScreen(
      scenario = DefaultDataRepository().scenario,
      gameState = DefaultDataRepository().gameState.value,
      onBack = {},
      onSelectRecipe = {},
      onPurchaseRecipe = { _, _ -> false },
      onPurchaseIngredient = { _, _, _ -> false },
      onStartBrewing = {},
    )
  }
}
