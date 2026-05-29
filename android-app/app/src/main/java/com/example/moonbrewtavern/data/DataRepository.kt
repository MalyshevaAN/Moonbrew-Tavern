package com.example.moonbrewtavern.data

import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.Ingredient
import com.example.moonbrewtavern.domain.model.IngredientRarity
import com.example.moonbrewtavern.domain.model.Recipe
import com.example.moonbrewtavern.domain.model.BrewResult
import com.example.moonbrewtavern.domain.model.ServingOutcome
import com.example.moonbrewtavern.domain.model.Tavern
import com.example.moonbrewtavern.domain.model.Visitor
import com.example.moonbrewtavern.domain.model.VisitorMood
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface DataRepository {
  val scenario: GameScenario
  val data: Flow<GameScenario>

  fun evaluateBrew(selectedIngredientIds: Set<String>): BrewResult
}

class DefaultDataRepository : DataRepository {
  override val scenario: GameScenario = firstNightScenario()
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

  private fun firstNightScenario(): GameScenario {
    val moonmint = Ingredient(id = "moonmint", name = "Moonmint", rarity = IngredientRarity.Common, flavorNote = "cool and bright")
    val emberzest = Ingredient(id = "emberzest", name = "Ember Zest", rarity = IngredientRarity.Uncommon, flavorNote = "warm citrus spark")
    val silverfoam = Ingredient(id = "silverfoam", name = "Silverfoam", rarity = IngredientRarity.Rare, flavorNote = "soft shimmer on top")
    val duskSyrup = Ingredient(id = "dusk-syrup", name = "Dusk Syrup", rarity = IngredientRarity.Common, flavorNote = "thick sweetness")
    val frostThyme = Ingredient(id = "frost-thyme", name = "Frost Thyme", rarity = IngredientRarity.Uncommon, flavorNote = "dry mountain herbal")
    val cinderBloom = Ingredient(id = "cinderbloom", name = "Cinderbloom", rarity = IngredientRarity.Rare, flavorNote = "smoky floral heat")

    return GameScenario(
      tavern = Tavern(name = "Moonbrew Tavern", level = 1, atmosphere = "Lantern glow, rain on the windows, and a counter that still smells of fresh cedar."),
      initialState = GameState(day = 3, gold = 12, reputation = 4, phase = GamePhase.Tavern),
      visitor =
        Visitor(
          id = "lyra",
          name = "Lyra Vale",
          title = "Картограф Северного тракта",
          mood = VisitorMood.Curious,
          openingLine = "Так вот о какой таверне шепчутся путники, когда дорога становится особенно долгой.",
          requestLine = "Если рука у тебя твердая, приготовь мне что-нибудь ясное по вкусу, с легким теплым послевкусием.",
          favoriteFlavor = "прохладные травы с теплым послевкусием",
        ),
      recipe =
        Recipe(
          id = "starglow-tonic",
          name = "Starglow Tonic",
          description = "A focused tonic for travelers who need calm nerves and a bright mind.",
          requiredIngredients = listOf(moonmint, emberzest, silverfoam),
        ),
      availableIngredients = listOf(moonmint, emberzest, silverfoam, duskSyrup, frostThyme, cinderBloom),
      brewingHint = "Keep the drink clean and bright. The warmth should arrive late, not overwhelm the first sip.",
      outcome =
        ServingOutcome(
          title = "A promising first regular",
          summary = "Lyra lingers after the last sip, sketches a star-map on a napkin, and asks if she can return tomorrow.",
          reactionLine = "That is exactly what I needed. Quiet first, then courage.",
          goldReward = 7,
          reputationReward = 2,
        ),
    )
  }
}
