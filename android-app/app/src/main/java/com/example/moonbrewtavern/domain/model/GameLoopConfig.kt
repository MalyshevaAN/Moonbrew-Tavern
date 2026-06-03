package com.example.moonbrewtavern.domain.model

object GameLoopConfig {
  const val nightDurationMs: Long = 5 * 60 * 1000L
  const val guestDrinkDurationMs: Long = 10_000L
  const val guestArrivalIntervalMs: Long = 12_000L
  const val nightTickMs: Long = 1_000L
}
