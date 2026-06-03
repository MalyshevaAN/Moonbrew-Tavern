package com.example.moonbrewtavern.domain.model

/** Bundles the current visitor, recipe, tavern metadata, and hints for one serving flow. */
data class GameScenario(
  val tavern: Tavern,
  val initialState: GameState,
  val visitor: Visitor,
  val recipe: Recipe,
  val availableIngredients: List<Ingredient>,
  val brewingHint: String,
  val outcome: ServingOutcome,
)
