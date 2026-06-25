package com.example.moonbrewtavern.domain.model

import kotlinx.serialization.Serializable

/** Runtime tavern capacity and occupancy state shared across screens. */
@Serializable
data class TavernState(
  val capacity: Int = 3,
  val occupiedSeats: Int = 0,
)
