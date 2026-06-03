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
  val visitorsById = visitors.associateBy(Visitor::id)
  val visitorDefinitionsById = visitorDefinitions.associateBy(VisitorDefinition::id)
  val starterQueueVisitorIds = listOf(brannVisitor.id, mirelleVisitor.id, lyraVisitor.id, corinVisitor.id)
}
