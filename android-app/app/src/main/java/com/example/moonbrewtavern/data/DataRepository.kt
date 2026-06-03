package com.example.moonbrewtavern.data

import com.example.moonbrewtavern.data.content.ContentCatalog
import com.example.moonbrewtavern.data.content.firstNightOutcome
import com.example.moonbrewtavern.data.content.scenarioForVisitor
import com.example.moonbrewtavern.data.content.visitors.lyraVisitor
import com.example.moonbrewtavern.domain.model.BrewResult
import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.NightPhase
import com.example.moonbrewtavern.domain.model.NightState
import com.example.moonbrewtavern.domain.model.ServingOutcome
import com.example.moonbrewtavern.domain.model.TavernState
import com.example.moonbrewtavern.domain.model.VisitorState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

  fun finishNight()
}

class DefaultDataRepository : DataRepository {
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
        seatedVisitorIds = emptyList(),
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
    _nightState.value =
      NightState(
        queueVisitorIds = ContentCatalog.starterQueueVisitorIds,
        seatedVisitorIds = emptyList(),
        currentVisitorId = null,
        phase = NightPhase.Entrance,
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
    if (visitorId !in snapshot.queueVisitorIds || snapshot.seatedVisitorIds.size >= currentState.tavern.capacity) {
      return
    }

    val newQueue = snapshot.queueVisitorIds - visitorId
    val newSeated = snapshot.seatedVisitorIds + visitorId
    val currentVisitorId = snapshot.currentVisitorId ?: newSeated.firstOrNull()

    _nightState.value =
      snapshot.copy(
        queueVisitorIds = newQueue,
        seatedVisitorIds = newSeated,
        currentVisitorId = currentVisitorId,
      )
    _gameState.update { state ->
      state.copy(
        tavern = state.tavern.copy(occupiedSeats = newSeated.size),
      )
    }
    rebuildScenario()
  }

  override fun rejectVisitor(visitorId: String) {
    _nightState.update { state ->
      state.copy(queueVisitorIds = state.queueVisitorIds - visitorId)
    }
  }

  override fun enterTavern() {
    if (_nightState.value.seatedVisitorIds.isEmpty()) return
    _nightState.update { state ->
      state.copy(
        phase = NightPhase.Tavern,
        currentVisitorId = state.currentVisitorId ?: state.seatedVisitorIds.firstOrNull(),
      )
    }
    _gameState.update { it.copy(phase = GamePhase.Tavern) }
    rebuildScenario()
  }

  override fun returnToEntrance() {
    _nightState.update { it.copy(phase = NightPhase.Entrance) }
    _gameState.update { it.copy(phase = GamePhase.Entrance) }
  }

  override fun startDialogue(visitorId: String) {
    if (visitorId !in _nightState.value.seatedVisitorIds) return
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

    _gameState.update { state ->
      val currentVisitorState = state.visitorStates[visitorId] ?: VisitorState()
      val relationshipGain =
        when {
          result.isExactMatch -> 2
          result.matchedIngredients >= 2 -> 1
          else -> 0
        }

      state.copy(
        gold = state.gold + result.outcome.goldReward,
        reputation = state.reputation + result.outcome.reputationReward,
        phase = GamePhase.Result,
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
    _nightState.update { it.copy(phase = NightPhase.Result) }
    rebuildScenario(visitorId)
    return result
  }

  override fun finishNight() {
    _gameState.update { state ->
      state.copy(
        day = state.day + 1,
        phase = GamePhase.Entrance,
        tavern = state.tavern.copy(occupiedSeats = 0),
      )
    }
    startNight()
  }

  private fun rebuildScenario(visitorId: String? = _nightState.value.currentVisitorId ?: lyraVisitor.id) {
    val activeVisitor = ContentCatalog.visitorsById[visitorId] ?: lyraVisitor
    _data.value = scenarioForVisitor(activeVisitor, _gameState.value)
  }
}
