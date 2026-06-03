package com.example.moonbrewtavern.domain.model

/** Flavor metadata that describes the tavern itself for scenario presentation. */
data class Tavern(
  val name: String,
  val level: Int,
  val atmosphere: String,
)
