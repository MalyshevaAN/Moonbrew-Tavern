package com.example.moonbrewtavern.domain.model

import kotlinx.serialization.Serializable

/** Narrative and mechanical result of serving a brewed drink to a guest. */
@Serializable
data class ServingOutcome(
  val title: String,
  val summary: String,
  val reactionLine: String,
  val goldReward: Int,
  val reputationReward: Int,
)
