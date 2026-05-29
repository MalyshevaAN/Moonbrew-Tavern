package com.example.moonbrewtavern

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable data object Dialogue : NavKey

@Serializable data object RecipeBook : NavKey

@Serializable data object Brewing : NavKey

@Serializable data object Result : NavKey
