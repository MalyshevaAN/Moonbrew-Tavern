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
import androidx.compose.foundation.layout.heightIn
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

@Composable
fun BrewingScreen(
  scenario: GameScenario,
  selectedIngredientIds: List<String>,
  onIngredientToggle: (String) -> Unit,
  onServe: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val selectedIngredients = scenario.availableIngredients.filter { it.id in selectedIngredientIds }
  val requiredIds = scenario.recipe.requiredIngredients.map { it.id }.toSet()
  val matchedCount = selectedIngredients.count { it.id in requiredIds }
  val canServe = selectedIngredientIds.size == 3
  val statusText =
    when {
      selectedIngredientIds.isEmpty() -> "Choose three ingredients for Lyra's order."
      selectedIngredientIds.size < 3 -> "One more careful pick and the pour is ready."
      selectedIngredientIds.toSet() == requiredIds -> "This reads clean, bright, and exact."
      matchedCount >= 2 -> "Close. The bones of the drink are right, but one note is off."
      else -> "This will taste improvised. Brave, but probably not what she asked for."
    }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0xFFECE0D1),
              Color(0xFFF8F2E9),
            ),
          ),
        )
        .padding(22.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      BrewingHeader(scenario = scenario)

      Row(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Column(
          modifier = Modifier.weight(0.9f),
          verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
          OrderCard(scenario = scenario)
          GuestCard(scenario = scenario)
        }

        WorkbenchCard(
          scenario = scenario,
          selectedIngredients = selectedIngredients,
          matchedCount = matchedCount,
          modifier = Modifier.weight(1.1f),
        )

        Column(
          modifier = Modifier.weight(0.88f),
          verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
          CurrentPourCard(
            selectedIngredients = selectedIngredients,
            matchedCount = matchedCount,
            statusText = statusText,
            totalRequired = scenario.recipe.requiredIngredients.size,
          )
        }
      }

      IngredientShelf(
        scenario = scenario,
        selectedIngredientIds = selectedIngredientIds,
        onIngredientToggle = onIngredientToggle,
        modifier = Modifier.heightIn(min = 220.dp),
      )

      ActionBar(
        selectedCount = selectedIngredientIds.size,
        canServe = canServe,
        onServe = onServe,
      )
    }
  }
}

@Composable
private fun BrewingHeader(scenario: GameScenario) {
  val colors = MaterialTheme.colorScheme
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Surface(
        shape = RoundedCornerShape(999.dp),
        color = colors.secondaryContainer,
      ) {
        Text(
          text = "Brewing Station",
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          style = MaterialTheme.typography.labelLarge,
          color = colors.onSecondaryContainer,
          fontWeight = FontWeight.SemiBold,
        )
      }
      Text(
        text = scenario.recipe.name,
        style = MaterialTheme.typography.displaySmall,
        color = Color(0xFF21372E),
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Build the drink in one continuous scene: order on the left, mixing in the middle, judgment on the right.",
        style = MaterialTheme.typography.bodyLarge,
        color = colors.onSurfaceVariant,
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      StatPill(label = "Night", value = scenario.initialState.day.toString())
      StatPill(label = "Gold", value = scenario.initialState.gold.toString())
      StatPill(label = "Rep", value = scenario.initialState.reputation.toString())
    }
  }
}

@Composable
private fun OrderCard(scenario: GameScenario) {
  Surface(
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    tonalElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      SectionEyebrow("ORDER")
      Text(
        text = "\"${scenario.visitor.requestLine}\"",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = scenario.brewingHint,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.height(6.dp))
      FlavorLine(label = "Target profile", value = scenario.visitor.favoriteFlavor)
      FlavorLine(label = "Recipe", value = scenario.recipe.name)
    }
  }
}

@Composable
private fun GuestCard(scenario: GameScenario) {
  Surface(
    shape = RoundedCornerShape(28.dp),
    color = Color(0xFF2F5B4A),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(22.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(
                  Color(0xFFF1C9B4),
                  Color(0xFFAA6E56),
                ),
              ),
            ),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = scenario.visitor.name.take(1),
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
      }
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = scenario.visitor.name,
          style = MaterialTheme.typography.titleLarge,
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = scenario.visitor.title,
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFFD9E7DF),
        )
        Text(
          text = "Mood: ${scenario.visitor.mood.name}",
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFFD9E7DF),
        )
      }
    }
  }
}

