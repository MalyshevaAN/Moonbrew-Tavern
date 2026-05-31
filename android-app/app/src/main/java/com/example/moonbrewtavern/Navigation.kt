package com.example.moonbrewtavern

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.ui.brewing.BrewingScreen
import com.example.moonbrewtavern.ui.dialogue.DialogueScreen
import com.example.moonbrewtavern.ui.main.MainScreen
import com.example.moonbrewtavern.ui.main.MainScreenViewModel
import com.example.moonbrewtavern.ui.recipebook.RecipeBookScreen
import com.example.moonbrewtavern.ui.result.ResultScreen

@Composable
fun MainNavigation() {
  val repository = remember { DefaultDataRepository() }
  val scenario = remember { repository.scenario }
  val backStack = rememberNavBackStack(Main)
  var brewResult by remember { mutableStateOf(repository.evaluateBrew(emptySet())) }

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(
            onItemClick = { navKey -> backStack.add(navKey) },
            modifier = Modifier,
            viewModel = viewModel { MainScreenViewModel(repository) },
          )
        }
        entry<Dialogue> {
          DialogueScreen(
            scenario = scenario,
            onContinue = { backStack.add(RecipeBook) },
            modifier = Modifier,
          )
        }
        entry<RecipeBook> {
          RecipeBookScreen(
            scenario = scenario,
            onStartBrewing = { backStack.add(Brewing) },
            modifier = Modifier,
          )
        }
        entry<Brewing> {
          BrewingScreen(
            scenario = scenario,
            onServe = { selectedIds ->
              brewResult = repository.evaluateBrew(selectedIds)
              backStack.add(Result)
            },
            modifier = Modifier,
          )
        }
        entry<Result> {
          ResultScreen(
            scenario = scenario,
            brewResult = brewResult,
            onReturnToTavern = {
              brewResult = repository.evaluateBrew(emptySet())
              while (backStack.size > 1) {
                backStack.removeLastOrNull()
              }
            },
            modifier = Modifier,
          )
        }
      },
  )
}
