package com.example.moonbrewtavern.domain.model

import kotlinx.serialization.Serializable

/** Summary snapshot shown between the end of one night and the start of the next. */
@Serializable
data class NightSummary(
  val completedDay: Int,
  val nextDay: Int,
  val gold: Int,
  val reputation: Int,
  val unlockedRecipes: Int,
  val relationshipsTracked: Int,
)
