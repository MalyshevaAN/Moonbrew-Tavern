package com.example.moonbrewtavern.data

import com.example.moonbrewtavern.data.content.ContentCatalog
import com.example.moonbrewtavern.data.content.firstNightOutcome
import com.example.moonbrewtavern.data.content.scenarioForVisitor
import com.example.moonbrewtavern.data.content.visitors.lyraVisitor
import com.example.moonbrewtavern.domain.model.BrewResult
import com.example.moonbrewtavern.domain.model.GameLoopConfig
import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.NightPhase
import com.example.moonbrewtavern.domain.model.NightState
import com.example.moonbrewtavern.domain.model.ServingOutcome
import com.example.moonbrewtavern.domain.model.TavernGuest
import com.example.moonbrewtavern.domain.model.TavernGuestStatus
import com.example.moonbrewtavern.domain.model.TavernState
import com.example.moonbrewtavern.domain.model.VisitorState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface DataRepository {
  val scenario: GameScenario
    get() = data.value

  val data: StateFlow<GameScenario>
  val gameState: StateFlow<GameState>
  val nightState: StateFlow<NightState>
  val lastBrewResult: StateFlow<BrewResult?>

  fun startNight()

  fun admitVisitor(visitorId: String)

  fun rejectVisitor(visitorId: String)

  fun enterTavern()

  fun returnToEntrance()

  fun startDialogue(visitorId: String)

  fun openRecipeBook()

  fun openBrewing()

  fun evaluateBrew(selectedIngredientIds: Set<String>): BrewResult

  fun serveBrew(selectedIngredientIds: Set<String>): BrewResult

  fun collectGuestDeparture(visitorId: String)

  fun confirmGuestDeparture()

  fun finishNight()
}

class DefaultDataRepository : DataRepository {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private var nightJob: Job? = null

  private val initialVisitorStates = ContentCatalog.visitors.associate { it.id to VisitorState() }

  private val _gameState =
    MutableStateFlow(
      GameState(
        day = 3,
        gold = 12,
        reputation = 4,
        phase = GamePhase.Entrance,
        unlockedRecipeIds = setOf(ContentCatalog.recipes.first().id),
        visitorStates = initialVisitorStates,
        tavern = TavernState(capacity = 3, occupiedSeats = 0),
      ),
    )
  override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

  private val _nightState =
    MutableStateFlow(
      NightState(
        queueVisitorIds = ContentCatalog.starterQueueVisitorIds,
        pendingVisitorIds = emptyList(),
        guests = emptyList(),
        currentVisitorId = null,
        phase = NightPhase.Entrance,
      ),
    )
  override val nightState: StateFlow<NightState> = _nightState.asStateFlow()

  private val _data = MutableStateFlow(scenarioForVisitor(lyraVisitor, _gameState.value))
  override val data: StateFlow<GameScenario> = _data.asStateFlow()

  private val _lastBrewResult = MutableStateFlow<BrewResult?>(null)
  override val lastBrewResult: StateFlow<BrewResult?> = _lastBrewResult.asStateFlow()

  override fun startNight() {
    stopNightLoop()
    _nightState.value =
      NightState(
        queueVisitorIds = ContentCatalog.starterQueueVisitorIds,
        pendingVisitorIds = emptyList(),
        guests = emptyList(),
        currentVisitorId = null,
        phase = NightPhase.Entrance,
        remainingNightMs = GameLoopConfig.nightDurationMs,
        elapsedNightMs = 0L,
        nightEnded = false,
      )
    _gameState.update { state ->
      state.copy(
        phase = GamePhase.Entrance,
        tavern = state.tavern.copy(occupiedSeats = 0),
      )
    }
    _lastBrewResult.value = null
    rebuildScenario()
  }

