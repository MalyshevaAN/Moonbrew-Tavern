package com.example.moonbrewtavern.domain.model

data class GameScenario(
  val tavern: Tavern,
  val initialState: GameState,
  val visitor: Visitor,
  val recipe: Recipe,
  val brewingHint: String,
  val outcome: ServingOutcome,
)
