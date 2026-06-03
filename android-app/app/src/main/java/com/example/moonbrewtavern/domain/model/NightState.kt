package com.example.moonbrewtavern.domain.model

/** Runtime state for a single tavern night, including queue, guests, and timers. */
data class NightState(
  val queueVisitorIds: List<String> = emptyList(),
  val pendingVisitorIds: List<String> = emptyList(),
  val guests: List<TavernGuest> = emptyList(),
  val currentVisitorId: String? = null,
  val phase: NightPhase = NightPhase.Entrance,
  val remainingNightMs: Long = GameLoopConfig.nightDurationMs,
  val elapsedNightMs: Long = 0L,
  val nightEnded: Boolean = false,
) {
  /** Convenience projection of seated guest ids in their current table order. */
  val seatedVisitorIds: List<String>
    get() = guests.map(TavernGuest::visitorId)
}

/** Tracks a guest currently attached to the live tavern floor. */
data class TavernGuest(
  val visitorId: String,
  val status: TavernGuestStatus = TavernGuestStatus.WaitingForOrder,
  val drinkRemainingMs: Long = 0L,
  val servedOutcome: ServingOutcome? = null,
  val brewResult: BrewResult? = null,
)

/** Service lifecycle for a guest seated inside the tavern. */
enum class TavernGuestStatus {
  WaitingForOrder,
  Drinking,
  WantsToLeave,
}

/** More granular phase model for the nightly tavern loop. */
enum class NightPhase {
  Entrance,
  Tavern,
  Dialogue,
  RecipeBook,
  Brewing,
  Result,
  Summary,
}
