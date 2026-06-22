package com.example.moonbrewtavern.data

import com.example.moonbrewtavern.data.content.ContentCatalog
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

/**
 * Central gameplay repository for the tavern loop.
 *
 * It exposes both long-lived progression state and the transient state of the current night.
 */
interface DataRepository {
  /** Convenience accessor for the currently active scenario snapshot. */
  val scenario: GameScenario
    get() = data.value

  /** Scenario data used by the currently visible dialogue, recipe, brewing, and result screens. */
  val data: StateFlow<GameScenario>

  /** Persistent game progress shared across nights. */
  val gameState: StateFlow<GameState>

  /** Live state for the current night. */
  val nightState: StateFlow<NightState>

  /** Most recent brew evaluation shown on the result screen. */
  val lastBrewResult: StateFlow<BrewResult?>

  /** Resets the nightly loop and reinitializes the entrance queue. */
  fun startNight()

  /** Moves a visitor from the entrance queue into the tavern if capacity allows. */
  fun admitVisitor(visitorId: String)

  /** Removes a visitor from the entrance queue for the current night. */
  fun rejectVisitor(visitorId: String)

  /** Starts the timed tavern phase once at least one guest is seated. */
  fun enterTavern()

  /** Returns the UI to the entrance phase without resetting persistent progress. */
  fun returnToEntrance()

  /** Opens the dialogue phase for a waiting guest. */
  fun startDialogue(visitorId: String)

  /** Advances the active flow from dialogue into the recipe book. */
  fun openRecipeBook()

  /** Advances the active flow from recipe selection into brewing. */
  fun openBrewing()

  /** Selects an already unlocked recipe for the current order. */
  fun selectRecipe(recipeId: String)

  /** Purchases a recipe with gold and unlocks it permanently for this session. */
  fun purchaseRecipe(recipeId: String, price: Int): Boolean

  /** Purchases ingredient units and adds them to persistent stock. */
  fun purchaseIngredient(ingredientId: String, quantity: Int, unitPrice: Int): Boolean

  /** Scores a brew against the active recipe without mutating the night state. */
  fun evaluateBrew(selectedIngredientIds: List<String>): BrewResult

  /** Applies a brewed drink to the current guest and returns the resulting score. */
  fun serveBrew(selectedIngredientIds: List<String>): BrewResult

  /** Starts the departure/result flow for a guest who is ready to leave. */
  fun collectGuestDeparture(visitorId: String)

  /** Finalizes departure rewards after the result screen has been acknowledged. */
  fun confirmGuestDeparture()

  /** Ends the current night and advances to the next day. */
  fun finishNight()
}

/** Default in-memory implementation used by the current single-session demo. */
class DefaultDataRepository : DataRepository {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private var nightJob: Job? = null
  private var activeRecipeId: String = ContentCatalog.recipes.first().id

  private val initialVisitorStates = ContentCatalog.visitors.associate { it.id to VisitorState() }

  private val _gameState =
    MutableStateFlow(
      GameState(
        day = 3,
        gold = 12,
        reputation = 4,
        phase = GamePhase.Entrance,
        unlockedRecipeIds = setOf(ContentCatalog.recipes.first().id),
        ingredientStock = ContentCatalog.ingredients.associate { it.id to it.stockCount },
        visitorStates = initialVisitorStates,
        tavern = TavernState(capacity = 3, occupiedSeats = 0),
      ),
    )
  override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

