package com.example.moonbrewtavern.ui.entrance

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.data.content.firstNightScenario
import com.example.moonbrewtavern.data.content.ContentCatalog
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.NightState
import com.example.moonbrewtavern.domain.model.VisitorDefinition
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme

@Composable
fun EntranceScreen(
  gameState: GameState,
  nightState: NightState,
  visitorDefinitions: Map<String, VisitorDefinition>,
  onAdmit: (String) -> Unit,
  onReject: (String) -> Unit,
  onEnterTavern: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val freeSeats = (gameState.tavern.capacity - nightState.seatedVisitorIds.size).coerceAtLeast(0)

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(Color(0xFFBBD8EF)),
  ) {
    Image(
      painter = painterResource(R.drawable.tavern_bg),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(Color(0x22000000), Color(0x11000000), Color(0x660E0A08)),
            ),
          ),
    )

    Column(
      modifier = Modifier.fillMaxSize().padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Image(
          painter = painterResource(R.drawable.ui_sign),
          contentDescription = null,
          modifier = Modifier.width(260.dp),
          contentScale = ContentScale.FillWidth,
        )
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xD92A1D18),
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text("Ночь ${gameState.day}", color = Color(0xFFF4E7C6), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("Свободно мест: $freeSeats/${gameState.tavern.capacity}", color = Color(0xFFE6CFAF), style = MaterialTheme.typography.bodyMedium)
              Text("Золото ${gameState.gold} • Репутация ${gameState.reputation}", color = Color(0xFFE6CFAF), style = MaterialTheme.typography.bodyMedium)
            }
          }

          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End,
          ) {
            Text(
              text =
                if (nightState.seatedVisitorIds.isNotEmpty()) {
                  "Гости готовы. Можно открывать зал."
                } else {
                  "Сначала впусти хотя бы одного гостя."
                },
              color = Color(0xFFE6CFAF),
              style = MaterialTheme.typography.bodySmall,
            )
            Button(
              onClick = onEnterTavern,
              enabled = nightState.seatedVisitorIds.isNotEmpty(),
              modifier = Modifier.width(220.dp).height(54.dp),
            ) {
              Text("Открыть зал", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }

      Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color(0xC21D1412),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text("Очередь у входа", style = MaterialTheme.typography.titleLarge, color = Color(0xFFF3E3CF), fontWeight = FontWeight.Bold)
            Text(
              "Игрок решает, кого впустить этой ночью. Как только в зале будет хотя бы один гость, можно открыть двери и перейти внутрь.",
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFFD9C1A7),
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          nightState.queueVisitorIds.forEach { visitorId ->
            val definition = visitorDefinitions[visitorId] ?: return@forEach
            QueueCard(
              definition = definition,
              admitEnabled = freeSeats > 0,
              onAdmit = { onAdmit(visitorId) },
              onReject = { onReject(visitorId) },
            )
          }
        }

        if (nightState.seatedVisitorIds.isNotEmpty()) {
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xC1231916),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text("Уже допущены", style = MaterialTheme.typography.titleMedium, color = Color(0xFFF3E3CF), fontWeight = FontWeight.SemiBold)
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                nightState.seatedVisitorIds.forEach { visitorId ->
                  val definition = visitorDefinitions[visitorId] ?: return@forEach
                  Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFF4A362B)) {
                    Text(
                      text = definition.name,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                      color = Color(0xFFF4E7C6),
                      style = MaterialTheme.typography.labelLarge,
                    )
                  }
                }
              }
            }
          }
        }
        }
      }
  }
}

@Composable
private fun QueueCard(
  definition: VisitorDefinition,
  admitEnabled: Boolean,
  onAdmit: () -> Unit,
  onReject: () -> Unit,
) {
  Surface(
    modifier = Modifier.width(180.dp),
    shape = RoundedCornerShape(22.dp),
    color = Color(0xD1261C19),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF3C2A25))
            .clickable(enabled = admitEnabled, onClick = onAdmit),
        contentAlignment = Alignment.Center,
      ) {
        Image(
          painter = painterResource(definition.assets.queueRes),
          contentDescription = definition.name,
          modifier = Modifier.height(110.dp),
          contentScale = ContentScale.FillHeight,
        )
      }
      Text(definition.name, style = MaterialTheme.typography.titleSmall, color = Color(0xFFF3E3CF), fontWeight = FontWeight.Bold)
      Text(definition.title, style = MaterialTheme.typography.labelSmall, color = Color(0xFFD9C1A7))
      Text(
        text = if (admitEnabled) "Тап по гостю тоже впускает его в таверну." else "Свободных мест больше нет.",
        color = Color(0xFFCDB49A),
        style = MaterialTheme.typography.labelSmall,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Button(
          onClick = onAdmit,
          enabled = admitEnabled,
          modifier = Modifier.weight(1f),
        ) {
          Text("Впустить")
        }
        OutlinedButton(
          onClick = onReject,
          modifier = Modifier.weight(1f),
        ) {
          Text("Отказать")
        }
      }
    }
  }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun EntranceScreenPreview() {
  MoonbrewTavernTheme {
    EntranceScreen(
      gameState = firstNightScenario.initialState,
      nightState = NightState(queueVisitorIds = ContentCatalog.starterQueueVisitorIds),
      visitorDefinitions = ContentCatalog.visitorDefinitionsById,
      onAdmit = {},
      onReject = {},
      onEnterTavern = {},
    )
  }
}
