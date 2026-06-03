package com.example.moonbrewtavern.domain.model

/** Runtime recipe model used by the current brewing flow. */
data class Recipe(
  val id: String,
  val name: String,
  val description: String,
  val requiredIngredients: List<Ingredient>,
)
