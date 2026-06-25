package com.example.moonbrewtavern.data

import com.example.moonbrewtavern.data.persistence.PersistedGameSnapshot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DefaultDataRepositoryTest {
  @Test
  fun nightTimer_continuesWhileDialogueIsOpen() = runBlocking {
    val repository = DefaultDataRepository()
    val visitorId = repository.nightState.value.queueVisitorIds.first()
    repository.admitVisitor(visitorId)
    repository.enterTavern()

    delay(1_200L)
    val beforeDialogue = repository.nightState.value.remainingNightMs
    assertTrue(beforeDialogue < com.example.moonbrewtavern.domain.model.GameLoopConfig.nightDurationMs)

    repository.startDialogue(visitorId)

    delay(1_200L)
    assertTrue(repository.nightState.value.remainingNightMs < beforeDialogue)
  }

  @Test
  fun nightTimer_continuesOnStreetAndDoesNotResetWhenReturning() = runBlocking {
    val repository = DefaultDataRepository()
    val firstVisitorId = repository.nightState.value.queueVisitorIds.first()
    repository.admitVisitor(firstVisitorId)
    repository.enterTavern()

    delay(1_200L)
    val beforeStreet = repository.nightState.value.remainingNightMs
    assertTrue(beforeStreet < com.example.moonbrewtavern.domain.model.GameLoopConfig.nightDurationMs)

    repository.returnToEntrance()

    delay(1_200L)
    val onStreet = repository.nightState.value.remainingNightMs
    assertTrue(onStreet < beforeStreet)

    repository.enterTavern()

    assertEquals(onStreet, repository.nightState.value.remainingNightMs)
  }

  @Test
  fun purchaseRecipe_deductsGoldUnlocksAndSelectsRecipe() {
    val repository = DefaultDataRepository()
    val initialGold = repository.gameState.value.gold

    assertTrue(repository.purchaseRecipe("herbal-mix", 10))

    assertEquals(initialGold - 10, repository.gameState.value.gold)
    assertTrue("herbal-mix" in repository.gameState.value.unlockedRecipeIds)
    assertEquals("herbal-mix", repository.scenario.recipe.id)
  }

  @Test
  fun purchaseRecipe_rejectsRecipeWhenGoldIsInsufficient() {
    val repository = DefaultDataRepository()
    val initialGold = repository.gameState.value.gold

    assertFalse(repository.purchaseRecipe("moon-ale", initialGold + 1))

    assertEquals(initialGold, repository.gameState.value.gold)
    assertFalse("moon-ale" in repository.gameState.value.unlockedRecipeIds)
  }

  @Test
  fun rejectingWholeQueue_opensSummaryAndStartsNextNightAfterConfirmation() {
    val repository = DefaultDataRepository()
    val firstQueue = repository.nightState.value.queueVisitorIds

    firstQueue.forEach(repository::rejectVisitor)

    assertEquals(com.example.moonbrewtavern.domain.model.GamePhase.Summary, repository.gameState.value.phase)
    assertNotNull(repository.lastNightSummary.value)

    repository.confirmNightSummary()

    assertEquals(4, repository.gameState.value.day)
    assertTrue(repository.nightState.value.queueVisitorIds.isNotEmpty())
    assertFalse(repository.nightState.value.queueVisitorIds == firstQueue)
  }

  @Test
  fun purchaseIngredient_deductsGoldAndAddsStock() {
    val repository = DefaultDataRepository()
    val initialGold = repository.gameState.value.gold
    val initialStock = repository.gameState.value.ingredientStock.getValue("moonmint")

    assertTrue(repository.purchaseIngredient("moonmint", quantity = 1, unitPrice = 2))

    assertEquals(initialGold - 2, repository.gameState.value.gold)
    assertEquals(initialStock + 1, repository.gameState.value.ingredientStock["moonmint"])
    assertEquals(initialStock + 1, repository.scenario.availableIngredients.first { it.id == "moonmint" }.stockCount)
  }

  @Test
  fun servingBrew_consumesSelectedIngredients() {
    val repository = DefaultDataRepository()
    val visitorId = repository.nightState.value.queueVisitorIds.first()
    repository.admitVisitor(visitorId)
    repository.enterTavern()
    repository.startDialogue(visitorId)
    val before = repository.gameState.value.ingredientStock

    repository.serveBrew(listOf("moonmint", "emberzest", "silverfoam"))

    assertEquals(before.getValue("moonmint") - 1, repository.gameState.value.ingredientStock["moonmint"])
    assertEquals(before.getValue("emberzest") - 1, repository.gameState.value.ingredientStock["emberzest"])
    assertEquals(before.getValue("silverfoam") - 1, repository.gameState.value.ingredientStock["silverfoam"])
  }

  @Test
  fun repository_restoresSavedSnapshotOnCreation() {
    val sourceRepository = DefaultDataRepository()
    val visitorId = sourceRepository.nightState.value.queueVisitorIds.first()
    val snapshotGameState =
      sourceRepository.gameState.value.copy(
        gold = 120,
        unlockedRecipeIds = sourceRepository.gameState.value.unlockedRecipeIds + "herbal-mix",
      )
    val snapshotNightState =
      sourceRepository.nightState.value.copy(
        queueVisitorIds = sourceRepository.nightState.value.queueVisitorIds - visitorId,
        guests = listOf(com.example.moonbrewtavern.domain.model.TavernGuest(visitorId = visitorId)),
        currentVisitorId = visitorId,
        nightStarted = true,
      )

    val snapshot =
      PersistedGameSnapshot(
        gameState = snapshotGameState,
        nightState = snapshotNightState,
        activeRecipeId = "herbal-mix",
        lastBrewResult = sourceRepository.lastBrewResult.value,
      )
    val restoredRepository =
      DefaultDataRepository(
        initialSnapshot = snapshot,
      )

    assertEquals(snapshotGameState, restoredRepository.gameState.value)
    assertEquals(snapshotNightState, restoredRepository.nightState.value)
    assertEquals("herbal-mix", restoredRepository.scenario.recipe.id)
  }

  @Test
  fun repository_recoversToSummaryFromEmptyEntranceSnapshot() {
    val sourceRepository = DefaultDataRepository()
    val restoredRepository =
      DefaultDataRepository(
        initialSnapshot =
          PersistedGameSnapshot(
            gameState =
              sourceRepository.gameState.value.copy(
                phase = com.example.moonbrewtavern.domain.model.GamePhase.Entrance,
              ),
            nightState =
              sourceRepository.nightState.value.copy(
                queueVisitorIds = emptyList(),
                guests = emptyList(),
                currentVisitorId = null,
                phase = com.example.moonbrewtavern.domain.model.NightPhase.Entrance,
              ),
            activeRecipeId = sourceRepository.scenario.recipe.id,
            lastBrewResult = null,
            lastNightSummary = null,
          ),
      )

    assertEquals(com.example.moonbrewtavern.domain.model.GamePhase.Summary, restoredRepository.gameState.value.phase)
    assertNotNull(restoredRepository.lastNightSummary.value)
  }

  @Test
  fun finishNight_exposesSummaryUntilNextNightIsConfirmed() {
    val repository = DefaultDataRepository()

    repository.finishNight()

    assertEquals(com.example.moonbrewtavern.domain.model.GamePhase.Summary, repository.gameState.value.phase)
    assertNotNull(repository.lastNightSummary.value)

    repository.confirmNightSummary()

    assertEquals(com.example.moonbrewtavern.domain.model.GamePhase.Entrance, repository.gameState.value.phase)
    assertEquals(4, repository.gameState.value.day)
    assertNull(repository.lastNightSummary.value)
  }
}
