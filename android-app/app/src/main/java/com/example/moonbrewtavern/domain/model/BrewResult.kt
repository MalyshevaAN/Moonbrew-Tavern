package com.example.moonbrewtavern.domain.model

/** Captures how well the brewed drink matched the active guest request. */
data class BrewResult(
  val selectedIngredients: List<Ingredient>,
  val matchedIngredients: Int,
  val isExactMatch: Boolean,
  val outcome: ServingOutcome,
)
