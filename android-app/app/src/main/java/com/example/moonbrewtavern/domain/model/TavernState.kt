package com.example.moonbrewtavern.domain.model

/** Runtime tavern capacity and occupancy state shared across screens. */
data class TavernState(
  val capacity: Int = 3,
  val occupiedSeats: Int = 0,
)