  override fun admitVisitor(visitorId: String) {
    val snapshot = _nightState.value
    val currentState = _gameState.value
    if (
      visitorId !in snapshot.queueVisitorIds ||
      snapshot.guests.size >= currentState.tavern.capacity ||
      snapshot.guests.any { it.visitorId == visitorId }
    ) {
      return
    }

    val updatedGuests = snapshot.guests + TavernGuest(visitorId = visitorId)
    _nightState.value =
      snapshot.copy(
        queueVisitorIds = snapshot.queueVisitorIds - visitorId,
        guests = updatedGuests,
        currentVisitorId = snapshot.currentVisitorId ?: updatedGuests.firstOrNull()?.visitorId,
      )
    syncOccupiedSeats(updatedGuests.size)
    rebuildScenario()
  }

  override fun rejectVisitor(visitorId: String) {
    _nightState.update { state ->
      state.copy(queueVisitorIds = state.queueVisitorIds - visitorId)
    }
  }

  override fun enterTavern() {
    val snapshot = _nightState.value
    if (snapshot.guests.isEmpty()) return

    _nightState.value =
      snapshot.copy(
        phase = NightPhase.Tavern,
        pendingVisitorIds = emptyList(),
        remainingNightMs = GameLoopConfig.nightDurationMs,
        elapsedNightMs = 0L,
        nightEnded = false,
        currentVisitorId = snapshot.guests.firstOrNull { it.status == TavernGuestStatus.WaitingForOrder }?.visitorId,
      )
    _gameState.update { it.copy(phase = GamePhase.Tavern) }
    rebuildScenario()
    startNightLoop()
  }

  override fun returnToEntrance() {
    _nightState.update { it.copy(phase = NightPhase.Entrance) }
    _gameState.update { it.copy(phase = GamePhase.Entrance) }
  }

  override fun startDialogue(visitorId: String) {
    val guest = _nightState.value.guests.firstOrNull { it.visitorId == visitorId } ?: return
    if (guest.status != TavernGuestStatus.WaitingForOrder) return

    _nightState.update { state ->
      state.copy(currentVisitorId = visitorId, phase = NightPhase.Dialogue)
    }
    _gameState.update { it.copy(phase = GamePhase.Dialogue) }
    rebuildScenario(visitorId)
  }

  override fun openRecipeBook() {
    _nightState.update { it.copy(phase = NightPhase.RecipeBook) }
    _gameState.update { it.copy(phase = GamePhase.RecipeBook) }
    rebuildScenario()
  }

  override fun openBrewing() {
    _nightState.update { it.copy(phase = NightPhase.Brewing) }
    _gameState.update { it.copy(phase = GamePhase.Brewing) }
    rebuildScenario()
  }

  override fun evaluateBrew(selectedIngredientIds: Set<String>): BrewResult {
    val activeScenario = scenario
    val selectedIngredients = activeScenario.availableIngredients.filter { it.id in selectedIngredientIds }
    val requiredIds = activeScenario.recipe.requiredIngredients.mapTo(linkedSetOf()) { it.id }
    val matchedIngredients = selectedIngredients.count { it.id in requiredIds }
    val isExactMatch = selectedIngredientIds.size == requiredIds.size && selectedIngredientIds == requiredIds

    val outcome =
      when {
        isExactMatch -> activeScenario.outcome
        matchedIngredients >= 2 ->
          ServingOutcome(
            title = "A steady hand, if not a perfect one",
            summary = "${activeScenario.visitor.name} studies the glass for a beat, then nods. The drink lands close enough to earn respect, even if the finish strays a little richer than expected.",
            reactionLine = "Not quite the shape I imagined, but the intention is there.",
            goldReward = 4,
            reputationReward = 1,
          )
        else ->
          ServingOutcome(
            title = "A rough first pour",
            summary = "${activeScenario.visitor.name} finishes only half the cup. The effort is noticed, but the tavern still feels like a promise more than a certainty.",
            reactionLine = "There is heart in it. The rest can come later.",
            goldReward = 1,
            reputationReward = 0,
          )
      }

    return BrewResult(
      selectedIngredients = selectedIngredients,
      matchedIngredients = matchedIngredients,
      isExactMatch = isExactMatch,
      outcome = outcome,
    )
  }

