package com.example.moonbrewtavern.domain.model

data class GameState(
  val day: Int,
  val gold: Int,
  val reputation: Int,
  val phase: GamePhase,
  val unlockedRecipeIds: Set<String> = emptySet(),
  val visitorStates: Map<String, VisitorState> = emptyMap(),
  val tavern: TavernState = TavernState(),
)
