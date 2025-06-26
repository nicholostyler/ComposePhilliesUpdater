package nicholos.tyler.philliesupdater.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import nicholos.tyler.philliesupdater.MLBTeam
import nicholos.tyler.philliesupdater.data.models.Season
import nicholos.tyler.philliesupdater.data.SettingsRepository
import nicholos.tyler.philliesupdater.data.models.resolveDefaults
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val selectedTeam: StateFlow<MLBTeam> = settingsRepository.selectedTeam
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MLBTeam.PHILLIES)

    val resolvedSeason = settingsRepository.currentSeason
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onTeamSelected(team: MLBTeam) {
        viewModelScope.launch {
            settingsRepository.setSelectedTeam(team)
        }
    }

    fun storeResolvedSeasonOnceIfMissing(incoming: Season?) {
        viewModelScope.launch {
            if (incoming == null) return@launch
            val current = settingsRepository.currentSeason.firstOrNull()
            if (current == null) {
                settingsRepository.setResolvedSeason(incoming.resolveDefaults())
            }
        }
    }

    fun checkAndStoreSeasonIfNeeded(incoming: Season?) {
        viewModelScope.launch {
            if (incoming == null) return@launch

            val currentStored = settingsRepository.currentSeason.firstOrNull()
            val currentYear = currentStored?.seasonId?.take(4)
            val incomingYear = incoming.seasonId?.take(4)
            val thisYear = java.time.Year.now().toString()

            val shouldStore = currentStored == null ||
                    currentYear != thisYear ||
                    incomingYear != thisYear

            if (shouldStore && !incomingYear.isNullOrBlank() && incomingYear == thisYear) {
                settingsRepository.setResolvedSeason(incoming.resolveDefaults())
            }
        }
    }

}