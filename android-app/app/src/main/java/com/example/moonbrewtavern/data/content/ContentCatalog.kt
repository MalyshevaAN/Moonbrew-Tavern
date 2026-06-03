package com.example.moonbrewtavern.data.content

import com.example.moonbrewtavern.data.content.ingredients.firstNightIngredients
import com.example.moonbrewtavern.data.content.recipes.starglowTonicRecipe
import com.example.moonbrewtavern.data.content.visitors.lyraVisitor

object ContentCatalog {
  val visitors = listOf(lyraVisitor)
  val recipes = listOf(starglowTonicRecipe)
  val ingredients = firstNightIngredients
}
