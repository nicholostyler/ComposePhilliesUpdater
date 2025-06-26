package nicholos.tyler.philliesupdater.data

import kotlinx.coroutines.flow.Flow
import nicholos.tyler.philliesupdater.MLBTeam

interface ISettingsRepository {
    val selectedteam: Flow<MLBTeam>
    suspend fun setSelectedTeam(team: MLBTeam)
}