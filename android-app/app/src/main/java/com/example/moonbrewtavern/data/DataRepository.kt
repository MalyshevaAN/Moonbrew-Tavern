package com.example.moonbrewtavern.data

import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.Ingredient
import com.example.moonbrewtavern.domain.model.IngredientRarity
import com.example.moonbrewtavern.domain.model.Recipe
import com.example.moonbrewtavern.domain.model.ServingOutcome
import com.example.moonbrewtavern.domain.model.Tavern
import com.example.moonbrewtavern.domain.model.Visitor
import com.example.moonbrewtavern.domain.model.VisitorMood
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface DataRepository {
  val scenario: GameScenario
  val data: Flow<GameScenario>
}

class DefaultDataRepository : DataRepository {
  override val scenario: GameScenario = firstNightScenario()
  override val data: Flow<GameScenario> = flowOf(scenario)

  private fun firstNightScenario(): GameScenario {
    val moonmint = Ingredient(id = "moonmint", name = "Moonmint", rarity = IngredientRarity.Common, flavorNote = "cool and bright")
    val emberzest = Ingredient(id = "emberzest", name = "Ember Zest", rarity = IngredientRarity.Uncommon, flavorNote = "warm citrus spark")
    val silverfoam = Ingredient(id = "silverfoam", name = "Silverfoam", rarity = IngredientRarity.Rare, flavorNote = "soft shimmer on top")

    return GameScenario(
      tavern = Tavern(name = "Moonbrew Tavern", level = 1, atmosphere = "Lantern glow, rain on the windows, and a counter that still smells of fresh cedar."),
      initialState = GameState(day = 3, gold = 12, reputation = 4, phase = GamePhase.Tavern),
      visitor =
        Visitor(
          id = "lyra",
          name = "Lyra Vale",
          title = "Cartographer of the North Road",
          mood = VisitorMood.Curious,
          openingLine = "So this is the tavern people whisper about when the roads get lonely.",
          requestLine = "If you have a steady hand, brew me something clear-headed with a little warmth under it.",
          favoriteFlavor = "cool herbs with a warm finish",
        ),
      recipe =
        Recipe(
          id = "starglow-tonic",
          name = "Starglow Tonic",
          description = "A focused tonic for travelers who need calm nerves and a bright mind.",
          requiredIngredients = listOf(moonmint, emberzest, silverfoam),
        ),
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
