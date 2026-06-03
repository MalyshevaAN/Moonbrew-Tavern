package com.example.moonbrewtavern.domain.model

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
  val seatedVisitorIds: List<String>
    get() = guests.map(TavernGuest::visitorId)
}

data class TavernGuest(
  val visitorId: String,
  val status: TavernGuestStatus = TavernGuestStatus.WaitingForOrder,
  val drinkRemainingMs: Long = 0L,
  val servedOutcome: ServingOutcome? = null,
  val brewResult: BrewResult? = null,
)

enum class TavernGuestStatus {
  WaitingForOrder,
  Drinking,
  WantsToLeave,
}

enum class NightPhase {
  Entrance,
  Tavern,
  Dialogue,
  RecipeBook,
  Brewing,
  Result,
  Summary,
}
