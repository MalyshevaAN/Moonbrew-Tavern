package com.example.moonbrewtavern.domain.model

import kotlinx.serialization.Serializable

/** High-level navigation phases exposed to the UI. */
@Serializable
enum class GamePhase {
  Entrance,
  Tavern,
  Dialogue,
  RecipeBook,
  Brewing,
  Result,
}
