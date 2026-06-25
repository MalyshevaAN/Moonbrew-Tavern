package com.example.moonbrewtavern.domain.model

import kotlinx.serialization.Serializable

/** Persistent progression state for a visitor across multiple nights. */
@Serializable
data class VisitorState(
  val relationship: Int = 0,
  val timesVisited: Int = 0,
  val unlocked: Boolean = true,
  val storyFlags: Set<String> = emptySet(),
)
