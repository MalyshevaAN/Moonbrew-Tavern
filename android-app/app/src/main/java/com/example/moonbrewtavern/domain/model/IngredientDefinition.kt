package com.example.moonbrewtavern.domain.model

/** Content-layer description of an ingredient that can be reused across nights. */
data class IngredientDefinition(
  val id: String,
  val name: String,
  val rarity: IngredientRarity,
  val flavorTags: Set<FlavorTag> = emptySet(),
  val stockCount: Int = 0,
  val iconRes: Int,
)
