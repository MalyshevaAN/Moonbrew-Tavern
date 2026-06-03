package com.example.moonbrewtavern.ui.tavernroom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.data.content.ContentCatalog
import com.example.moonbrewtavern.data.content.firstNightScenario
import com.example.moonbrewtavern.domain.model.GameLoopConfig
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.NightState
import com.example.moonbrewtavern.domain.model.TavernGuestStatus
import com.example.moonbrewtavern.domain.model.VisitorDefinition
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme
import com.example.moonbrewtavern.ui.common.resolveVisitorDefinition
import java.util.Locale

@Composable
fun TavernRoomScreen(
  gameState: GameState,
  nightState: NightState,
  visitorDefinitions: Map<String, VisitorDefinition>,
  onGuestClick: (String) -> Unit,
  onBackToStreet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
  ) {
    // Общий фон внутренней таверны.
    Image(
      painter = painterResource(R.drawable.tavern_room_background),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    // Бармен за стойкой.
    Image(
      painter = painterResource(R.drawable.tavern_room_bartender),
      contentDescription = null,
      modifier = Modifier.align(Alignment.TopCenter).offset(x = (-128).dp, y = 56.dp).width(86.dp),
      contentScale = ContentScale.FillWidth,
    )

    TavernRoomOverlay(
      gameState = gameState,
      nightState = nightState,
      visitorDefinitions = visitorDefinitions,
      onGuestClick = onGuestClick,
      onBackToStreet = onBackToStreet,
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp),
    )
  }
}

@Composable
private fun TavernRoomOverlay(
  gameState: GameState,
  nightState: NightState,
  visitorDefinitions: Map<String, VisitorDefinition>,
  onGuestClick: (String) -> Unit,
  onBackToStreet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    // Левая верхняя панель задач.
    Image(
      painter = painterResource(R.drawable.tavern_room_task_panel),
      contentDescription = null,
      modifier = Modifier.width(148.dp).align(Alignment.TopStart).offset(y = 6.dp),
      contentScale = ContentScale.FillWidth,
    )

    // Кнопка возврата на уличную сцену.
    StreetBackButton(
      onClick = onBackToStreet,
      modifier = Modifier.align(Alignment.TopStart).offset(x = 4.dp, y = 150.dp),
    )

    // Верхняя плашка с ресурсами.
    Image(
      painter = painterResource(R.drawable.tavern_room_resource_bar),
      contentDescription = null,
      modifier = Modifier.width(302.dp).align(Alignment.TopEnd).offset(x = (-92).dp, y = 8.dp),
      contentScale = ContentScale.FillWidth,
    )

    // Иконки быстрых действий справа сверху.
    Row(
      modifier = Modifier.align(Alignment.TopEnd).offset(y = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      IconTile(R.drawable.tavern_room_storage_icon, "Склад")
      IconTile(R.drawable.tavern_room_upgrades_icon, "Улучшения")
    }

    val seatAnchors =
      listOf(
        Modifier.align(Alignment.BottomStart).offset(x = 118.dp, y = (-38).dp),
        Modifier.align(Alignment.BottomCenter).offset(x = (-16).dp, y = (-54).dp),
        Modifier.align(Alignment.BottomEnd).offset(x = (-116).dp, y = (-34).dp),
      )

    nightState.seatedVisitorIds.take(seatAnchors.size).forEachIndexed { index, visitorId ->
      val definition = resolveVisitorDefinition(visitorId, visitorDefinitions) ?: return@forEachIndexed
      val guest = nightState.guests.firstOrNull { it.visitorId == visitorId } ?: return@forEachIndexed
      val isCurrent = visitorId == nightState.currentVisitorId
      val statusRes =
        when (guest.status) {
          TavernGuestStatus.WaitingForOrder -> R.drawable.tavern_room_status_wants_beer
          TavernGuestStatus.Drinking -> R.drawable.tavern_room_status_drinking
          TavernGuestStatus.WantsToLeave -> R.drawable.tavern_room_status_wants_leave
        }
      val statusLabel =
        when (guest.status) {
          TavernGuestStatus.WaitingForOrder -> "${definition.name} ждет заказ"
          TavernGuestStatus.Drinking -> "Пьет"
          TavernGuestStatus.WantsToLeave -> "Хочет уйти"
        }

      GuestAtTable(
        guestRes = definition.assets.tavernSeatRes,
        statusRes = statusRes,
        statusLabel = statusLabel,
        name = definition.name,
        highlighted = isCurrent || guest.status == TavernGuestStatus.WantsToLeave,
        drinkSecondsLeft =
          if (guest.status == TavernGuestStatus.Drinking) {
            (guest.drinkRemainingMs / 1000L).toInt()
          } else {
            null
          },
        onClick = { onGuestClick(visitorId) },
        modifier = seatAnchors[index],
      )
    }

    if (nightState.seatedVisitorIds.isEmpty()) {
      Surface(
        modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-70).dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xCC281B18),
      ) {
        Text(
          text = "Пока пусто. На улице еще ждут путники.",
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
          color = Color(0xFFF4E7C6),
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }

    Surface(
      modifier = Modifier.align(Alignment.TopCenter).offset(y = 8.dp),
      shape = RoundedCornerShape(16.dp),
      color = Color(0xCC281B18),
    ) {
      Text(
        text =
          "Ночь ${formatMillisAsClock(nightState.remainingNightMs)} • " +
            "В зале ${nightState.seatedVisitorIds.size}/${gameState.tavern.capacity} • " +
            "Золото ${gameState.gold} • Репутация ${gameState.reputation}",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        color = Color(0xFFF3E5C9),
        style = MaterialTheme.typography.labelLarge,
      )
    }
  }
}

