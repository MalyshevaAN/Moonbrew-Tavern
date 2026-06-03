package com.example.moonbrewtavern.domain.model

data class RecipeDefinition(
  val id: String,
  val name: String,
  val description: String,
  val ingredientIds: List<String>,
  val tags: Set<FlavorTag> = emptySet(),
  val unlockRule: UnlockRule = UnlockRule.Default,
  val rewardGold: Int = 0,
  val rewardReputation: Int = 0,
)

sealed interface UnlockRule {
  data object Default : UnlockRule

  data class Reputation(val min: Int) : UnlockRule

  data class Relationship(val visitorId: String, val min: Int) : UnlockRule

  data class DayReached(val day: Int) : UnlockRule
}
