package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.InferenceSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studioDataStore: DataStore<Preferences> by preferencesDataStore(name = "studio_settings")

data class PersistedModelRef(
    val path: String = "",
    val displayName: String = "",
    val sizeBytes: Long = 0L
)

class StudioPreferencesRepository(private val context: Context) {

    private object Keys {
        val MODEL_PATH = stringPreferencesKey("model_path")
        val MODEL_NAME = stringPreferencesKey("model_name")
        val MODEL_SIZE = stringPreferencesKey("model_size_bytes")
        val CTX_LENGTH = intPreferencesKey("ctx_length")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val TOP_P = floatPreferencesKey("top_p")
        val TOP_K = intPreferencesKey("top_k")
        val REPEAT_PENALTY = floatPreferencesKey("repeat_penalty")
        val THREADS = intPreferencesKey("threads")
        val USE_MMAP = booleanPreferencesKey("use_mmap")
    }

    val modelRefFlow: Flow<PersistedModelRef> = context.studioDataStore.data.map { prefs ->
        PersistedModelRef(
            path = prefs[Keys.MODEL_PATH].orEmpty(),
            displayName = prefs[Keys.MODEL_NAME].orEmpty(),
            sizeBytes = prefs[Keys.MODEL_SIZE]?.toLongOrNull() ?: 0L
        )
    }

    val inferenceSettingsFlow: Flow<InferenceSettings> = context.studioDataStore.data.map { prefs ->
        InferenceSettings(
            contextLength = prefs[Keys.CTX_LENGTH] ?: 6144,
            temperature = prefs[Keys.TEMPERATURE] ?: 0.70f,
            topP = prefs[Keys.TOP_P] ?: 0.80f,
            topK = prefs[Keys.TOP_K] ?: 20,
            repeatPenalty = prefs[Keys.REPEAT_PENALTY] ?: 1.10f,
            threads = prefs[Keys.THREADS] ?: 4,
            useMmap = prefs[Keys.USE_MMAP] ?: true
        )
    }

    suspend fun saveModelRef(path: String, displayName: String, sizeBytes: Long) {
        context.studioDataStore.edit { prefs ->
            prefs[Keys.MODEL_PATH] = path
            prefs[Keys.MODEL_NAME] = displayName
            prefs[Keys.MODEL_SIZE] = sizeBytes.toString()
        }
    }

    suspend fun clearModelRef() {
        context.studioDataStore.edit { prefs ->
            prefs.remove(Keys.MODEL_PATH)
            prefs.remove(Keys.MODEL_NAME)
            prefs.remove(Keys.MODEL_SIZE)
        }
    }

    suspend fun updateInferenceSettings(settings: InferenceSettings) {
        context.studioDataStore.edit { prefs ->
            prefs[Keys.CTX_LENGTH] = settings.contextLength
            prefs[Keys.TEMPERATURE] = settings.temperature
            prefs[Keys.TOP_P] = settings.topP
            prefs[Keys.TOP_K] = settings.topK
            prefs[Keys.REPEAT_PENALTY] = settings.repeatPenalty
            prefs[Keys.THREADS] = settings.threads
            prefs[Keys.USE_MMAP] = settings.useMmap
        }
    }
}
