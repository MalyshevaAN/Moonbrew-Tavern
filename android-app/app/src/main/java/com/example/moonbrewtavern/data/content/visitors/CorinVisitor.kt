package com.example.moonbrewtavern.data.content.visitors

import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.domain.model.FlavorTag
import com.example.moonbrewtavern.domain.model.Visitor
import com.example.moonbrewtavern.domain.model.VisitorAssets
import com.example.moonbrewtavern.domain.model.VisitorDefinition
import com.example.moonbrewtavern.domain.model.VisitorMood
import com.example.moonbrewtavern.domain.model.VisitorRequest

private const val corinRequestLine = "Сделай что-нибудь мягкое и сладкое. Мне бы передохнуть от чужих новостей."

val corinVisitor =
  Visitor(
    id = "corin",
    name = "Corin Vale",
    title = "Старый переписчик дорожных слухов",
    mood = VisitorMood.Curious,
    openingLine = "Если тут еще подают напитки тем, кто слишком много слушает, я останусь до закрытия.",
    requestLine = corinRequestLine,
    favoriteFlavor = "мягкие сладковатые напитки",
  )

val corinDefinition =
  VisitorDefinition(
    id = corinVisitor.id,
    name = corinVisitor.name,
    title = corinVisitor.title,
    mood = corinVisitor.mood,
    favoriteTags = setOf(FlavorTag.Sweet, FlavorTag.Warm, FlavorTag.Bright),
    dislikedTags = setOf(FlavorTag.Smoky),
    requestPool =
      listOf(
        VisitorRequest(
          id = "corin-rest",
          text = corinRequestLine,
          desiredTags = setOf(FlavorTag.Sweet, FlavorTag.Warm),
        ),
      ),
    assets =
      VisitorAssets(
        queueRes = R.drawable.portrait_corin,
        tavernSeatRes = R.drawable.portrait_corin,
        dialoguePortraitRes = R.drawable.portrait_corin,
        resultPortraitRes = R.drawable.portrait_corin,
      ),
  )
