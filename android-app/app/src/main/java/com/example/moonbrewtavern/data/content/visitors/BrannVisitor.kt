package com.example.moonbrewtavern.data.content.visitors

import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.domain.model.FlavorTag
import com.example.moonbrewtavern.domain.model.Visitor
import com.example.moonbrewtavern.domain.model.VisitorAssets
import com.example.moonbrewtavern.domain.model.VisitorDefinition
import com.example.moonbrewtavern.domain.model.VisitorMood
import com.example.moonbrewtavern.domain.model.VisitorRequest

private const val brannRequestLine = "Налей чего-нибудь дымного и теплого. Сегодня дорога не была доброй."

val brannVisitor =
  Visitor(
    id = "brann",
    name = "Brann Holt",
    title = "Плащеносец с западного тракта",
    mood = VisitorMood.Guarded,
    openingLine = "Если у тебя еще есть место у очага, я не стану спорить с удачей.",
    requestLine = brannRequestLine,
    favoriteFlavor = "дымные и согревающие напитки",
  )

val brannDefinition =
  VisitorDefinition(
    id = brannVisitor.id,
    name = brannVisitor.name,
    title = brannVisitor.title,
    mood = brannVisitor.mood,
    favoriteTags = setOf(FlavorTag.Warm, FlavorTag.Smoky, FlavorTag.Herbal),
    dislikedTags = setOf(FlavorTag.Sweet),
    requestPool =
      listOf(
        VisitorRequest(
          id = "brann-fire",
          text = brannRequestLine,
          desiredTags = setOf(FlavorTag.Warm, FlavorTag.Smoky),
        ),
      ),
    assets =
      VisitorAssets(
        queueRes = R.drawable.npc_hood,
        tavernSeatRes = R.drawable.tavern_room_guest_one,
        dialoguePortraitRes = R.drawable.npc_hood,
        resultPortraitRes = R.drawable.npc_hood,
      ),
  )
