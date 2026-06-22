package com.example.moonbrewtavern.data.content.recipes

import com.example.moonbrewtavern.data.content.ingredients.emberzest
import com.example.moonbrewtavern.data.content.ingredients.moonmint
import com.example.moonbrewtavern.data.content.ingredients.silverfoam
import com.example.moonbrewtavern.domain.model.Recipe

val starglowTonicRecipe =
  Recipe(
    id = "starglow-tonic",
    name = "Звездный тоник",
    description = "Легкий напиток для тех, кому нужно сохранить ясную голову и внутреннее равновесие.",
    requiredIngredients = listOf(moonmint, emberzest, silverfoam),
  )
