package com.example.moonbrewtavern.domain.model

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

data class VisitorAssets(
  val queueRes: Int,
  val tavernSeatRes: Int,
  val dialoguePortraitRes: Int,
  val resultPortraitRes: Int,
)

data class VisitorRequest(
  val id: String,
  val text: String,
  val desiredTags: Set<FlavorTag>,
  val forbiddenTags: Set<FlavorTag> = emptySet(),
)

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
