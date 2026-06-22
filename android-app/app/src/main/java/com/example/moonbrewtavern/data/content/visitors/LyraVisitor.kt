package com.example.moonbrewtavern.data.content.visitors

import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.domain.model.FlavorTag
import com.example.moonbrewtavern.domain.model.Visitor
import com.example.moonbrewtavern.domain.model.VisitorAssets
import com.example.moonbrewtavern.domain.model.VisitorDefinition
import com.example.moonbrewtavern.domain.model.VisitorMood
import com.example.moonbrewtavern.domain.model.VisitorRequest

const val lyraOpeningLine = "Так вот о какой таверне шепчутся путники, когда дорога становится особенно долгой."
const val lyraRequestLine = "Если рука у тебя твердая, приготовь мне что-нибудь ясное по вкусу, с легким теплым послевкусием."
const val lyraFavoriteFlavor = "прохладные травы с теплым послевкусием"

val lyraVisitor =
  Visitor(
    id = "lyra",
    name = "Lyra Vale",
    title = "Картограф Северного тракта",
    mood = VisitorMood.Curious,
    openingLine = lyraOpeningLine,
    requestLine = lyraRequestLine,
    favoriteFlavor = lyraFavoriteFlavor,
  )

val lyraDefinition =
  VisitorDefinition(
    id = lyraVisitor.id,
    name = lyraVisitor.name,
    title = lyraVisitor.title,
    mood = lyraVisitor.mood,
    favoriteTags = setOf(FlavorTag.Fresh, FlavorTag.Bright, FlavorTag.Warm),
    dislikedTags = setOf(FlavorTag.Bitter, FlavorTag.Strange),
    preferredRecipeIds = setOf("starglow-tonic"),
    requestPool =
      listOf(
        VisitorRequest(
          id = "lyra-clear-mind",
          text = lyraRequestLine,
          desiredTags = setOf(FlavorTag.Fresh, FlavorTag.Bright, FlavorTag.Warm),
        ),
      ),
    assets =
      VisitorAssets(
        queueRes = R.drawable.portrait_lyra,
        tavernSeatRes = R.drawable.portrait_lyra,
        dialoguePortraitRes = R.drawable.portrait_lyra,
        resultPortraitRes = R.drawable.portrait_lyra,
      ),
  )
