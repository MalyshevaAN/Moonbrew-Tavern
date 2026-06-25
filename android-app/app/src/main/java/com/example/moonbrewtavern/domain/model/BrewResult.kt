package com.example.moonbrewtavern.domain.model

import kotlinx.serialization.Serializable

/** Captures how well the brewed drink matched the active guest request. */
@Serializable
data class BrewResult(
  val selectedIngredients: List<Ingredient>,
  val matchedIngredients: Int,
  val isExactMatch: Boolean,
  val outcome: ServingOutcome,
)
