package com.example.moonbrewtavern.data.content.ingredients

import com.example.moonbrewtavern.domain.model.Ingredient
import com.example.moonbrewtavern.domain.model.IngredientRarity

val moonmint =
  Ingredient(
    id = "moonmint",
    name = "Moonmint",
    rarity = IngredientRarity.Common,
    flavorNote = "cool and bright",
    stockCount = 6,
  )

val emberzest =
  Ingredient(
    id = "emberzest",
    name = "Ember Zest",
    rarity = IngredientRarity.Uncommon,
    flavorNote = "warm citrus spark",
    stockCount = 5,
  )

val silverfoam =
  Ingredient(
    id = "silverfoam",
    name = "Silverfoam",
    rarity = IngredientRarity.Rare,
    flavorNote = "soft shimmer on top",
    stockCount = 3,
  )

val duskSyrup =
  Ingredient(
    id = "dusk-syrup",
    name = "Dusk Syrup",
    rarity = IngredientRarity.Common,
    flavorNote = "thick sweetness",
    stockCount = 4,
  )

val frostThyme =
  Ingredient(
    id = "frost-thyme",
    name = "Frost Thyme",
    rarity = IngredientRarity.Uncommon,
    flavorNote = "dry mountain herbal",
    stockCount = 2,
  )

val cinderbloom =
  Ingredient(
    id = "cinderbloom",
    name = "Cinderbloom",
    rarity = IngredientRarity.Rare,
    flavorNote = "smoky floral heat",
    stockCount = 1,
  )

val firstNightIngredients =
  listOf(
    moonmint,
    emberzest,
    silverfoam,
    duskSyrup,
    frostThyme,
    cinderbloom,
  )
