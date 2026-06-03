package com.example.moonbrewtavern.domain.model

/** Persistent game progress that survives across individual nights and guests. */
data class GameState(
  val day: Int,
  val gold: Int,
  val reputation: Int,
  val phase: GamePhase,
  val unlockedRecipeIds: Set<String> = emptySet(),
  val visitorStates: Map<String, VisitorState> = emptyMap(),
  val tavern: TavernState = TavernState(),
)