@Composable
private fun IconTile(
  drawable: Int,
  label: String,
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    // Маленькая квадратная кнопка в правом верхнем углу.
    Box(
      modifier =
        Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xAA2A1E1A))
          .border(1.dp, Color(0xFF795D45), RoundedCornerShape(12.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Image(
        painter = painterResource(drawable),
        contentDescription = label,
        modifier = Modifier.size(22.dp),
        contentScale = ContentScale.Fit,
      )
    }
  }
}

@Composable
private fun StreetBackButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Простая кнопка возврата на уличную сцену.
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(999.dp))
        .background(Color(0xD22A1D18))
        .border(1.dp, Color(0xFF7C6048), RoundedCornerShape(999.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 7.dp),
  ) {
    Text(
      text = "На улицу",
      color = Color(0xFFF3E5C9),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

@Composable
private fun GuestAtTable(
  guestRes: Int,
  statusRes: Int,
  statusLabel: String,
  name: String,
  drinkSecondsLeft: Int? = null,
  modifier: Modifier = Modifier,
  highlighted: Boolean = false,
  onClick: () -> Unit,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Кружок-статус над головой гостя.
    Box(
      modifier =
        Modifier
          .size(if (highlighted) 44.dp else 40.dp)
          .clip(CircleShape)
          .background(Color(0xCC281B18))
          .border(
            width = if (highlighted) 2.dp else 1.dp,
            color = if (highlighted) Color(0xFFF0D28B) else Color(0xFF675244),
            shape = CircleShape,
          ),
      contentAlignment = Alignment.Center,
    ) {
      Image(
        painter = painterResource(statusRes),
        contentDescription = statusLabel,
        modifier = Modifier.size(if (highlighted) 26.dp else 22.dp),
        contentScale = ContentScale.Fit,
      )
    }

    // Сам спрайт гостя за столом.
    Box(
      modifier =
        Modifier
          .padding(top = 10.dp)
          .clip(RoundedCornerShape(18.dp))
          .then(
            if (highlighted) {
              Modifier.border(2.dp, Color(0xFFF0D28B), RoundedCornerShape(18.dp))
            } else {
              Modifier
            },
          )
          .clickable(onClick = onClick),
    ) {
      Image(
        painter = painterResource(guestRes),
        contentDescription = statusLabel,
        modifier = Modifier.width(if (highlighted) 92.dp else 82.dp),
        contentScale = ContentScale.FillWidth,
      )
    }

    if (highlighted) {
      // Подпись под активным гостем.
      Box(
        modifier =
          Modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xD9281D18))
            .padding(horizontal = 12.dp, vertical = 5.dp),
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = name,
            color = Color(0xFFF4E7C6),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
          )
          if (drinkSecondsLeft != null) {
            Text(
              text = "${drinkSecondsLeft}s",
              color = Color(0xFFE8C89A),
              style = MaterialTheme.typography.labelSmall,
            )
          }
        }
      }
    }
  }
}

private fun formatMillisAsClock(millis: Long): String {
  val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
  val minutes = totalSeconds / 60L
  val seconds = totalSeconds % 60L
  return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun TavernRoomScreenPreview() {
  MoonbrewTavernTheme {
    TavernRoomScreen(
      gameState = firstNightScenario.initialState.copy(tavern = firstNightScenario.initialState.tavern.copy(occupiedSeats = 2)),
      nightState =
        NightState(
          guests =
            listOf(
              com.example.moonbrewtavern.domain.model.TavernGuest(visitorId = "brann", status = TavernGuestStatus.Drinking, drinkRemainingMs = GameLoopConfig.guestDrinkDurationMs),
              com.example.moonbrewtavern.domain.model.TavernGuest(visitorId = "lyra", status = TavernGuestStatus.WaitingForOrder),
            ),
          currentVisitorId = "lyra",
          remainingNightMs = 4 * 60 * 1000L + 12_000L,
        ),
      visitorDefinitions = ContentCatalog.visitorDefinitionsById,
      onGuestClick = {},
      onBackToStreet = {},
    )
  }
}
