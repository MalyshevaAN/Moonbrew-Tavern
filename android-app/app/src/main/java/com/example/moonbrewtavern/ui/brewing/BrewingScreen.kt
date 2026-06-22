package com.example.moonbrewtavern.ui.brewing

import android.os.SystemClock
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.domain.model.GameScenario
import com.example.moonbrewtavern.domain.model.Ingredient
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme
import com.example.moonbrewtavern.ui.common.rememberFloatingOffset
import java.util.Locale
import kotlin.math.roundToInt

private data class BrewTone(
  val label: String,
  val color: Color,
)

private data class BrewIngredientVisual(
  @param:DrawableRes val iconRes: Int,
  val count: Int,
  val label: String,
)

private data class DragState(
  val visual: BrewIngredientVisual,
  val position: Offset,
)

/** Brewing minigame where the player drags ingredients, stirs, and serves the order. */
@Composable
fun BrewingScreen(
  scenario: GameScenario,
  onBack: () -> Unit,
  onServe: (List<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val ingredientVisuals = remember(scenario.availableIngredients) { ingredientVisualsForScenario(scenario) }
  val selectedIds = remember { mutableStateListOf<String>() }
  var selectedToneIndex by rememberSaveable { mutableIntStateOf(-1) }
  var dragState by remember { mutableStateOf<DragState?>(null) }
  var cauldronBounds by remember { mutableStateOf<Rect?>(null) }
  var stirrerOffset by remember { mutableStateOf(Offset.Zero) }
  var stirBaseMs by rememberSaveable { mutableLongStateOf(0L) }
  var stirDragStartMs by remember { mutableStateOf<Long?>(null) }

  val tones =
    listOf(
      BrewTone("Синий", Color(0xFF6678F1)),
      BrewTone("Травяной", Color(0xFF88B48D)),
      BrewTone("Розовый", Color(0xFFC779B2)),
      BrewTone("Фиалковый", Color(0xFF9B76D6)),
      BrewTone("Лиловый", Color(0xFF6E6FD4)),
    )
  val selectedTone = tones.getOrNull(selectedToneIndex)
  val selectedIngredients = selectedIds.mapNotNull { selectedId -> scenario.availableIngredients.firstOrNull { it.id == selectedId } }
  val selectedCounts = selectedIds.groupingBy { it }.eachCount()
  val canAddMore = selectedIds.size < 3
  val stirElapsedMs = currentStirElapsedMs(stirBaseMs, stirDragStartMs)
  val stirProgress = (stirElapsedMs / 2000f).coerceIn(0f, 1f)
  val isStirred = stirElapsedMs >= 2000L
  val canStir = selectedIngredients.isNotEmpty()
  val canServe = selectedIds.size == 3 && isStirred && selectedTone != null
  val cauldronFloat = rememberFloatingOffset(amplitude = 5f, durationMs = 2800)
  val bubbleFloat = rememberFloatingOffset(amplitude = 8f, durationMs = 1900)

  fun resetBrewProgress(keepIngredients: Boolean = true) {
    if (!keepIngredients) {
      selectedIds.clear()
    }
    selectedToneIndex = -1
    stirrerOffset = Offset.Zero
    stirBaseMs = 0L
    stirDragStartMs = null
  }

  // Root brewing scene that layers the background, controls, and drag overlay.
  Box(modifier = modifier.fillMaxSize()) {
    // Illustrated brewing background.
    Image(
      painter = painterResource(R.drawable.brew_scene_background),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    // Main brewing UI stack: top bar, work area, and color rail.
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      BrewingTopBar(onBack = onBack)

      // Three-column work area: ingredients, cauldron, and step checklist.
      Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        IngredientRack(
          visuals = ingredientVisuals,
          selectedCounts = selectedCounts,
          canAddMore = canAddMore,
          onIngredientDropped = { ingredient ->
            val alreadySelected = selectedCounts[ingredient.id] ?: 0
            if (alreadySelected < ingredient.stockCount && selectedIds.size < 3) {
              selectedIds.add(ingredient.id)
              selectedToneIndex = -1
              stirrerOffset = Offset.Zero
              stirBaseMs = 0L
              stirDragStartMs = null
            }
          },
          onDragStateChange = { dragState = it },
          cauldronBounds = cauldronBounds,
          modifier = Modifier.width(114.dp).fillMaxHeight(),
        )

        CauldronStage(
          selectedIngredients = selectedIngredients,
          selectedTone = selectedTone,
          stirProgress = stirProgress,
          stirrerOffset = stirrerOffset,
          canStir = canStir,
          ambientFloat = cauldronFloat,
          bubbleFloat = bubbleFloat,
          onCauldronBoundsChange = { cauldronBounds = it },
          onRemoveIngredient = { ingredientIndex ->
            if (ingredientIndex in selectedIds.indices) {
              selectedIds.removeAt(ingredientIndex)
            }
            resetBrewProgress(keepIngredients = true)
          },
          onStirDragStart = {
            if (canStir && !isStirred && stirDragStartMs == null) {
              stirDragStartMs = SystemClock.elapsedRealtime()
            }
          },
          onStirDrag = { delta ->
            if (canStir && !isStirred) {
              stirrerOffset = Offset(
                x = (stirrerOffset.x + delta.x).coerceIn(-42f, 42f),
                y = (stirrerOffset.y + delta.y).coerceIn(-24f, 24f),
              )
            }
          },
          onStirDragEnd = {
            stirDragStartMs?.let { startedAt ->
              stirBaseMs += (SystemClock.elapsedRealtime() - startedAt)
            }
            stirDragStartMs = null
          },
          modifier = Modifier.weight(1f).fillMaxHeight(),
        )

        BrewingStepsPanel(
          selectedCount = selectedIds.size,
          stirProgress = stirProgress,
          selectedTone = selectedTone,
          canServe = canServe,
          onBack = onBack,
          onReset = { resetBrewProgress(keepIngredients = false) },
          onServe = { onServe(selectedIds.toList()) },
          modifier = Modifier.width(266.dp).fillMaxHeight(),
        )
      }

      // Bottom palette used to choose the brew tone/color.
      ColorRail(
        tones = tones,
        selectedToneIndex = selectedToneIndex,
        onSelectTone = { selectedToneIndex = it },
      )
    }

    // Floating ingredient sprite shown while dragging an item.
    dragState?.let { dragged ->
      DragIngredientOverlay(dragged)
    }
  }
}

