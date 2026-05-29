package com.example.moonbrewtavern.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.moonbrewtavern.data.DefaultDataRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.moonbrewtavern.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      MainScreen(
        scenario = DefaultDataRepository().scenario,
        onStartDialogue = {},
      )
    }
  }

  @Test
  fun title_exists() {
    composeTestRule.onNodeWithText("Moonbrew Tavern").assertExists()
  }
}
