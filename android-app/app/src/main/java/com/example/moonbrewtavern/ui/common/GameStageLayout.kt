package com.example.moonbrewtavern.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.domain.model.GamePhase
import com.example.moonbrewtavern.domain.model.GameState

@Composable
fun GameStageLayout(
  phaseLabel: String,
  title: String,
  subtitle: String,
  state: GameState,
  modifier: Modifier = Modifier,
  actionLabel: String? = null,
  actionNote: String? = null,
  actionEnabled: Boolean = true,
  onAction: (() -> Unit)? = null,
  sceneContent: @Composable ColumnScope.() -> Unit,
  detailContent: @Composable ColumnScope.() -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          Brush.linearGradient(
            listOf(colors.background, colors.surface, colors.primaryContainer.copy(alpha = 0.45f)),
          ),
        ),
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(28.dp),
      horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
      Column(
        modifier = Modifier.weight(1.35f).fillMaxHeight(),
      ) {
        PhaseBadge(phaseLabel)
        Spacer(Modifier.height(18.dp))
        Text(text = title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyLarge,
          color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        ProgressTrack(currentPhase = state.phase)
        Spacer(Modifier.height(20.dp))
        Surface(
          modifier = Modifier.fillMaxWidth().weight(1f),
          shape = RoundedCornerShape(28.dp),
          color = colors.surface.copy(alpha = 0.84f),
          tonalElevation = 4.dp,
        ) {
          Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = sceneContent,
          )
        }
      }

      Column(
        modifier = Modifier.weight(1f).fillMaxHeight(),
      ) {
        StatusBoard(state = state)
        Spacer(Modifier.height(20.dp))
        Surface(
          modifier = Modifier.fillMaxWidth().weight(1f),
          shape = RoundedCornerShape(28.dp),
          color = colors.surface.copy(alpha = 0.88f),
          tonalElevation = 3.dp,
        ) {
          Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = detailContent,
          )
        }
        if (actionLabel != null && onAction != null) {
          Spacer(Modifier.height(16.dp))
          if (actionNote != null) {
            Text(
              text = actionNote,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
          }
          Button(
            onClick = onAction,
            enabled = actionEnabled,
            modifier = Modifier.fillMaxWidth().height(56.dp),
          ) {
            Text(text = actionLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}

@Composable
private fun ProgressTrack(currentPhase: GamePhase) {
  val steps =
    listOf(
      GamePhase.Tavern to "Таверна",
      GamePhase.Dialogue to "Диалог",
      GamePhase.RecipeBook to "Рецепт",
      GamePhase.Brewing to "Варка",
      GamePhase.Result to "Итог",
    )
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    steps.forEach { (phase, label) ->
      val isActive = phase == currentPhase
      val isPassed = phase.ordinal < currentPhase.ordinal
      val container =
        when {
          isActive -> MaterialTheme.colorScheme.primary
          isPassed -> MaterialTheme.colorScheme.primaryContainer
          else -> MaterialTheme.colorScheme.surfaceVariant
        }
      val contentColor =
        when {
          isActive -> MaterialTheme.colorScheme.onPrimary
          else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
      Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(999.dp),
        color = container,
      ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
          )
        }
      }
    }
  }
}

@Composable
fun SectionTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.SemiBold,
  )
}

@Composable
fun InfoLine(label: String, value: String) {
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(text = value, style = MaterialTheme.typography.bodyLarge)
  }
}

@Composable
private fun PhaseBadge(label: String) {
  Box(
    modifier =
      Modifier
        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(999.dp))
        .padding(horizontal = 14.dp, vertical = 8.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSecondaryContainer,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun StatusBoard(state: GameState) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    StatusTile(label = "Night", value = state.day.toString(), modifier = Modifier.weight(1f))
    StatusTile(label = "Gold", value = state.gold.toString(), modifier = Modifier.weight(1f))
    StatusTile(label = "Rep", value = state.reputation.toString(), modifier = Modifier.weight(1f))
  }
}

@Composable
private fun StatusTile(label: String, value: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
fun AccentBlock(
  modifier: Modifier = Modifier,
  accent: Color = MaterialTheme.colorScheme.primaryContainer,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    color = accent.copy(alpha = 0.65f),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      content = content,
    )
  }
}

@Composable
fun AmbientScenePanel(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(64.dp)
            .background(
              brush =
                Brush.radialGradient(
                  colors =
                    listOf(
                      MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                      MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ),
              shape = RoundedCornerShape(20.dp),
            ),
      )
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun IngredientBadge(
  name: String,
  note: String,
  selected: Boolean = false,
  enabled: Boolean = true,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  val tint =
    when {
      selected -> MaterialTheme.colorScheme.primaryContainer
      else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
    }
  Surface(
    modifier =
      modifier
        .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
        .border(
          width = if (selected) 2.dp else 1.dp,
          color =
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
          shape = RoundedCornerShape(18.dp),
        ),
    shape = RoundedCornerShape(18.dp),
    color = tint,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(text = name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
      Text(
        text = note,
        style = MaterialTheme.typography.bodySmall,
        color =
          if (selected) MaterialTheme.colorScheme.onPrimaryContainer
          else MaterialTheme.colorScheme.onTertiaryContainer,
      )
    }
  }
}
