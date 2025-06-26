package nicholos.tyler.philliesupdater

import jakarta.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import nicholos.tyler.philliesupdater.data.models.Season
import nicholos.tyler.philliesupdater.data.SettingsRepository
import nicholos.tyler.philliesupdater.data.models.defaultSeason
import nicholos.tyler.philliesupdater.data.models.resolveDefaults
import java.time.Year
import javax.inject.Singleton

@Singleton
class SeasonManager @Inject constructor(
    private val settingsRepository: SettingsRepository,
){
    suspend fun checkAndStoreSeasonIfNeeded(incoming: Season?): Season {
        val currentStored = settingsRepository.currentSeason.firstOrNull()
        val currentYear = currentStored?.seasonId?.take(4)
        val incomingYear = incoming?.seasonId?.take(4)
        val thisYear = Year.now().toString()

        return when {
            // No stored season or year mismatch, and incoming is valid for this year
            (currentStored == null || currentYear != thisYear) && incomingYear == thisYear -> {
                val resolved = incoming.resolveDefaults()
                settingsRepository.setResolvedSeason(resolved)
                resolved
            }

            // Stored season is valid and matches current year
            currentStored != null && currentYear == thisYear -> currentStored.resolveDefaults()

            // Fallback: incoming is not valid or missing, use default
            else -> defaultSeason()
        }
    }




}