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
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme

@Composable
fun TavernRoomScreen(
  onGuestClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
  ) {
    Image(
      painter = painterResource(R.drawable.tavern_room_background),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    Image(
      painter = painterResource(R.drawable.tavern_room_bartender),
      contentDescription = null,
      modifier = Modifier.align(Alignment.TopCenter).offset(x = (-108).dp, y = 188.dp).width(158.dp),
      contentScale = ContentScale.FillWidth,
    )

    TavernRoomOverlay(
      onGuestClick = onGuestClick,
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp),
    )
  }
}

@Composable
private fun TavernRoomOverlay(
  onGuestClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    Image(
      painter = painterResource(R.drawable.tavern_room_task_panel),
      contentDescription = null,
      modifier = Modifier.width(152.dp).align(Alignment.TopStart),
      contentScale = ContentScale.FillWidth,
    )

    Image(
      painter = painterResource(R.drawable.tavern_room_resource_bar),
      contentDescription = null,
      modifier = Modifier.width(318.dp).align(Alignment.TopEnd).offset(x = (-96).dp),
      contentScale = ContentScale.FillWidth,
    )

    Row(
      modifier = Modifier.align(Alignment.TopEnd),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      IconTile(R.drawable.tavern_room_storage_icon, "Склад")
      IconTile(R.drawable.tavern_room_upgrades_icon, "Улучшения")
    }

    GuestAtTable(
      guestRes = R.drawable.tavern_room_guest_one,
      statusRes = R.drawable.tavern_room_status_drinking,
      statusLabel = "Пьет",
      modifier = Modifier.align(Alignment.BottomStart).offset(x = 94.dp, y = (-18).dp),
    )

    GuestAtTable(
      guestRes = R.drawable.tavern_room_guest_two,
      statusRes = R.drawable.tavern_room_status_wants_beer,
      statusLabel = "Лира ждет заказ",
      highlighted = true,
      onClick = onGuestClick,
      modifier = Modifier.align(Alignment.BottomCenter).offset(x = (-18).dp, y = (-8).dp),
    )

    GuestAtTable(
      guestRes = R.drawable.tavern_room_guest_three,
      statusRes = R.drawable.tavern_room_status_wants_leave,
      statusLabel = "Хочет уйти",
      modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-94).dp, y = (-20).dp),
    )
  }
}

@Composable
private fun IconTile(
  drawable: Int,
  label: String,
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier =
        Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xAA2A1E1A))
          .border(1.dp, Color(0xFF795D45), RoundedCornerShape(12.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Image(
        painter = painterResource(drawable),
        contentDescription = label,
        modifier = Modifier.size(26.dp),
        contentScale = ContentScale.Fit,
      )
    }
  }
}

@Composable
private fun GuestAtTable(
  guestRes: Int,
  statusRes: Int,
  statusLabel: String,
  modifier: Modifier = Modifier,
  highlighted: Boolean = false,
  onClick: (() -> Unit)? = null,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier =
        Modifier
          .size(56.dp)
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
        modifier = Modifier.size(34.dp),
        contentScale = ContentScale.Fit,
      )
    }

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
          .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
      Image(
        painter = painterResource(guestRes),
        contentDescription = statusLabel,
        modifier = Modifier.width(if (highlighted) 150.dp else 132.dp),
        contentScale = ContentScale.FillWidth,
      )
    }

    if (highlighted) {
      Box(
        modifier =
          Modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xD9281D18))
            .padding(horizontal = 12.dp, vertical = 5.dp),
      ) {
        Text(
          text = "Лира",
          color = Color(0xFFF4E7C6),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun TavernRoomScreenPreview() {
  MoonbrewTavernTheme {
    TavernRoomScreen(onGuestClick = {})
  }
}
э