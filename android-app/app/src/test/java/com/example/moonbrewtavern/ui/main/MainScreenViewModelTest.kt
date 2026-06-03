package com.example.moonbrewtavern.ui.main

import com.example.moonbrewtavern.data.DataRepository
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.domain.model.BrewResult
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.NightState
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {
  @Test
  fun uiState_initiallyLoading() = runTest {
    val viewModel = MainScreenViewModel(FakeMyModelRepository())
    assertTrue(viewModel.uiState.first() is MainScreenUiState.Loading)
  }

  @Test
  fun uiState_eventuallyEmitsScenario() = runTest {
    val viewModel = MainScreenViewModel(FakeMyModelRepository())
    val successState = viewModel.uiState.first { it is MainScreenUiState.Success } as MainScreenUiState.Success

    assertTrue(successState.scenario.recipe.name.isNotBlank())
  }
}

private class FakeMyModelRepository : DataRepository {
  private val backingRepository = DefaultDataRepository()
  override val data: StateFlow<com.example.moonbrewtavern.domain.model.GameScenario> = MutableStateFlow(backingRepository.scenario)
  override val gameState: StateFlow<GameState> = MutableStateFlow(backingRepository.gameState.value)
  override val nightState: StateFlow<NightState> = MutableStateFlow(backingRepository.nightState.value)
  override val lastBrewResult: StateFlow<BrewResult?> = MutableStateFlow(null)

  override fun evaluateBrew(selectedIngredientIds: Set<String>): BrewResult = DefaultDataRepository().evaluateBrew(selectedIngredientIds)

  override fun serveBrew(selectedIngredientIds: Set<String>): BrewResult = evaluateBrew(selectedIngredientIds)

  override fun collectGuestDeparture(visitorId: String) = Unit

  override fun confirmGuestDeparture() = Unit

  override fun startNight() = Unit

  override fun admitVisitor(visitorId: String) = Unit

  override fun rejectVisitor(visitorId: String) = Unit

  override fun enterTavern() = Unit

  override fun returnToEntrance() = Unit

  override fun startDialogue(visitorId: String) = Unit

  override fun openRecipeBook() = Unit

  override fun openBrewing() = Unit

  override fun finishNight() = Unit
}
