package com.example.moonbrewtavern.data

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DefaultDataRepositoryTest {
  @Test
  fun tavernTimer_ticksAndDoesNotResetAfterReturningFromDialogue() = runBlocking {
    val repository = DefaultDataRepository()
    val visitorId = repository.nightState.value.queueVisitorIds.first()
    repository.admitVisitor(visitorId)
    repository.enterTavern()

    delay(1_200L)
    val afterFirstTick = repository.nightState.value.remainingNightMs
    assertTrue(afterFirstTick < com.example.moonbrewtavern.domain.model.GameLoopConfig.nightDurationMs)

    repository.startDialogue(visitorId)
    repository.enterTavern()

    assertEquals(afterFirstTick, repository.nightState.value.remainingNightMs)
    delay(1_200L)
    assertTrue(repository.nightState.value.remainingNightMs < afterFirstTick)
  }

  @Test
  fun purchaseRecipe_deductsGoldUnlocksAndSelectsRecipe() {
    val repository = DefaultDataRepository()

    assertTrue(repository.purchaseRecipe("herbal-mix", 10))

    assertEquals(2, repository.gameState.value.gold)
    assertTrue("herbal-mix" in repository.gameState.value.unlockedRecipeIds)
    assertEquals("herbal-mix", repository.scenario.recipe.id)
  }

  @Test
  fun purchaseRecipe_rejectsRecipeWhenGoldIsInsufficient() {
    val repository = DefaultDataRepository()

    assertFalse(repository.purchaseRecipe("moon-ale", 18))

    assertEquals(12, repository.gameState.value.gold)
    assertFalse("moon-ale" in repository.gameState.value.unlockedRecipeIds)
  }

  @Test
  fun rejectingWholeQueue_advancesNightAndRefillsQueue() {
    val repository = DefaultDataRepository()
    val firstQueue = repository.nightState.value.queueVisitorIds

    firstQueue.forEach(repository::rejectVisitor)

    assertEquals(4, repository.gameState.value.day)
    assertTrue(repository.nightState.value.queueVisitorIds.isNotEmpty())
    assertFalse(repository.nightState.value.queueVisitorIds == firstQueue)
  }

  @Test
  fun purchaseIngredient_deductsGoldAndAddsStock() {
    val repository = DefaultDataRepository()
    val initialStock = repository.gameState.value.ingredientStock.getValue("moonmint")

    assertTrue(repository.purchaseIngredient("moonmint", quantity = 1, unitPrice = 2))

    assertEquals(10, repository.gameState.value.gold)
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
}
