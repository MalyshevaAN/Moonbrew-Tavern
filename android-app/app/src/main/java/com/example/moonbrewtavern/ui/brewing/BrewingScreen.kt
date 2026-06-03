package com.example.moonbrewtavern.ui.brewing

import android.os.SystemClock
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import java.util.Locale
import kotlin.math.roundToInt

private data class BrewTone(
  val label: String,
  val color: Color,
)

private data class BrewIngredientVisual(
  @DrawableRes val iconRes: Int,
  val count: Int,
  val label: String,
)

private data class DragState(
  val ingredient: Ingredient,
  val visual: BrewIngredientVisual,
  val position: Offset,
)

/** Brewing minigame where the player drags ingredients, stirs, and serves the order. */
@Composable
fun BrewingScreen(
  scenario: GameScenario,
  onServe: (Set<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val ingredientVisuals = remember(scenario.availableIngredients) { ingredientVisualsForScenario(scenario) }
  val selectedIds = remember { mutableStateListOf<String>() }
  var selectedToneIndex by rememberSaveable { mutableStateOf(-1) }
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
  val selectedIngredients = scenario.availableIngredients.filter { it.id in selectedIds }
  val canAddMore = selectedIds.size < 3
  val stirElapsedMs = currentStirElapsedMs(stirBaseMs, stirDragStartMs)
  val stirProgress = (stirElapsedMs / 2000f).coerceIn(0f, 1f)
  val isStirred = stirElapsedMs >= 2000L
  val canStir = selectedIngredients.isNotEmpty()
  val canServe = selectedIds.size == 3 && isStirred && selectedTone != null

  Box(modifier = modifier.fillMaxSize()) {
    Image(
      painter = painterResource(R.drawable.brew_scene_background),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      BrewingTopBar()

      Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        IngredientRack(
          visuals = ingredientVisuals,
          selectedIds = selectedIds,
          canAddMore = canAddMore,
          onIngredientDropped = { ingredient ->
            if (ingredient.id !in selectedIds && selectedIds.size < 3) {
              selectedIds.add(ingredient.id)
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
          onCauldronBoundsChange = { cauldronBounds = it },
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
          onServe = { onServe(selectedIds.toSet()) },
          modifier = Modifier.width(266.dp).fillMaxHeight(),
        )
      }

      ColorRail(
        tones = tones,
        selectedToneIndex = selectedToneIndex,
        onSelectTone = { selectedToneIndex = it },
      )
    }

    dragState?.let { dragged ->
      DragIngredientOverlay(dragged)
    }
  }
}

@Composable
private fun BrewingTopBar() {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Image(
      painter = painterResource(R.drawable.brew_header_bar),
      contentDescription = null,
      modifier = Modifier.width(296.dp),
      contentScale = ContentScale.FillWidth,
    )

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
  selectedIds: List<String>,
  canAddMore: Boolean,
  cauldronBounds: Rect?,
  onIngredientDropped: (Ingredient) -> Unit,
  onDragStateChange: (DragState?) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    visuals.take(4).forEach { (ingredient, visual) ->
      val isSelected = ingredient.id in selectedIds
      val enabled = isSelected || canAddMore
      IngredientTile(
        visual = visual,
        selected = isSelected,
        enabled = enabled,
        onDropped = { onIngredientDropped(ingredient) },
        onDragStateChange = onDragStateChange,
        cauldronBounds = cauldronBounds,
        ingredient = ingredient,
        modifier = Modifier.weight(1f).fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun IngredientTile(
  ingredient: Ingredient,
  visual: BrewIngredientVisual,
  selected: Boolean,
  enabled: Boolean,
  cauldronBounds: Rect?,
  onDropped: () -> Unit,
  onDragStateChange: (DragState?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val backgroundRes =
    when {
      !enabled -> R.drawable.brew_slot_locked
      selected -> R.drawable.brew_slot_selected
      else -> R.drawable.brew_slot_default
    }
  var originInRoot by remember { mutableStateOf(Offset.Zero) }
  var currentPosition by remember { mutableStateOf(Offset.Zero) }

  Box(
    modifier =
      modifier
        .onGloballyPositioned { coordinates ->
          val bounds = coordinates.boundsInRoot()
          originInRoot = Offset(bounds.left + bounds.width / 2f, bounds.top + bounds.height / 2f)
        }
        .pointerInput(enabled, selected, cauldronBounds) {
          detectDragGestures(
            onDragStart = {
              if (enabled && !selected) {
                currentPosition = originInRoot
                onDragStateChange(DragState(ingredient, visual, currentPosition))
              }
            },
            onDrag = { change, dragAmount ->
              change.consume()
              if (enabled && !selected) {
                currentPosition += dragAmount
                onDragStateChange(DragState(ingredient, visual, currentPosition))
              }
            },
            onDragEnd = {
              if (enabled && !selected && cauldronBounds?.contains(currentPosition) == true) {
                onDropped()
              }
              onDragStateChange(null)
            },
            onDragCancel = { onDragStateChange(null) },
          )
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
      text = visual.count.toString(),
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
  onCauldronBoundsChange: (Rect) -> Unit,
  onStirDragStart: () -> Unit,
  onStirDrag: (Offset) -> Unit,
  onStirDragEnd: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val liquidColor = selectedTone?.color ?: Color(0xFF4B6888)
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

    Box(
      modifier =
        Modifier
          .size(width = 560.dp, height = 430.dp)
          .clip(RoundedCornerShape(999.dp)),
    ) {
      Box(
        modifier =
          Modifier
            .align(Alignment.Center)
            .offset(y = (-8).dp)
            .size(width = 236.dp, height = 90.dp)
            .clip(CircleShape)
            .background(liquidColor.copy(alpha = 0.34f)),
      )

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

      StirrerControl(
        canStir = canStir,
        stirrerOffset = stirrerOffset,
        onStirDragStart = onStirDragStart,
        onStirDrag = onStirDrag,
        onStirDragEnd = onStirDragEnd,
      )

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
                y = ingredientOffset.y.dp,
              ),
          contentScale = ContentScale.Fit,
        )
      }

      Surface(
        modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-6).dp),
        shape = RoundedCornerShape(999.dp),
        color = Color(0xAA1B140F),
      ) {
        Text(
          text = if (selectedIngredients.isEmpty()) "Перетащи ингредиенты в котел" else "В котле: ${selectedIngredients.size}/3 • Перемешано: ${(stirProgress * 100).toInt()}%",
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
  onServe: () -> Unit,
  modifier: Modifier = Modifier,
) {
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
      iconRes = R.drawable.brew_item_honey_bottle,
      title = "4. Разлей",
      subtitle = if (canServe) "Можно подавать" else "Не все шаги завершены",
      modifier = Modifier.height(78.dp).fillMaxWidth(),
      footerButton = {
        Button(
          onClick = onServe,
          enabled = canServe,
          modifier = Modifier.fillMaxWidth().height(26.dp),
        ) {
          Text(
            text = "Подать",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
          )
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
      onServe = {},
    )
  }
}
