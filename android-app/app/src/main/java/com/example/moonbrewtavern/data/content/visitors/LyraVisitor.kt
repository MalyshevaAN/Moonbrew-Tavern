package com.example.moonbrewtavern.data.content.visitors

import com.example.moonbrewtavern.domain.model.Visitor
import com.example.moonbrewtavern.domain.model.VisitorMood

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
