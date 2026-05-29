package com.example.moonbrewtavern.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.moonbrewtavern.data.DefaultDataRepository
import com.example.moonbrewtavern.theme.MoonbrewTavernTheme

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(DefaultDataRepository()) },
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  when (state) {
    MainScreenUiState.Loading -> {
      MainScreenContent(
        title = "Moonbrew Tavern",
        subtitle = "Loading tavern...",
        labels = listOf("android", "compose", "bootstrap"),
        modifier = modifier,
      ) {
        CircularProgressIndicator()
      }
    }
    is MainScreenUiState.Success -> {
      MainScreenContent(
        title = "Moonbrew Tavern",
        subtitle = "Prototype is running",
        labels = (state as MainScreenUiState.Success).data,
        modifier = modifier,
      )
    }
    is MainScreenUiState.Error -> {
      MainScreenContent(
        title = "Moonbrew Tavern",
        subtitle = "Failed to load initial data",
        labels = listOf((state as MainScreenUiState.Error).throwable.message ?: "Unknown error"),
        modifier = modifier,
      )
    }
  }
}

@Composable
private fun MainScreenContent(
  title: String,
  subtitle: String,
  labels: List<String>,
  modifier: Modifier = Modifier,
  indicator: @Composable (() -> Unit)? = null,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(horizontal = 24.dp, vertical = 32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    TavernMarker()
    Spacer(Modifier.height(24.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
    if (indicator != null) {
      Spacer(Modifier.height(20.dp))
      indicator()
    }
    Spacer(Modifier.height(28.dp))
    StatusPanel(labels = labels)
  }
}

@Composable
private fun TavernMarker() {
  Box(
    modifier =
      Modifier
        .size(120.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(MaterialTheme.colorScheme.primaryContainer)
        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier =
        Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.primary),
    )
  }
}

@Composable
private fun StatusPanel(labels: List<String>) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(
      text = "Status",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
    )
    labels.forEach { label ->
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
  MoonbrewTavernTheme {
    MainScreenContent(
      title = "Moonbrew Tavern",
      subtitle = "Prototype is running",
      labels = listOf("android", "compose", "bootstrap"),
    )
  }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
fun MainScreenPortraitPreview() {
  MoonbrewTavernTheme {
    MainScreenContent(
      title = "Moonbrew Tavern",
      subtitle = "Prototype is running",
      labels = listOf("android", "compose", "bootstrap"),
    )
  }
}
