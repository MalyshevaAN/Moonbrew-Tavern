package com.example.moonbrewtavern.data.content

import com.example.moonbrewtavern.data.content.ingredients.firstNightIngredients
import com.example.moonbrewtavern.data.content.recipes.starglowTonicRecipe
import com.example.moonbrewtavern.data.content.visitors.lyraVisitor
import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.ServingOutcome
import com.example.moonbrewtavern.domain.model.Tavern
import com.example.moonbrewtavern.domain.model.Visitor

val firstNightOutcome =
  ServingOutcome(
    title = "A promising first regular",
    summary = "Lyra lingers after the last sip, sketches a star-map on a napkin, and asks if she can return tomorrow.",
    reactionLine = "That is exactly what I needed. Quiet first, then courage.",
    goldReward = 7,
    reputationReward = 2,
  )

const val firstNightBrewingHint = "Keep the drink clean and bright. The warmth should arrive late, not overwhelm the first sip."

val firstNightTavern =
  Tavern(
    name = "Moonbrew Tavern",
    level = 1,
    atmosphere = "Lantern glow, rain on the windows, and a counter that still smells of fresh cedar.",
  )

fun scenarioForVisitor(
  visitor: Visitor,
  initialState: GameState = GameState(day = 3, gold = 12, reputation = 4, phase = GamePhase.Entrance),
): GameScenario =
  GameScenario(
    tavern = firstNightTavern,
    initialState = initialState,
    visitor = visitor,
    recipe = starglowTonicRecipe,
    availableIngredients = firstNightIngredients,
    brewingHint = firstNightBrewingHint,
    outcome = firstNightOutcome,
  )

val firstNightScenario = scenarioForVisitor(lyraVisitor)
