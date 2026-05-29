package com.example.moonbrewtavern.ui.main

import com.example.moonbrewtavern.data.DataRepository
import com.example.moonbrewtavern.data.DefaultDataRepository
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
  override val scenario = DefaultDataRepository().scenario
  override val data: Flow<com.example.moonbrewtavern.domain.model.GameScenario> = flowOf(scenario)
}