  override fun serveBrew(selectedIngredientIds: Set<String>): BrewResult {
    val visitorId = _nightState.value.currentVisitorId ?: lyraVisitor.id
    val result = evaluateBrew(selectedIngredientIds)
    _lastBrewResult.value = result

    val updatedGuests =
      _nightState.value.guests.map { guest ->
        if (guest.visitorId == visitorId) {
          guest.copy(
            status = TavernGuestStatus.Drinking,
            drinkRemainingMs = GameLoopConfig.guestDrinkDurationMs,
            servedOutcome = result.outcome,
            brewResult = result,
          )
        } else {
          guest
        }
      }

    val nextCurrentVisitorId = updatedGuests.firstOrNull { it.status == TavernGuestStatus.WaitingForOrder }?.visitorId

    _nightState.update { state ->
      state.copy(
        guests = updatedGuests,
        currentVisitorId = nextCurrentVisitorId,
        phase = NightPhase.Tavern,
      )
    }
    _gameState.update { it.copy(phase = GamePhase.Tavern) }
    rebuildScenario(nextCurrentVisitorId)
    return result
  }

  override fun collectGuestDeparture(visitorId: String) {
    val guest = _nightState.value.guests.firstOrNull { it.visitorId == visitorId } ?: return
    if (guest.status != TavernGuestStatus.WantsToLeave) return

    val departureResult = guest.brewResult ?: unresolvedDepartureFor(visitorId)
    _lastBrewResult.value = departureResult
    _nightState.update { state ->
      state.copy(
        currentVisitorId = visitorId,
        phase = NightPhase.Result,
      )
    }
    _gameState.update { it.copy(phase = GamePhase.Result) }
    rebuildScenario(visitorId)
  }

  override fun confirmGuestDeparture() {
    val visitorId = _nightState.value.currentVisitorId ?: return
    val guest = _nightState.value.guests.firstOrNull { it.visitorId == visitorId } ?: return
    if (guest.status != TavernGuestStatus.WantsToLeave) return

    val brewResult = guest.brewResult
    val outcome = guest.servedOutcome
    val currentVisitorState = _gameState.value.visitorStates[visitorId] ?: VisitorState()
    val relationshipGain =
      when {
        brewResult?.isExactMatch == true -> 2
        brewResult?.matchedIngredients ?: 0 >= 2 -> 1
        else -> 0
      }

    _gameState.update { state ->
      state.copy(
        gold = state.gold + (outcome?.goldReward ?: 0),
        reputation = state.reputation + (outcome?.reputationReward ?: 0),
        visitorStates =
          state.visitorStates + (
            visitorId to
              currentVisitorState.copy(
                relationship = currentVisitorState.relationship + relationshipGain,
                timesVisited = currentVisitorState.timesVisited + 1,
              )
          ),
      )
    }

    val updatedGuests = _nightState.value.guests.filterNot { it.visitorId == visitorId }
    val nextCurrentVisitorId =
      updatedGuests.firstOrNull { it.status == TavernGuestStatus.WaitingForOrder }?.visitorId
        ?: updatedGuests.firstOrNull { it.status == TavernGuestStatus.WantsToLeave }?.visitorId

    _nightState.update { state ->
      state.copy(
        guests = updatedGuests,
        currentVisitorId = nextCurrentVisitorId,
        phase = if (updatedGuests.isEmpty() && state.nightEnded) NightPhase.Summary else NightPhase.Tavern,
      )
    }
    _gameState.update {
      it.copy(
        phase = if (_nightState.value.nightEnded && updatedGuests.isEmpty()) GamePhase.Entrance else GamePhase.Tavern,
      )
    }
    syncOccupiedSeats(updatedGuests.size)
    _lastBrewResult.value = null
    rebuildScenario(nextCurrentVisitorId)

    if (_nightState.value.nightEnded && updatedGuests.isEmpty()) {
      finishNight()
    }
  }

