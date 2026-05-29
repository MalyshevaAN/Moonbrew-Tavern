package com.example.moonbrewtavern.domain.model

data class BrewResult(
  val selectedIngredients: List<Ingredient>,
  val matchedIngredients: Int,
  val isExactMatch: Boolean,
  val outcome: ServingOutcome,
)
