package com.example.moonbrewtavern.domain.model

data class IngredientDefinition(
  val id: String,
  val name: String,
  val rarity: IngredientRarity,
  val flavorTags: Set<FlavorTag> = emptySet(),
  val stockCount: Int = 0,
  val iconRes: Int,
)
