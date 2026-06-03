package com.example.moonbrewtavern.domain.model

/** Runtime ingredient model used by the current scenario and brewing UI. */
data class Ingredient(
  val id: String,
  val name: String,
  val rarity: IngredientRarity,
  val flavorNote: String,
  val stockCount: Int = 0,
)

/** Coarse rarity tiers for ingredient presentation and balancing. */
enum class IngredientRarity {
  Common,
  Uncommon,
  Rare,
}
