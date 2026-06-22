package com.example.moonbrewtavern.data.content

import com.example.moonbrewtavern.data.content.ingredients.firstNightIngredients
import com.example.moonbrewtavern.data.content.recipes.starglowTonicRecipe
import com.example.moonbrewtavern.data.content.visitors.lyraVisitor
import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.ServingOutcome
import com.example.moonbrewtavern.domain.model.Recipe
import com.example.moonbrewtavern.domain.model.Tavern
import com.example.moonbrewtavern.domain.model.Visitor

/** Default successful outcome for the first-night visitor flow. */
val firstNightOutcome =
  ServingOutcome(
    title = "Новый постоянный гость",
    summary = "Lyra Vale задерживается после последнего глотка и спрашивает, можно ли вернуться завтра.",
    reactionLine = "Именно то, что мне было нужно. Сначала покой, а затем смелость.",
    goldReward = 7,
    reputationReward = 2,
  )

/** Flavor hint shown during brewing for the current starter recipe. */
const val firstNightBrewingHint = "Сохрани вкус чистым и ярким. Тепло должно появиться в послевкусии, а не перебить первый глоток."

/** Narrative metadata for the tavern at the start of the demo. */
val firstNightTavern =
  Tavern(
    name = "Moonbrew Tavern",
    level = 1,
    atmosphere = "Lantern glow, rain on the windows, and a counter that still smells of fresh cedar.",
  )

/** Builds a scenario snapshot for the provided visitor using the shared starter content. */
fun scenarioForVisitor(
  visitor: Visitor,
  initialState: GameState = GameState(day = 3, gold = 12, reputation = 4, phase = GamePhase.Entrance),
  recipe: Recipe = starglowTonicRecipe,
): GameScenario {
  val stockedIngredients =
    firstNightIngredients.map { ingredient ->
      ingredient.copy(stockCount = initialState.ingredientStock[ingredient.id] ?: ingredient.stockCount)
    }
  val stockedById = stockedIngredients.associateBy { it.id }
  val stockedRecipe =
    recipe.copy(
      requiredIngredients =
        recipe.requiredIngredients.map { ingredient ->
          stockedById[ingredient.id] ?: ingredient
        },
    )

  return GameScenario(
    tavern = firstNightTavern,
    initialState = initialState,
    visitor = visitor,
    recipe = stockedRecipe,
    availableIngredients = stockedIngredients,
    brewingHint = firstNightBrewingHint,
    outcome =
      firstNightOutcome.copy(
        summary = "${visitor.name} задерживается после последнего глотка и спрашивает, можно ли вернуться завтра.",
      ),
  )
}

/** Default scenario preview used by tests, previews, and the original single-guest flow. */
val firstNightScenario = scenarioForVisitor(lyraVisitor)
