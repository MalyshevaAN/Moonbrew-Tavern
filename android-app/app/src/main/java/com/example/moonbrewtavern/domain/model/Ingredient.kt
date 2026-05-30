package com.example.moonbrewtavern.domain.model

data class Ingredient(
  val id: String,
  val name: String,
  val rarity: IngredientRarity,
  val flavorNote: String,
  val stockCount: Int = 0,
)

enum class IngredientRarity {
  Common,
  Uncommon,
  Rare,
}
