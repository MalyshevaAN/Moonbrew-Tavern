package com.example.moonbrewtavern

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.ui.brewing.BrewingScreen
import com.example.moonbrewtavern.ui.dialogue.DialogueScreen
import com.example.moonbrewtavern.ui.main.MainScreen
import com.example.moonbrewtavern.ui.main.MainScreenViewModel
import com.example.moonbrewtavern.ui.result.ResultScreen

@Composable
fun MainNavigation() {
  val repository = remember { DefaultDataRepository() }
  val scenario = remember { repository.scenario }
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(
            onItemClick = { navKey -> backStack.add(navKey) },
            modifier = Modifier.safeDrawingPadding().padding(16.dp),
            viewModel = viewModel { MainScreenViewModel(repository) },
          )
        }
        entry<Dialogue> {
          DialogueScreen(
            scenario = scenario,
            onContinue = { backStack.add(Brewing) },
            modifier = Modifier.safeDrawingPadding().padding(16.dp),
          )
        }
        entry<Brewing> {
          BrewingScreen(
            scenario = scenario,
            onServe = { backStack.add(Result) },
            modifier = Modifier.safeDrawingPadding().padding(16.dp),
          )
        }
        entry<Result> {
          ResultScreen(
            scenario = scenario,
            onReturnToTavern = {
              while (backStack.size > 1) {
                backStack.removeLastOrNull()
              }
            },
            modifier = Modifier.safeDrawingPadding().padding(16.dp),
          )
        }
      },
  )
}
