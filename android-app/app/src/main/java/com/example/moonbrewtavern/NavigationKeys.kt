package com.example.moonbrewtavern

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Entrance queue and street view. */
@Serializable data object Main : NavKey

/** Main tavern room where guests wait, drink, and leave. */
@Serializable data object TavernRoom : NavKey

/** Dialogue scene for the active guest. */
@Serializable data object Dialogue : NavKey

/** Recipe selection screen for the current order. */
@Serializable data object RecipeBook : NavKey

/** Brewing minigame screen. */
@Serializable data object Brewing : NavKey

/** Outcome screen shown after a guest finishes the visit. */
@Serializable data object Result : NavKey
