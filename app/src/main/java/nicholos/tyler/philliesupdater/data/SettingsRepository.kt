package nicholos.tyler.philliesupdater.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nicholos.tyler.philliesupdater.MLBTeam
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import jakarta.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.json.Json
import nicholos.tyler.philliesupdater.data.models.Season

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val SELECTED_TEAM_ID_KEY = intPreferencesKey("selected_team_id")
        private val RESOLVED_SEASON_KEY = stringPreferencesKey("resolved_season_json")
    }

    val selectedTeam: Flow<MLBTeam> = dataStore.data
        .catch { e ->
            Log.e("SettingsRepository", "Error reading selectedTeam", e)
            emit(emptyPreferences())
        }
        .map { prefs ->
            val id = prefs[SELECTED_TEAM_ID_KEY] ?: MLBTeam.PHILLIES.teamId
            MLBTeam.fromId(id) ?: MLBTeam.PHILLIES
        }
        .distinctUntilChanged()

    suspend fun setSelectedTeam(team: MLBTeam) {
        dataStore.edit { prefs ->
            prefs[SELECTED_TEAM_ID_KEY] = team.teamId
        }
    }

    suspend fun setResolvedSeason(season: Season) {
        val serialized = json.encodeToString(season)
        dataStore.edit { prefs ->
            prefs[RESOLVED_SEASON_KEY] = serialized
        }
    }

    val currentSeason: Flow<Season?> = dataStore.data
        .map { prefs ->
            prefs[RESOLVED_SEASON_KEY]?.let {
                try {
                    json.decodeFromString<Season>(it)
                } catch (e: Exception) {
                    null
                }
            }
        }
}
