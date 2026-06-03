package com.example.moonbrewtavern.data.content

import com.example.moonbrewtavern.data.content.ingredients.firstNightIngredients
import com.example.moonbrewtavern.data.content.recipes.starglowTonicRecipe
import com.example.moonbrewtavern.data.content.visitors.brannDefinition
import com.example.moonbrewtavern.data.content.visitors.brannVisitor
import com.example.moonbrewtavern.data.content.visitors.corinDefinition
import com.example.moonbrewtavern.data.content.visitors.corinVisitor
import com.example.moonbrewtavern.data.content.visitors.lyraVisitor
import com.example.moonbrewtavern.data.content.visitors.lyraDefinition
import com.example.moonbrewtavern.data.content.visitors.mirelleDefinition
import com.example.moonbrewtavern.data.content.visitors.mirelleVisitor
import com.example.moonbrewtavern.domain.model.Visitor
import com.example.moonbrewtavern.domain.model.VisitorDefinition

object ContentCatalog {
  val visitors: List<Visitor> = listOf(brannVisitor, mirelleVisitor, lyraVisitor, corinVisitor)
  val visitorDefinitions: List<VisitorDefinition> = listOf(brannDefinition, mirelleDefinition, lyraDefinition, corinDefinition)
  val recipes = listOf(starglowTonicRecipe)
  val ingredients = firstNightIngredients
  val starterQueueVisitorIds = listOf(brannVisitor.id, mirelleVisitor.id, lyraVisitor.id, corinVisitor.id)

  init {
    requireUniqueIds("visitors", visitors.map(Visitor::id))
    requireUniqueIds("visitor definitions", visitorDefinitions.map(VisitorDefinition::id))

    val visitorIds = visitors.map(Visitor::id).toSet()
    val definitionIds = visitorDefinitions.map(VisitorDefinition::id).toSet()
    val missingDefinitionIds = visitorIds - definitionIds
    check(missingDefinitionIds.isEmpty()) {
      "Missing VisitorDefinition entries for visitor ids: ${missingDefinitionIds.joinToString()}"
    }

    val orphanDefinitionIds = definitionIds - visitorIds
    check(orphanDefinitionIds.isEmpty()) {
      "VisitorDefinition entries without matching Visitor ids: ${orphanDefinitionIds.joinToString()}"
    }

    val unknownStarterIds = starterQueueVisitorIds.filterNot(visitorIds::contains)
    check(unknownStarterIds.isEmpty()) {
      "Starter queue references unknown visitor ids: ${unknownStarterIds.joinToString()}"
    }
  }

  val visitorsById = visitors.associateBy(Visitor::id)
  val visitorDefinitionsById = visitorDefinitions.associateBy(VisitorDefinition::id)

  private fun requireUniqueIds(label: String, ids: List<String>) {
    val duplicateIds = ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    check(duplicateIds.isEmpty()) {
      "Duplicate $label ids found: ${duplicateIds.joinToString()}"
    }
  }
}
