package com.example.moonbrewtavern.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Moss80,
    secondary = Clay80,
    tertiary = Sage80,
    background = Bark10,
    surface = Bark20,
    surfaceVariant = Color(0xFF3A322E),
    primaryContainer = Color(0xFF264337),
    secondaryContainer = Color(0xFF6F3D2B),
    tertiaryContainer = Color(0xFF4A5A2E),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Moss40,
    secondary = Clay40,
    tertiary = Sage40,
    background = Linen95,
    surface = Color(0xFFFBF6F0),
    surfaceVariant = Linen90,
    primaryContainer = Color(0xFFD5E6DB),
    secondaryContainer = Color(0xFFF2D7CA),
    tertiaryContainer = Color(0xFFD8E2BC),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Moss20,
    onSurface = Moss20,
  )

/** Applies the shared Material theme used across the Moonbrew Tavern demo. */
@Composable
fun MoonbrewTavernTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