@Composable
private fun WorkbenchCard(
  scenario: GameScenario,
  selectedIngredients: List<Ingredient>,
  matchedCount: Int,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(32.dp),
    color = Color(0xFF264337),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(24.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionEyebrow("WORKBENCH", contentColor = Color(0xFFDCE9E1))
        Text(
          text = "Copper cauldron",
          style = MaterialTheme.typography.headlineSmall,
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "The selected ingredients should feel like they are entering one shared vessel, not just flipping cards in a form.",
          style = MaterialTheme.typography.bodyLarge,
          color = Color(0xFFDCE9E1),
        )
      }

      Box(
        modifier = Modifier.fillMaxWidth().weight(1f),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier =
            Modifier
              .size(250.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  listOf(
                    Color(0xFF5F8D7B),
                    Color(0xFF1D2F28),
                  ),
                ),
              )
              .border(10.dp, Color(0xFF8C6042), CircleShape),
        )

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          if (selectedIngredients.isEmpty()) {
            Text(
              text = "Empty pot",
              style = MaterialTheme.typography.headlineSmall,
              color = Color.White,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = "Select ingredients below",
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFFDCE9E1),
            )
          } else {
            selectedIngredients.forEach { ingredient ->
              Surface(
                shape = RoundedCornerShape(999.dp),
                color = colors.secondaryContainer.copy(alpha = 0.92f),
              ) {
                Text(
                  text = ingredient.name,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                  style = MaterialTheme.typography.bodyMedium,
                  color = colors.onSecondaryContainer,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.12f),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          FlavorLine(label = "Matched recipe notes", value = "$matchedCount / ${scenario.recipe.requiredIngredients.size}", valueColor = Color.White, labelColor = Color(0xFFDCE9E1))
          FlavorLine(label = "Required", value = scenario.recipe.requiredIngredients.joinToString { it.name }, valueColor = Color.White, labelColor = Color(0xFFDCE9E1))
        }
      }
    }
  }
}

@Composable
private fun CurrentPourCard(
  selectedIngredients: List<Ingredient>,
  matchedCount: Int,
  statusText: String,
  totalRequired: Int,
) {
  Surface(
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    tonalElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      SectionEyebrow("CURRENT POUR")
      Text(
        text = if (selectedIngredients.isEmpty()) "Nothing selected yet" else "Tray is coming together",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = statusText,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FlavorLine(label = "Selected", value = "${selectedIngredients.size} / 3")
          FlavorLine(label = "Recipe match", value = "$matchedCount / $totalRequired")
        }
      }
      selectedIngredients.forEach { ingredient ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = ingredient.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
          )
          Text(
            text = ingredient.rarity.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
private fun IngredientShelf(
  scenario: GameScenario,
  selectedIngredientIds: List<String>,
  onIngredientToggle: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(30.dp),
    color = Color(0xFFF4ECDF),
    tonalElevation = 1.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Ingredient shelf",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Pick exactly three. A correct drink needs cool clarity, warm finish, and a silver shimmer.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Surface(
          shape = RoundedCornerShape(999.dp),
          color = MaterialTheme.colorScheme.tertiaryContainer,
        ) {
          Text(
            text = "${selectedIngredientIds.size}/3 selected",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      scenario.availableIngredients.chunked(3).forEach { rowIngredients ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          rowIngredients.forEach { ingredient ->
            val isSelected = ingredient.id in selectedIngredientIds
            val canSelectMore = selectedIngredientIds.size < 3
            IngredientShelfCard(
              ingredient = ingredient,
              selected = isSelected,
              enabled = isSelected || canSelectMore,
              modifier = Modifier.weight(1f),
              onClick = { onIngredientToggle(ingredient.id) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ActionBar(
  selectedCount: Int,
  canServe: Boolean,
  onServe: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = Color.White.copy(alpha = 0.92f),
    tonalElevation = 2.dp,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = if (canServe) "The tray is ready" else "Choose exactly three ingredients",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = "$selectedCount / 3 selected",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Button(
        onClick = onServe,
        enabled = canServe,
        modifier = Modifier.width(220.dp).height(54.dp),
      ) {
        Text(
          text = "Serve the drink",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Composable
private fun IngredientShelfCard(
  ingredient: Ingredient,
  selected: Boolean,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val background =
    when {
      selected -> colors.primaryContainer
      else -> Color.White
    }
  Surface(
    modifier =
      modifier
        .clip(RoundedCornerShape(22.dp))
        .clickable(enabled = enabled, onClick = onClick)
        .border(
          width = if (selected) 2.dp else 1.dp,
          color = if (selected) colors.primary else Color(0xFFE2D8CA),
          shape = RoundedCornerShape(22.dp),
        ),
    color = background,
    shape = RoundedCornerShape(22.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = ingredient.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
        Box(
          modifier =
            Modifier
              .size(12.dp)
              .clip(CircleShape)
              .background(
                when (ingredient.rarity) {
                  com.example.moonbrewtavern.domain.model.IngredientRarity.Common -> Color(0xFF7AA17F)
                  com.example.moonbrewtavern.domain.model.IngredientRarity.Uncommon -> Color(0xFFB8784F)
                  com.example.moonbrewtavern.domain.model.IngredientRarity.Rare -> Color(0xFF6A82C8)
                },
              ),
        )
      }
      Text(
        text = ingredient.flavorNote,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.onSurfaceVariant,
      )
      Text(
        text = if (selected) "Selected" else ingredient.rarity.name,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) colors.primary else colors.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
private fun StatPill(label: String, value: String) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = Color.White.copy(alpha = 0.85f),
  ) {
    Column(
      modifier = Modifier.width(112.dp).padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
private fun SectionEyebrow(text: String, contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
  Text(
    text = text,
    style = MaterialTheme.typography.labelLarge,
    color = contentColor,
    fontWeight = FontWeight.SemiBold,
  )
}

@Composable
private fun FlavorLine(
  label: String,
  value: String,
  labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
  Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = labelColor,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyLarge,
      color = valueColor,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun BrewingScreenPreview() {
  MoonbrewTavernTheme {
    BrewingScreen(
      scenario = DefaultDataRepository().scenario,
      selectedIngredientIds = listOf("moonmint", "emberzest"),
      onIngredientToggle = {},
      onServe = {},
    )
  }
}
