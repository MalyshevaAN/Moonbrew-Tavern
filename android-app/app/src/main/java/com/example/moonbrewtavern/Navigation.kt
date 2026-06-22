package com.example.moonbrewtavern

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.data.content.ContentCatalog
import com.example.moonbrewtavern.ui.brewing.BrewingScreen
import com.example.moonbrewtavern.ui.dialogue.DialogueScreen
import com.example.moonbrewtavern.ui.entrance.EntranceScreen
import com.example.moonbrewtavern.ui.recipebook.RecipeBookScreen
import com.example.moonbrewtavern.ui.result.ResultScreen
import com.example.moonbrewtavern.ui.tavernroom.TavernRoomScreen

/** Wires the repository-backed night flow into the app navigation graph. */
@Composable
fun MainNavigation() {
  val repository = remember { DefaultDataRepository() }
  val backStack = rememberNavBackStack(Main)
  val scenario by repository.data.collectAsStateWithLifecycle()
  val gameState by repository.gameState.collectAsStateWithLifecycle()
  val nightState by repository.nightState.collectAsStateWithLifecycle()
  val brewResult by repository.lastBrewResult.collectAsStateWithLifecycle()

  LaunchedEffect(gameState.phase, backStack.size) {
    if (gameState.phase == com.example.moonbrewtavern.domain.model.GamePhase.Entrance && backStack.size > 1) {
      while (backStack.size > 1) {
        backStack.removeLastOrNull()
      }
    }
  }

  NavDisplay(
    backStack = backStack,
    onBack = {
      if (backStack.lastOrNull() == Result) {
        repository.confirmGuestDeparture()
        backStack.removeLastOrNull()
        return@NavDisplay
      }

      backStack.removeLastOrNull()
      when (backStack.lastOrNull()) {
        Main -> repository.returnToEntrance()
        TavernRoom -> repository.enterTavern()
        Dialogue -> nightState.currentVisitorId?.let(repository::startDialogue)
        RecipeBook -> repository.openRecipeBook()
        Brewing -> repository.openBrewing()
        Result -> {}
        null -> {}
      }
    },
    entryProvider =
      entryProvider {
        entry<Main> {
          EntranceScreen(
            gameState = gameState,
            nightState = nightState,
            visitorDefinitions = ContentCatalog.visitorDefinitionsById,
            onAdmit = repository::admitVisitor,
            onReject = repository::rejectVisitor,
            onEnterTavern = {
              repository.enterTavern()
              backStack.add(TavernRoom)
            },
            modifier = Modifier,
          )
        }
        entry<TavernRoom> {
          TavernRoomScreen(
            gameState = gameState,
            nightState = nightState,
            visitorDefinitions = ContentCatalog.visitorDefinitionsById,
            onGuestClick = { visitorId ->
              val guest = nightState.guests.firstOrNull { it.visitorId == visitorId }
              when (guest?.status) {
                com.example.moonbrewtavern.domain.model.TavernGuestStatus.WaitingForOrder -> {
                  repository.startDialogue(visitorId)
                  backStack.add(Dialogue)
                }
                com.example.moonbrewtavern.domain.model.TavernGuestStatus.WantsToLeave -> {
                  repository.collectGuestDeparture(visitorId)
                  backStack.add(Result)
                }
                else -> Unit
              }
            },
            onBackToStreet = {
              repository.returnToEntrance()
              backStack.removeLastOrNull()
            },
            modifier = Modifier,
          )
        }
        entry<Dialogue> {
          DialogueScreen(
            scenario = scenario,
            onContinue = {
              repository.openRecipeBook()
              backStack.add(RecipeBook)
            },
            modifier = Modifier,
          )
        }
        entry<RecipeBook> {
          RecipeBookScreen(
            scenario = scenario,
            gameState = gameState,
            onBack = {
              repository.startDialogue(scenario.visitor.id)
              backStack.removeLastOrNull()
            },
            onSelectRecipe = repository::selectRecipe,
            onPurchaseRecipe = repository::purchaseRecipe,
            onPurchaseIngredient = repository::purchaseIngredient,
            onStartBrewing = {
              repository.openBrewing()
              backStack.add(Brewing)
            },
            modifier = Modifier,
          )
        }
        entry<Brewing> {
          BrewingScreen(
            scenario = scenario,
            onBack = {
              repository.openRecipeBook()
              backStack.removeLastOrNull()
            },
            onServe = { selectedIds ->
              repository.serveBrew(selectedIds)
              while (backStack.lastOrNull() != TavernRoom) {
                backStack.removeLastOrNull()
              }
            },
            modifier = Modifier,
          )
        }
        entry<Result> {
          ResultScreen(
            scenario = scenario,
            gameState = gameState,
            brewResult = brewResult ?: repository.evaluateBrew(emptyList()),
            onReturnToTavern = {
              repository.confirmGuestDeparture()
              while (backStack.lastOrNull() != TavernRoom && backStack.size > 1) {
                backStack.removeLastOrNull()
              }
            },
            modifier = Modifier,
          )
        }
      },
  )
}
