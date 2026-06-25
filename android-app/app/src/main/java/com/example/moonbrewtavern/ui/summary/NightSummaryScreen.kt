package com.example.moonbrewtavern.ui.summary

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.domain.model.NightSummary
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme

/** End-of-night screen shown before the next queue starts. */
@Composable
fun NightSummaryScreen(
  summary: NightSummary,
  onStartNextNight: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize()) {
    Image(
      painter = painterResource(R.drawable.tavern_room_background),
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
              listOf(Color(0xE8130F0D), Color(0xC3201714), Color(0xF015100E)),
            ),
          ),
    )

    Surface(
      modifier = Modifier.align(Alignment.Center).width(620.dp),
      shape = RoundedCornerShape(28.dp),
      color = Color(0xEE261A16),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF76543E)),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        Text(
          text = "Ночь ${summary.completedDay} завершена",
          color = Color(0xFFF2D099),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "Свечи догорают, скамьи пустеют, и Moonbrew Tavern ненадолго затихает перед новым вечером.",
          color = Color(0xFFF5E6D3),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.SemiBold,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          SummaryTile("Золото", summary.gold.toString(), Modifier.weight(1f))
          SummaryTile("Репутация", summary.reputation.toString(), Modifier.weight(1f))
          SummaryTile("Открыто рецептов", summary.unlockedRecipes.toString(), Modifier.weight(1f))
        }

        SummaryTile(
          label = "Знакомые гости",
          value = summary.relationshipsTracked.toString(),
          modifier = Modifier.fillMaxWidth(),
        )

        Text(
          text = "Дальше: ночь ${summary.nextDay}",
          color = Color(0xFFD9C3AC),
          style = MaterialTheme.typography.bodyLarge,
        )

        Button(
          onClick = onStartNextNight,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "Начать следующую ночь",
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}

@Composable
private fun SummaryTile(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    color = Color(0xFF34241E),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF654A37)),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = label,
        color = Color(0xFFD6BDA2),
        style = MaterialTheme.typography.labelLarge,
      )
      Text(
        text = value,
        color = Color(0xFFF5E6D3),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun NightSummaryScreenPreview() {
  MoonbrewTavernTheme {
    NightSummaryScreen(
      summary =
        NightSummary(
          completedDay = 3,
          nextDay = 4,
          gold = 18,
          reputation = 6,
          unlockedRecipes = 2,
          relationshipsTracked = 3,
        ),
      onStartNextNight = {},
    )
  }
}
