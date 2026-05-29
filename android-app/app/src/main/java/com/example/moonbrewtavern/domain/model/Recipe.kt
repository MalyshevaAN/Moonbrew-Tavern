package com.example.moonbrewtavern.domain.model

data class Recipe(
  val id: String,
  val name: String,
  val description: String,
  val requiredIngredients: List<Ingredient>,
)
