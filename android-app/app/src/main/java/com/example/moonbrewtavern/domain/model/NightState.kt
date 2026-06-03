package com.example.moonbrewtavern.domain.model

data class NightState(
  val queueVisitorIds: List<String> = emptyList(),
  val seatedVisitorIds: List<String> = emptyList(),
  val currentVisitorId: String? = null,
  val phase: NightPhase = NightPhase.Entrance,
)

enum class NightPhase {
  Entrance,
  Tavern,
  Dialogue,
  RecipeBook,
  Brewing,
  Result,
  Summary,
}
