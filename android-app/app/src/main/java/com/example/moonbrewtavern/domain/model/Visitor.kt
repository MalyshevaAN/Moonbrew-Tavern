package com.example.moonbrewtavern.domain.model

data class Visitor(
  val id: String,
  val name: String,
  val title: String,
  val mood: VisitorMood,
  val openingLine: String,
  val requestLine: String,
  val favoriteFlavor: String,
)

enum class VisitorMood {
  Curious,
  Guarded,
  Warm,
}
