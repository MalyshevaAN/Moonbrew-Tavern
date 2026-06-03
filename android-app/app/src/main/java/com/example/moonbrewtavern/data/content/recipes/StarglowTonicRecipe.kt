package com.example.moonbrewtavern.data.content.recipes

import com.example.moonbrewtavern.data.content.ingredients.emberzest
import com.example.moonbrewtavern.data.content.ingredients.moonmint
import com.example.moonbrewtavern.data.content.ingredients.silverfoam
import com.example.moonbrewtavern.domain.model.Recipe

val starglowTonicRecipe =
  Recipe(
    id = "starglow-tonic",
    name = "Starglow Tonic",
    description = "A focused tonic for travelers who need calm nerves and a bright mind.",
    requiredIngredients = listOf(moonmint, emberzest, silverfoam),
  )
