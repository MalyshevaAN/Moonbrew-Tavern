package com.example.moonbrewtavern.domain.model

/** Content definition for a reusable visitor archetype. */
data class VisitorDefinition(
  val id: String,
  val name: String,
  val title: String,
  val mood: VisitorMood,
  val favoriteTags: Set<FlavorTag>,
  val dislikedTags: Set<FlavorTag> = emptySet(),
  val preferredRecipeIds: Set<String> = emptySet(),
  val requestPool: List<VisitorRequest> = emptyList(),
  val assets: VisitorAssets,
)

/** Asset references for the same visitor across multiple game screens. */
data class VisitorAssets(
  val queueRes: Int,
  val tavernSeatRes: Int,
  val dialoguePortraitRes: Int,
  val resultPortraitRes: Int,
)

/** One possible order prompt that can be selected for a visitor. */
data class VisitorRequest(
  val id: String,
  val text: String,
  val desiredTags: Set<FlavorTag>,
  val forbiddenTags: Set<FlavorTag> = emptySet(),
)

/** Flavor dimensions used for recipe matching and guest preferences. */
enum class FlavorTag {
  Fresh,
  Warm,
  Sweet,
  Herbal,
  Smoky,
  Bitter,
  Bright,
  Strange,
}
