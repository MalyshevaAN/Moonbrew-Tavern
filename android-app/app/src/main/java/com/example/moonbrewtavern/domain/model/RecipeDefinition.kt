package com.example.moonbrewtavern.domain.model

/** Content-layer recipe definition used for unlocks, rewards, and flavor matching. */
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

/** Describes how a recipe becomes available in the long-term progression layer. */
sealed interface UnlockRule {
  data object Default : UnlockRule

  data class Reputation(val min: Int) : UnlockRule

  data class Relationship(val visitorId: String, val min: Int) : UnlockRule

  data class DayReached(val day: Int) : UnlockRule
}
