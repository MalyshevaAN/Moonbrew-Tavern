package com.example.moonbrewtavern.data.content.recipes

import com.example.moonbrewtavern.data.content.ingredients.cinderbloom
import com.example.moonbrewtavern.data.content.ingredients.duskSyrup
import com.example.moonbrewtavern.data.content.ingredients.emberzest
import com.example.moonbrewtavern.data.content.ingredients.frostThyme
import com.example.moonbrewtavern.data.content.ingredients.moonmint
import com.example.moonbrewtavern.data.content.ingredients.silverfoam
import com.example.moonbrewtavern.domain.model.Recipe

val herbalMixRecipe =
  Recipe(
    id = "herbal-mix",
    name = "Травяной сбор",
    description = "Нежный вечерний настой с сухими травами и успокаивающим послевкусием.",
    requiredIngredients = listOf(frostThyme, moonmint, duskSyrup),
  )

val gingerGrogRecipe =
  Recipe(
    id = "ginger-grog",
    name = "Имбирный грог",
    description = "Плотный согревающий напиток для холодной дороги и тяжелых разговоров.",
    requiredIngredients = listOf(emberzest, cinderbloom, duskSyrup),
  )

val moonAleRecipe =
  Recipe(
    id = "moon-ale",
    name = "Лунный эль",
    description = "Редкий пенящийся напиток с холодным сиянием и чуть сладковатой дымкой.",
    requiredIngredients = listOf(silverfoam, moonmint, emberzest),
  )
