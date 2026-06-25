package com.example.moonbrewtavern.domain.model

import kotlinx.serialization.Serializable

/** Persistent game progress that survives across individual nights and guests. */
@Serializable
data class GameState(
  val day: Int,
  val gold: Int,
  val reputation: Int,
  val phase: GamePhase,
  val unlockedRecipeIds: Set<String> = emptySet(),
  val ingredientStock: Map<String, Int> = emptyMap(),
  val visitorStates: Map<String, VisitorState> = emptyMap(),
  val tavern: TavernState = TavernState(),
)
