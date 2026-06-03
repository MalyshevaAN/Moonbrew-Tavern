package com.example.moonbrewtavern.data

import com.example.moonbrewtavern.data.content.firstNightScenario
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.BrewResult
import com.example.moonbrewtavern.domain.model.ServingOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface DataRepository {
  val scenario: GameScenario
  val data: Flow<GameScenario>

  fun evaluateBrew(selectedIngredientIds: Set<String>): BrewResult
}

class DefaultDataRepository : DataRepository {
  override val scenario: GameScenario = firstNightScenario
  override val data: Flow<GameScenario> = flowOf(scenario)

  override fun evaluateBrew(selectedIngredientIds: Set<String>): BrewResult {
    val selectedIngredients = scenario.availableIngredients.filter { it.id in selectedIngredientIds }
    val requiredIds = scenario.recipe.requiredIngredients.mapTo(linkedSetOf()) { it.id }
    val matchedIngredients = selectedIngredients.count { it.id in requiredIds }
    val isExactMatch = selectedIngredientIds.size == requiredIds.size && selectedIngredientIds == requiredIds

    val outcome =
      when {
        isExactMatch -> scenario.outcome
        matchedIngredients >= 2 ->
          ServingOutcome(
            title = "A steady hand, if not a perfect one",
            summary = "Lyra studies the glass for a beat, then nods. The drink lands close enough to earn respect, even if the finish strays a little richer than she hoped.",
            reactionLine = "Not quite the shape I imagined, but the intention is there.",
            goldReward = 4,
            reputationReward = 1,
          )
        else ->
          ServingOutcome(
            title = "A rough first pour",
            summary = "Lyra finishes only half the cup. She is polite, but you can feel the tavern still needs time before it becomes a place travelers trust by instinct.",
            reactionLine = "There is heart in it. The rest can come later.",
            goldReward = 1,
            reputationReward = 0,
          )
      }

    return BrewResult(
      selectedIngredients = selectedIngredients,
      matchedIngredients = matchedIngredients,
      isExactMatch = isExactMatch,
      outcome = outcome,
    )
  }
}
