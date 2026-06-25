package com.example.moonbrewtavern

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.data.content.ContentCatalog
import com.example.moonbrewtavern.data.persistence.DataStoreGameSaveStore
import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.ui.brewing.BrewingScreen
import com.example.moonbrewtavern.ui.dialogue.DialogueScreen
import com.example.moonbrewtavern.ui.entrance.EntranceScreen
import com.example.moonbrewtavern.ui.recipebook.RecipeBookScreen
import com.example.moonbrewtavern.ui.result.ResultScreen
import com.example.moonbrewtavern.ui.summary.NightSummaryScreen
import com.example.moonbrewtavern.ui.tavernroom.TavernRoomScreen

/** Wires the repository-backed night flow into the app navigation graph. */
@Composable
fun MainNavigation() {
  val context = LocalContext.current
  val saveStore = remember(context.applicationContext) { DataStoreGameSaveStore(context.applicationContext) }
  val repository by
    produceState<DefaultDataRepository?>(initialValue = null, saveStore) {
      val initialSnapshot = saveStore.read()
      value = DefaultDataRepository(saveStore = saveStore, initialSnapshot = initialSnapshot)
    }
  if (repository == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
    return
  }
  val loadedRepository = repository ?: return
  val backStack = rememberNavBackStack(Main)
  val scenario by loadedRepository.data.collectAsStateWithLifecycle()
  val gameState by loadedRepository.gameState.collectAsStateWithLifecycle()
  val nightState by loadedRepository.nightState.collectAsStateWithLifecycle()
  val brewResult by loadedRepository.lastBrewResult.collectAsStateWithLifecycle()
  val nightSummary by loadedRepository.lastNightSummary.collectAsStateWithLifecycle()

  LaunchedEffect(gameState.phase, backStack.size) {
    if (gameState.phase == GamePhase.Entrance && backStack.size > 1) {
      while (backStack.size > 1) {
        backStack.removeLastOrNull()
      }
    }

    if (backStack.size == 1) {
      when (gameState.phase) {
        GamePhase.Entrance -> Unit
        GamePhase.Tavern -> backStack.add(TavernRoom)
        GamePhase.Dialogue -> {
          backStack.add(TavernRoom)
          backStack.add(Dialogue)
        }
        GamePhase.RecipeBook -> {
          backStack.add(TavernRoom)
          backStack.add(Dialogue)
          backStack.add(RecipeBook)
        }
        GamePhase.Brewing -> {
          backStack.add(TavernRoom)
          backStack.add(Dialogue)
          backStack.add(RecipeBook)
          backStack.add(Brewing)
        }
        GamePhase.Result -> {
          backStack.add(TavernRoom)
          backStack.add(Result)
        }
        GamePhase.Summary -> backStack.add(Summary)
      }
    }
  }

  NavDisplay(
    backStack = backStack,
    onBack = {
      if (backStack.lastOrNull() == Result) {
        loadedRepository.confirmGuestDeparture()
        backStack.removeLastOrNull()
        return@NavDisplay
      }
      if (backStack.lastOrNull() == Summary) {
        loadedRepository.confirmNightSummary()
        backStack.removeLastOrNull()
        return@NavDisplay
      }

      backStack.removeLastOrNull()
      when (backStack.lastOrNull()) {
        Main -> loadedRepository.returnToEntrance()
        TavernRoom -> loadedRepository.enterTavern()
        Dialogue -> nightState.currentVisitorId?.let(loadedRepository::startDialogue)
        RecipeBook -> loadedRepository.openRecipeBook()
        Brewing -> loadedRepository.openBrewing()
        Result -> {}
        Summary -> {}
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
            onAdmit = loadedRepository::admitVisitor,
            onReject = loadedRepository::rejectVisitor,
            onEnterTavern = {
              loadedRepository.enterTavern()
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
                  loadedRepository.startDialogue(visitorId)
                  backStack.add(Dialogue)
                }
                com.example.moonbrewtavern.domain.model.TavernGuestStatus.WantsToLeave -> {
                  loadedRepository.collectGuestDeparture(visitorId)
                  backStack.add(Result)
                }
                else -> Unit
              }
            },
            onBackToStreet = {
              loadedRepository.returnToEntrance()
              backStack.removeLastOrNull()
            },
            modifier = Modifier,
          )
        }
        entry<Dialogue> {
          DialogueScreen(
            scenario = scenario,
            onContinue = {
              loadedRepository.openRecipeBook()
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
              loadedRepository.startDialogue(scenario.visitor.id)
              backStack.removeLastOrNull()
            },
            onSelectRecipe = loadedRepository::selectRecipe,
            onPurchaseRecipe = loadedRepository::purchaseRecipe,
            onPurchaseIngredient = loadedRepository::purchaseIngredient,
            onStartBrewing = {
              loadedRepository.openBrewing()
              backStack.add(Brewing)
            },
            modifier = Modifier,
          )
        }
        entry<Brewing> {
          BrewingScreen(
            scenario = scenario,
            onBack = {
              loadedRepository.openRecipeBook()
              backStack.removeLastOrNull()
            },
            onServe = { selectedIds ->
              loadedRepository.serveBrew(selectedIds)
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
            brewResult = brewResult ?: loadedRepository.evaluateBrew(emptyList()),
            onReturnToTavern = {
              loadedRepository.confirmGuestDeparture()
              while (backStack.lastOrNull() != TavernRoom && backStack.size > 1) {
                backStack.removeLastOrNull()
              }
            },
            modifier = Modifier,
          )
        }
        entry<Summary> {
          nightSummary?.let { summary ->
            NightSummaryScreen(
              summary = summary,
              onStartNextNight = {
                loadedRepository.confirmNightSummary()
                while (backStack.size > 1) {
                  backStack.removeLastOrNull()
                }
              },
              modifier = Modifier,
            )
          }
        }
      },
  )
}