  override fun finishNight() {
    stopNightLoop()
    _gameState.update { state ->
      state.copy(
        day = state.day + 1,
        phase = GamePhase.Entrance,
        tavern = state.tavern.copy(occupiedSeats = 0),
      )
    }
    startNight()
  }

  private fun startNightLoop() {
    stopNightLoop()
    nightJob =
      scope.launch {
        while (isActive && !_nightState.value.nightEnded) {
          delay(GameLoopConfig.nightTickMs)
          advanceNight()
        }
      }
  }

  private fun stopNightLoop() {
    nightJob?.cancel()
    nightJob = null
  }

  private fun advanceNight() {
    val snapshot = _nightState.value
    if (snapshot.phase == NightPhase.Entrance || snapshot.nightEnded) return

    val elapsed = snapshot.elapsedNightMs + GameLoopConfig.nightTickMs
    val remaining = (snapshot.remainingNightMs - GameLoopConfig.nightTickMs).coerceAtLeast(0L)
    var updatedGuests =
      snapshot.guests.map { guest ->
        if (guest.status == TavernGuestStatus.Drinking) {
          val drinkRemaining = (guest.drinkRemainingMs - GameLoopConfig.nightTickMs).coerceAtLeast(0L)
          if (drinkRemaining == 0L) {
            guest.copy(status = TavernGuestStatus.WantsToLeave, drinkRemainingMs = 0L)
          } else {
            guest.copy(drinkRemainingMs = drinkRemaining)
          }
        } else {
          guest
        }
      }

    val didNightEnd = remaining == 0L
    if (didNightEnd) {
      updatedGuests =
        updatedGuests.map { guest ->
          if (guest.status == TavernGuestStatus.Drinking || guest.status == TavernGuestStatus.WaitingForOrder) {
            guest.copy(status = TavernGuestStatus.WantsToLeave, drinkRemainingMs = 0L)
          } else {
            guest
          }
        }
    }

    val nextCurrentVisitorId =
      updatedGuests.firstOrNull { it.status == TavernGuestStatus.WaitingForOrder }?.visitorId
        ?: updatedGuests.firstOrNull { it.status == TavernGuestStatus.WantsToLeave }?.visitorId

    _nightState.value =
      snapshot.copy(
        pendingVisitorIds = snapshot.pendingVisitorIds,
        guests = updatedGuests,
        currentVisitorId = nextCurrentVisitorId,
        remainingNightMs = remaining,
        elapsedNightMs = elapsed,
        nightEnded = didNightEnd,
        phase = if (didNightEnd) NightPhase.Summary else NightPhase.Tavern,
      )
    syncOccupiedSeats(updatedGuests.size)
    rebuildScenario(nextCurrentVisitorId)
  }

  private fun syncOccupiedSeats(occupiedSeats: Int) {
    _gameState.update { state ->
      state.copy(tavern = state.tavern.copy(occupiedSeats = occupiedSeats))
    }
  }

  private fun unresolvedDepartureFor(visitorId: String): BrewResult {
    val visitor = ContentCatalog.visitorsById[visitorId] ?: lyraVisitor
    return BrewResult(
      selectedIngredients = emptyList(),
      matchedIngredients = 0,
      isExactMatch = false,
      outcome =
        ServingOutcome(
          title = "${visitor.name} leaves unsatisfied",
          summary = "${visitor.name} waited through the last stretch of the night, then rose from the table with an apologetic nod and no drink to remember the tavern by.",
          reactionLine = "Perhaps another evening.",
          goldReward = 0,
          reputationReward = -1,
        ),
    )
  }

  private fun rebuildScenario(visitorId: String? = _nightState.value.currentVisitorId ?: lyraVisitor.id) {
    val activeVisitor = ContentCatalog.visitorsById[visitorId] ?: lyraVisitor
    _data.value = scenarioForVisitor(activeVisitor, _gameState.value)
  }
}