@Composable
private fun BrewingTopBar(
  onBack: () -> Unit,
) {
  // Header bar with the brewing title asset and help affordance.
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF342823),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6B5443)),
      ) {
        Box(
          modifier = Modifier.size(width = 64.dp, height = 46.dp).clickable(onClick = onBack),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "←",
            color = Color(0xFFF0D39D),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
          )
        }
      }
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xE8342823),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6B5443)),
      ) {
        Text(
          text = "Приготовление",
          modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp),
          color = Color(0xFFF3E3CF),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(12.dp),
      color = Color(0xFF342823),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6B5443)),
    ) {
      Box(
        modifier = Modifier.size(46.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "?",
          color = Color(0xFFF0D39D),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
private fun IngredientRack(
  visuals: List<Pair<Ingredient, BrewIngredientVisual>>,
  selectedCounts: Map<String, Int>,
  canAddMore: Boolean,
  cauldronBounds: Rect?,
  onIngredientDropped: (Ingredient) -> Unit,
  onDragStateChange: (DragState?) -> Unit,
  modifier: Modifier = Modifier,
) {
  // Vertical rack of ingredient slots available for the current recipe.
  Box(modifier = modifier) {
    Column(
      modifier = Modifier.fillMaxSize().padding(end = 8.dp).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      visuals.forEach { (ingredient, visual) ->
        val selectedCount = selectedCounts[ingredient.id] ?: 0
        val enabled = canAddMore && selectedCount < ingredient.stockCount
        IngredientTile(
          visual = visual,
          selectedCount = selectedCount,
          enabled = enabled,
          onDropped = { onIngredientDropped(ingredient) },
          onDragStateChange = onDragStateChange,
          cauldronBounds = cauldronBounds,
          modifier = Modifier.height(90.dp).fillMaxWidth(),
        )
      }
    }
    Surface(
      modifier = Modifier.align(Alignment.BottomEnd),
      shape = RoundedCornerShape(999.dp),
      color = Color(0xCC2A1D18),
    ) {
      Text(
        text = "↕",
        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
        color = Color(0xFFF0D39D),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
private fun IngredientTile(
  visual: BrewIngredientVisual,
  selectedCount: Int,
  enabled: Boolean,
  cauldronBounds: Rect?,
  onDropped: () -> Unit,
  onDragStateChange: (DragState?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val backgroundRes =
    when {
      !enabled -> R.drawable.brew_slot_locked
      selectedCount > 0 -> R.drawable.brew_slot_selected
      else -> R.drawable.brew_slot_default
    }
  var originInRoot by remember { mutableStateOf(Offset.Zero) }
  var currentPosition by remember { mutableStateOf(Offset.Zero) }

  // Draggable ingredient slot that can drop its item into the cauldron.
  Box(
    modifier =
      modifier
        .onGloballyPositioned { coordinates ->
          val bounds = coordinates.boundsInRoot()
          originInRoot = Offset(bounds.left + bounds.width / 2f, bounds.top + bounds.height / 2f)
        }
        .pointerInput(enabled, selectedCount, cauldronBounds) {
          if (enabled) {
            detectDragGesturesAfterLongPress(
              onDragStart = {
                currentPosition = originInRoot
                onDragStateChange(DragState(visual, currentPosition))
              },
              onDrag = { change, dragAmount ->
                change.consume()
                currentPosition += dragAmount
                onDragStateChange(DragState(visual, currentPosition))
              },
              onDragEnd = {
                if (cauldronBounds?.contains(currentPosition) == true) {
                  onDropped()
                }
                onDragStateChange(null)
              },
              onDragCancel = { onDragStateChange(null) },
            )
          }
        },
  ) {
    Image(
      painter = painterResource(backgroundRes),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.FillBounds,
    )
    Image(
      painter = painterResource(visual.iconRes),
      contentDescription = visual.label,
      modifier = Modifier.align(Alignment.TopCenter).padding(top = 14.dp).size(50.dp),
      contentScale = ContentScale.Fit,
    )
    Text(
      text = "${visual.count - selectedCount}",
      modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 16.dp),
      color = Color(0xFFF4E8D4),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
private fun CauldronStage(
  selectedIngredients: List<Ingredient>,
  selectedTone: BrewTone?,
  stirProgress: Float,
  stirrerOffset: Offset,
  canStir: Boolean,
  ambientFloat: Float,
  bubbleFloat: Float,
  onCauldronBoundsChange: (Rect) -> Unit,
  onRemoveIngredient: (Int) -> Unit,
  onStirDragStart: () -> Unit,
  onStirDrag: (Offset) -> Unit,
  onStirDragEnd: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val liquidColor = selectedTone?.color ?: Color(0xFF4B6888)

  // Central brewing stage with the cauldron, ingredients, and stir control.
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    val ingredientOffsets =
      listOf(
        Offset(-74f, -18f),
        Offset(0f, -92f),
        Offset(76f, -8f),
      )

    // Large clipped interaction area around the cauldron.
    Box(
      modifier =
        Modifier
          .size(width = 560.dp, height = 430.dp)
          .clip(RoundedCornerShape(999.dp)),
    ) {
      // Colored liquid layer inside the cauldron.
      Box(
        modifier =
          Modifier
            .align(Alignment.Center)
            .offset(y = (-8).dp + ambientFloat.dp)
            .size(width = 236.dp, height = 90.dp)
            .clip(CircleShape)
            .background(liquidColor.copy(alpha = 0.44f)),
      )
      Box(
        modifier =
          Modifier
            .align(Alignment.Center)
            .offset(x = (-48).dp, y = (-18).dp + bubbleFloat.dp / 3)
            .size(18.dp)
            .clip(CircleShape)
            .background(liquidColor.copy(alpha = 0.26f)),
      )
      Box(
        modifier =
          Modifier
            .align(Alignment.Center)
            .offset(x = 42.dp, y = 8.dp - bubbleFloat.dp / 2)
            .size(12.dp)
            .clip(CircleShape)
            .background(liquidColor.copy(alpha = 0.34f)),
      )

      // Invisible drop target used for ingredient drag-and-drop.
      Box(
        modifier =
          Modifier
            .align(Alignment.Center)
            .offset(y = 10.dp)
            .size(width = 252.dp, height = 150.dp)
            .onGloballyPositioned { coordinates ->
              onCauldronBoundsChange(coordinates.boundsInRoot())
            },
      )

      // Draggable stirrer positioned over the cauldron.
      StirrerControl(
        canStir = canStir,
        stirrerOffset = stirrerOffset,
        onStirDragStart = onStirDragStart,
        onStirDrag = onStirDrag,
        onStirDragEnd = onStirDragEnd,
      )

      // Ingredient icons currently floating inside the brew.
      selectedIngredients.forEachIndexed { index, ingredient ->
        val visual = ingredientVisualForIngredient(ingredient)
        val ingredientOffset = ingredientOffsets.getOrElse(index) { Offset(0f, -48f) }
        Image(
          painter = painterResource(visual.iconRes),
          contentDescription = ingredient.name,
          modifier =
            Modifier
              .size(42.dp)
              .align(Alignment.Center)
              .offset(
                x = ingredientOffset.x.dp,
                y = ingredientOffset.y.dp + ambientFloat.dp / 2,
              ),
          contentScale = ContentScale.Fit,
        )
        Box(
          modifier =
            Modifier
              .size(56.dp)
              .align(Alignment.Center)
              .offset(
                x = ingredientOffset.x.dp,
                y = ingredientOffset.y.dp + ambientFloat.dp / 2,
              )
              .clickable { onRemoveIngredient(index) },
        )
      }

      // Bottom status pill describing brew progress.
      Surface(
        modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-6).dp),
        shape = RoundedCornerShape(999.dp),
        color = Color(0xAA1B140F),
      ) {
        Text(
          text =
            if (selectedIngredients.isEmpty()) {
              "Перетащи ингредиенты в котел"
            } else {
              "В котле: ${selectedIngredients.size}/3"
            },
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          color = Color(0xFFF3E5D2),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Composable
private fun BoxScope.StirrerControl(
  canStir: Boolean,
  stirrerOffset: Offset,
  onStirDragStart: () -> Unit,
  onStirDrag: (Offset) -> Unit,
  onStirDragEnd: () -> Unit,
) {
  // Stirrer handle that accumulates drag time toward the stirring goal.
  Image(
    painter = painterResource(R.drawable.brew_stirrer),
    contentDescription = null,
    modifier =
      Modifier
        .align(Alignment.Center)
        .offset(x = 82.dp, y = (-104).dp)
        .offset {
          IntOffset(
            x = stirrerOffset.x.roundToInt(),
            y = stirrerOffset.y.roundToInt(),
          )
        }
        .width(122.dp)
        .pointerInput(canStir) {
          detectDragGestures(
            onDragStart = { if (canStir) onStirDragStart() },
            onDrag = { change, dragAmount ->
              if (canStir) {
                change.consume()
                onStirDrag(dragAmount)
              }
            },
            onDragEnd = onStirDragEnd,
            onDragCancel = onStirDragEnd,
          )
        },
    contentScale = ContentScale.Fit,
  )
}

@Composable
private fun BrewingStepsPanel(
  selectedCount: Int,
  stirProgress: Float,
  selectedTone: BrewTone?,
  canServe: Boolean,
  onBack: () -> Unit = {},
  onReset: () -> Unit,
  onServe: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Right-side checklist that mirrors the brewing steps in order.
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    BrewStepCard(
      iconRes = R.drawable.brew_item_mushroom,
      title = "1. Добавь ингредиент",
      subtitle = "$selectedCount/3 в котле",
      modifier = Modifier.height(66.dp).fillMaxWidth(),
    )
    BrewStepCard(
      iconRes = R.drawable.brew_stirrer,
      title = "2. Перемешай",
      subtitle = "${(stirProgress * 2f).coerceAtMost(2f).formatOneDecimal()} / 2.0 сек",
      modifier = Modifier.height(66.dp).fillMaxWidth(),
      iconWide = true,
    )
    BrewStepCard(
      iconRes = R.drawable.brew_item_violet_flower,
      title = "3. Выбери цвет",
      subtitle = selectedTone?.label ?: "Оттенок не выбран",
      modifier = Modifier.height(66.dp).fillMaxWidth(),
      accent = selectedTone?.color,
    )
    BrewStepCard(
      iconRes = R.drawable.recipe_starglow_tonic,
      title = "4. Подай напиток",
      subtitle = if (canServe) "Напиток готов — нажми «Подать»" else "Нужно 3 ингредиента, перемешивание и цвет",
      modifier = Modifier.height(144.dp).fillMaxWidth(),
      footerButton = {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Button(
            onClick = onServe,
            enabled = canServe,
            modifier = Modifier.fillMaxWidth().height(30.dp),
          ) {
            Text(
              text = "Подать",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
            )
          }
          Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(28.dp),
          ) {
            Text(
              text = "Назад",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
            )
          }
          Button(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth().height(28.dp),
          ) {
            Text(
              text = "Сбросить",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      },
    )
  }
}

@Composable
private fun BrewStepCard(
  @DrawableRes iconRes: Int,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  iconWide: Boolean = false,
  accent: Color? = null,
  footerButton: @Composable (() -> Unit)? = null,
) {
  // Single step card with icon, current status text, and optional footer action.
  Box(modifier = modifier.clip(RoundedCornerShape(18.dp))) {
    Image(
      painter = painterResource(R.drawable.brew_step_panel),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.FillBounds,
    )
    Row(
      modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accent ?: Color(0x442E221D)),
        contentAlignment = Alignment.Center,
      ) {
        Image(
          painter = painterResource(iconRes),
          contentDescription = null,
          modifier = if (iconWide) Modifier.width(20.dp) else Modifier.size(24.dp),
          contentScale = ContentScale.Fit,
        )
      }

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(1.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
          color = Color(0xFFF2E1C6),
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelMedium,
          color = Color(0xFFD9C1A7),
          maxLines = 1,
        )
        footerButton?.invoke()
      }
    }
  }
}

@Composable
private fun ColorRail(
  tones: List<BrewTone>,
  selectedToneIndex: Int,
  onSelectTone: (Int) -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(999.dp),
    color = Color(0xFF2A1D18),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7A5A43)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Spacer(Modifier.width(52.dp))
      tones.forEachIndexed { index, tone ->
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(24.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(tone.color)
              .border(
                width = if (selectedToneIndex == index) 3.dp else 1.dp,
                color = if (selectedToneIndex == index) Color(0xFFF0D39D) else Color(0xFF4D362B),
                shape = RoundedCornerShape(8.dp),
              )
              .clickable { onSelectTone(index) },
        )
      }
      Spacer(Modifier.width(52.dp))
    }
  }
}

@Composable
private fun DragIngredientOverlay(dragged: DragState) {
  val density = LocalDensity.current
  val halfSizePx = with(density) { 26.dp.toPx() }
  Image(
    painter = painterResource(dragged.visual.iconRes),
    contentDescription = dragged.visual.label,
    modifier =
      Modifier
        .offset {
          IntOffset(
            x = (dragged.position.x - halfSizePx).roundToInt(),
            y = (dragged.position.y - halfSizePx).roundToInt(),
          )
        }
        .size(52.dp),
    contentScale = ContentScale.Fit,
  )
}

private fun ingredientVisualsForScenario(scenario: GameScenario): List<Pair<Ingredient, BrewIngredientVisual>> =
  scenario.availableIngredients.map { ingredient ->
    ingredient to ingredientVisualForIngredient(ingredient)
  }

private fun ingredientVisualForIngredient(ingredient: Ingredient): BrewIngredientVisual =
  when (ingredient.id) {
    "silverfoam" -> BrewIngredientVisual(R.drawable.brew_item_crystal, ingredient.stockCount, ingredient.name)
    "moonmint" -> BrewIngredientVisual(R.drawable.brew_item_leaf_bundle, ingredient.stockCount, ingredient.name)
    "emberzest" -> BrewIngredientVisual(R.drawable.brew_item_ginger_root, ingredient.stockCount, ingredient.name)
    "dusk-syrup" -> BrewIngredientVisual(R.drawable.brew_item_honey_bottle, ingredient.stockCount, ingredient.name)
    "frost-thyme" -> BrewIngredientVisual(R.drawable.brew_item_violet_flower, ingredient.stockCount, ingredient.name)
    "cinderbloom" -> BrewIngredientVisual(R.drawable.brew_item_mushroom, ingredient.stockCount, ingredient.name)
    else -> BrewIngredientVisual(R.drawable.brew_item_blackberry, ingredient.stockCount, ingredient.name)
  }

private fun currentStirElapsedMs(baseMs: Long, dragStartMs: Long?): Long =
  if (dragStartMs == null) {
    baseMs
  } else {
    baseMs + (SystemClock.elapsedRealtime() - dragStartMs)
  }

private fun Float.formatOneDecimal(): String = String.format(Locale.ROOT, "%.1f", this)

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun BrewingScreenPreview() {
  MoonbrewTavernTheme {
    BrewingScreen(
      scenario = DefaultDataRepository().scenario,
      onBack = {},
      onServe = {},
    )
  }
}
