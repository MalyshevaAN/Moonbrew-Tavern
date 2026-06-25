package com.example.moonbrewtavern.data.persistence

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataMigration
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import com.example.moonbrewtavern.domain.model.BrewResult
import com.example.moonbrewtavern.domain.model.GameState
import com.example.moonbrewtavern.domain.model.NightSummary
import com.example.moonbrewtavern.domain.model.NightState
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Read/write contract for a single persisted game snapshot. */
interface GameSaveStore {
  suspend fun read(): PersistedGameSnapshot?

  suspend fun write(snapshot: PersistedGameSnapshot)
}

/** Serializable save payload for the current prototype progression model. */
@Serializable
data class PersistedGameSnapshot(
  val gameState: GameState,
  val nightState: NightState,
  val activeRecipeId: String,
  val lastBrewResult: BrewResult?,
  val lastNightSummary: NightSummary? = null,
)

/** Envelope used by typed DataStore so the absence of a save is explicit. */
@Serializable
data class PersistedGameStore(
  val snapshot: PersistedGameSnapshot? = null,
)

/** Typed DataStore-backed implementation for the single game save slot. */
class DataStoreGameSaveStore(
  private val context: Context,
) : GameSaveStore {
  override suspend fun read(): PersistedGameSnapshot? = context.gameSaveDataStore.data.first().snapshot

  override suspend fun write(snapshot: PersistedGameSnapshot) {
    context.gameSaveDataStore.updateData { current ->
      current.copy(snapshot = snapshot)
    }
  }
}

private val gameSaveJson =
  Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

private object PersistedGameStoreSerializer : Serializer<PersistedGameStore> {
  override val defaultValue: PersistedGameStore = PersistedGameStore()

  override suspend fun readFrom(input: InputStream): PersistedGameStore =
    try {
      val raw = input.readBytes().decodeToString()
      if (raw.isBlank()) {
        defaultValue
      } else {
        gameSaveJson.decodeFromString<PersistedGameStore>(raw)
      }
    } catch (exception: SerializationException) {
      throw CorruptionException("Unable to read persisted game snapshot.", exception)
    }

  override suspend fun writeTo(
    t: PersistedGameStore,
    output: OutputStream,
  ) {
    output.write(gameSaveJson.encodeToString(PersistedGameStore.serializer(), t).encodeToByteArray())
  }
}

private val Context.gameSaveDataStore by dataStore(
  fileName = "moonbrew_save.json",
  serializer = PersistedGameStoreSerializer,
  corruptionHandler = ReplaceFileCorruptionHandler { PersistedGameStore() },
  produceMigrations = { appContext ->
    listOf(LegacySharedPreferencesGameSaveMigration(appContext))
  },
)

/**
 * One-time migration from the earlier SharedPreferences snapshot so existing local saves survive
 * the switch to typed DataStore.
 */
private class LegacySharedPreferencesGameSaveMigration(
  context: Context,
) : DataMigration<PersistedGameStore> {
  private val preferences = context.getSharedPreferences(LEGACY_PREFERENCES_NAME, Context.MODE_PRIVATE)

  override suspend fun shouldMigrate(currentData: PersistedGameStore): Boolean =
    currentData.snapshot == null && preferences.contains(LEGACY_SNAPSHOT_KEY)

  override suspend fun migrate(currentData: PersistedGameStore): PersistedGameStore {
    val raw = preferences.getString(LEGACY_SNAPSHOT_KEY, null) ?: return currentData
    val migratedSnapshot = runCatching { gameSaveJson.decodeFromString<PersistedGameSnapshot>(raw) }.getOrNull()
    return if (migratedSnapshot == null) currentData else currentData.copy(snapshot = migratedSnapshot)
  }

  override suspend fun cleanUp() {
    preferences.edit().remove(LEGACY_SNAPSHOT_KEY).apply()
  }

  private companion object {
    const val LEGACY_PREFERENCES_NAME = "moonbrew_save"
    const val LEGACY_SNAPSHOT_KEY = "snapshot"
  }
}
