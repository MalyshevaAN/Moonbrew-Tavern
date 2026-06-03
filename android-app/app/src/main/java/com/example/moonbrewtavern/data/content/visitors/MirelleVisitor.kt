package com.example.moonbrewtavern.data.content.visitors

import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.domain.model.FlavorTag
import com.example.moonbrewtavern.domain.model.Visitor
import com.example.moonbrewtavern.domain.model.VisitorAssets
import com.example.moonbrewtavern.domain.model.VisitorDefinition
import com.example.moonbrewtavern.domain.model.VisitorMood
import com.example.moonbrewtavern.domain.model.VisitorRequest

private const val mirelleRequestLine = "Хочу что-то травяное и светлое. Без горечи, пожалуйста."

val mirelleVisitor =
  Visitor(
    id = "mirelle",
    name = "Mirelle Ash",
    title = "Странствующая ведьма-садовница",
    mood = VisitorMood.Warm,
    openingLine = "У тебя здесь пахнет мятой, дождем и правильными секретами.",
    requestLine = mirelleRequestLine,
    favoriteFlavor = "светлые травяные настои",
  )

val mirelleDefinition =
  VisitorDefinition(
    id = mirelleVisitor.id,
    name = mirelleVisitor.name,
    title = mirelleVisitor.title,
    mood = mirelleVisitor.mood,
    favoriteTags = setOf(FlavorTag.Herbal, FlavorTag.Fresh, FlavorTag.Bright),
    dislikedTags = setOf(FlavorTag.Bitter, FlavorTag.Smoky),
    requestPool =
      listOf(
        VisitorRequest(
          id = "mirelle-herbs",
          text = mirelleRequestLine,
          desiredTags = setOf(FlavorTag.Herbal, FlavorTag.Fresh),
          forbiddenTags = setOf(FlavorTag.Bitter),
        ),
      ),
    assets =
      VisitorAssets(
        queueRes = R.drawable.npc_witch,
        tavernSeatRes = R.drawable.tavern_room_guest_three,
        dialoguePortraitRes = R.drawable.npc_witch,
        resultPortraitRes = R.drawable.npc_witch,
      ),
  )