  private val _nightState =
    MutableStateFlow(
      NightState(
        queueVisitorIds = queueForDay(3),
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
        queueVisitorIds = queueForDay(_gameState.value.day),
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
    if (_nightState.value.queueVisitorIds.isEmpty() && _nightState.value.guests.isEmpty()) {
      finishNight()
    }
  }

  override fun enterTavern() {
    val snapshot = _nightState.value
    if (snapshot.guests.isEmpty()) return

    val isOpeningTavern = snapshot.phase == NightPhase.Entrance
    _nightState.value =
      snapshot.copy(
        phase = NightPhase.Tavern,
        pendingVisitorIds = emptyList(),
        remainingNightMs = if (isOpeningTavern) GameLoopConfig.nightDurationMs else snapshot.remainingNightMs,
        elapsedNightMs = if (isOpeningTavern) 0L else snapshot.elapsedNightMs,
        nightEnded = if (isOpeningTavern) false else snapshot.nightEnded,
        currentVisitorId = snapshot.guests.firstOrNull { it.status == TavernGuestStatus.WaitingForOrder }?.visitorId,
      )
    _gameState.update { it.copy(phase = GamePhase.Tavern) }
    rebuildScenario()
    startNightLoop()
  }

  override fun returnToEntrance() {
    stopNightLoop()
    _nightState.update { it.copy(phase = NightPhase.Entrance) }
    _gameState.update { it.copy(phase = GamePhase.Entrance) }
  }

  override fun startDialogue(visitorId: String) {
    val guest = _nightState.value.guests.firstOrNull { it.visitorId == visitorId } ?: return
    if (guest.status != TavernGuestStatus.WaitingForOrder) return

    stopNightLoop()
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

  override fun selectRecipe(recipeId: String) {
    if (recipeId !in _gameState.value.unlockedRecipeIds || recipeId !in ContentCatalog.recipesById) return
    activeRecipeId = recipeId
    rebuildScenario()
  }

  override fun purchaseRecipe(recipeId: String, price: Int): Boolean {
    if (recipeId !in ContentCatalog.recipesById || price < 0) return false
    val snapshot = _gameState.value
    if (recipeId in snapshot.unlockedRecipeIds) {
      selectRecipe(recipeId)
      return true
    }
    if (snapshot.gold < price) return false

    _gameState.value =
      snapshot.copy(
        gold = snapshot.gold - price,
        unlockedRecipeIds = snapshot.unlockedRecipeIds + recipeId,
      )
    activeRecipeId = recipeId
    rebuildScenario()
    return true
  }

  override fun purchaseIngredient(ingredientId: String, quantity: Int, unitPrice: Int): Boolean {
    if (ingredientId !in ContentCatalog.ingredients.map { it.id } || quantity <= 0 || unitPrice < 0) return false
    val totalPrice = quantity * unitPrice
    val snapshot = _gameState.value
    if (snapshot.gold < totalPrice) return false

    _gameState.value =
      snapshot.copy(
        gold = snapshot.gold - totalPrice,
        ingredientStock =
          snapshot.ingredientStock + (
            ingredientId to ((snapshot.ingredientStock[ingredientId] ?: 0) + quantity)
          ),
      )
    rebuildScenario()
    return true
  }

  override fun evaluateBrew(selectedIngredientIds: List<String>): BrewResult {
    val activeScenario = scenario
    val selectedIngredients = selectedIngredientIds.mapNotNull { selectedId -> activeScenario.availableIngredients.firstOrNull { it.id == selectedId } }
    val requiredIds = activeScenario.recipe.requiredIngredients.map { it.id }
    val remainingRequiredIds = requiredIds.toMutableList()
    val matchedIngredients =
      selectedIngredientIds.count { selectedId ->
        val matchedIndex = remainingRequiredIds.indexOf(selectedId)
        if (matchedIndex >= 0) {
          remainingRequiredIds.removeAt(matchedIndex)
          true
        } else {
          false
        }
      }
    val isExactMatch =
      selectedIngredientIds.size == requiredIds.size &&
        selectedIngredientIds.groupingBy { it }.eachCount() == requiredIds.groupingBy { it }.eachCount()

    val outcome =
      when {
        isExactMatch -> activeScenario.outcome
        matchedIngredients >= 2 ->
          ServingOutcome(
            title = "Почти идеальный напиток",
            summary = "${activeScenario.visitor.name} внимательно пробует напиток и кивает. Вкус близок к заказу, хотя послевкусие получилось немного насыщеннее.",
            reactionLine = "Не совсем то, что я представлял, но замысел чувствуется.",
            goldReward = 4,
            reputationReward = 1,
          )
        else ->
          ServingOutcome(
            title = "Первый глоток вышел неровным",
            summary = "${activeScenario.visitor.name} выпивает лишь половину кружки. Старание замечено, но рецепт еще стоит доработать.",
            reactionLine = "В этом есть душа. Остальное придет с опытом.",
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

  override fun serveBrew(selectedIngredientIds: List<String>): BrewResult {
    val visitorId = _nightState.value.currentVisitorId ?: lyraVisitor.id
    val requestedCounts = selectedIngredientIds.groupingBy { it }.eachCount()
    val hasEnoughStock =
      requestedCounts.all { (ingredientId, count) ->
        (_gameState.value.ingredientStock[ingredientId] ?: 0) >= count
      }
    if (!hasEnoughStock) {
      return evaluateBrew(emptyList())
    }
    val result = evaluateBrew(selectedIngredientIds)
    _lastBrewResult.value = result
    _gameState.update { state ->
      state.copy(
        ingredientStock =
          state.ingredientStock.mapValues { (ingredientId, stock) ->
            (stock - (requestedCounts[ingredientId] ?: 0)).coerceAtLeast(0)
          },
      )
    }

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
    startNightLoop()
    return result
  }

  override fun collectGuestDeparture(visitorId: String) {
    val guest = _nightState.value.guests.firstOrNull { it.visitorId == visitorId } ?: return
    if (guest.status != TavernGuestStatus.WantsToLeave) return

    stopNightLoop()
    val departureResult = guest.brewResult ?: unresolvedDepartureFor(visitorId)
    val updatedGuests = _nightState.value.guests.filterNot { it.visitorId == visitorId }

    _lastBrewResult.value = departureResult
    _nightState.update { state ->
      state.copy(
        guests = updatedGuests,
        currentVisitorId = visitorId,
        phase = NightPhase.Result,
      )
    }
    _gameState.update { it.copy(phase = GamePhase.Result) }
    syncOccupiedSeats(updatedGuests.size)
    rebuildScenario(visitorId)
  }

  override fun confirmGuestDeparture() {
    val visitorId = _nightState.value.currentVisitorId ?: return
    val brewResult = _lastBrewResult.value ?: unresolvedDepartureFor(visitorId)
    val outcome = brewResult.outcome
    val currentVisitorState = _gameState.value.visitorStates[visitorId] ?: VisitorState()
    val relationshipGain =
      when {
        brewResult.isExactMatch -> 2
        brewResult.matchedIngredients >= 2 -> 1
        else -> 0
      }

    _gameState.update { state ->
      state.copy(
        gold = state.gold + outcome.goldReward,
        reputation = state.reputation + outcome.reputationReward,
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

    val updatedGuests = _nightState.value.guests
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
    val hasWaitingQueue = _nightState.value.queueVisitorIds.isNotEmpty()
    _gameState.update {
      it.copy(
        phase = if (updatedGuests.isEmpty()) GamePhase.Entrance else GamePhase.Tavern,
      )
    }
    syncOccupiedSeats(updatedGuests.size)
    _lastBrewResult.value = null
    rebuildScenario(nextCurrentVisitorId)

    if (updatedGuests.isEmpty() && !hasWaitingQueue) {
      finishNight()
    } else if (updatedGuests.isEmpty()) {
      _nightState.update { state ->
        state.copy(currentVisitorId = null, phase = NightPhase.Entrance)
      }
      rebuildScenario()
    } else if (!_nightState.value.nightEnded) {
      startNightLoop()
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
    if (snapshot.phase != NightPhase.Tavern || snapshot.nightEnded) return

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
          title = "${visitor.name} уходит без напитка",
          summary = "${visitor.name} не дождался напитка и покинул таверну с коротким прощальным кивком.",
          reactionLine = "Возможно, в другой вечер.",
          goldReward = 0,
          reputationReward = -1,
        ),
    )
  }

  private fun rebuildScenario(visitorId: String? = _nightState.value.currentVisitorId ?: lyraVisitor.id) {
    val activeVisitor = ContentCatalog.visitorsById[visitorId] ?: lyraVisitor
    val activeRecipe = ContentCatalog.recipesById[activeRecipeId] ?: ContentCatalog.recipes.first()
    _data.value = scenarioForVisitor(activeVisitor, _gameState.value, activeRecipe)
  }

  private companion object {
    fun queueForDay(day: Int): List<String> {
      val visitors = ContentCatalog.starterQueueVisitorIds
      if (visitors.isEmpty()) return emptyList()
      val shift = day % visitors.size
      return visitors.drop(shift) + visitors.take(shift)
    }
  }
}
