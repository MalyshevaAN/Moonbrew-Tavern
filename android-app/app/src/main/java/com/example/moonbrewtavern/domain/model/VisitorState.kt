package com.example.moonbrewtavern.domain.model

data class VisitorState(
  val relationship: Int = 0,
  val timesVisited: Int = 0,
  val unlocked: Boolean = true,
  val storyFlags: Set<String> = emptySet(),
)
