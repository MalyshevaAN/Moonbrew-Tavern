package com.example.moonbrewtavern.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable

/** Returns a gentle looping vertical offset used to keep scene elements from feeling static. */
@Composable
fun rememberFloatingOffset(
  amplitude: Float = 8f,
  durationMs: Int = 2600,
): Float {
  val transition = rememberInfiniteTransition(label = "ambient-float")
  return transition
    .animateFloat(
      initialValue = -amplitude,
      targetValue = amplitude,
      animationSpec =
        infiniteRepeatable(
          animation =
            tween(
              durationMillis = durationMs,
              easing = LinearEasing,
            ),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "ambient-float-value",
    )
    .value
}
