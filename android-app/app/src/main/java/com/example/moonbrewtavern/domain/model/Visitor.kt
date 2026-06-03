package com.example.moonbrewtavern.domain.model

/** Runtime guest model used by the currently active dialogue and result screens. */
data class Visitor(
  val id: String,
  val name: String,
  val title: String,
  val mood: VisitorMood,
  val openingLine: String,
  val requestLine: String,
  val favoriteFlavor: String,
)

/** Lightweight mood buckets for visitor presentation. */
enum class VisitorMood {
  Curious,
  Guarded,
  Warm,
}
