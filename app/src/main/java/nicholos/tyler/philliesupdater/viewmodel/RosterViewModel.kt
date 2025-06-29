package nicholos.tyler.philliesupdater.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nicholos.tyler.philliesupdater.BaseballHelper
import nicholos.tyler.philliesupdater.MLBTeam
import nicholos.tyler.philliesupdater.PlayerRoster
import nicholos.tyler.philliesupdater.TeamDetails
import nicholos.tyler.philliesupdater.data.BaseballRepository

@HiltViewModel
class RosterViewModel @Inject constructor(
    private val basebalLRepository: BaseballRepository
) : ViewModel(){
    private val _rosterUiState = MutableStateFlow(RosterUiState())
    val rosterUiState: StateFlow<RosterUiState> = _rosterUiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow<Boolean>(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var selectedTeam: MLBTeam = MLBTeam.PHILLIES

    init {
    }

    fun refresh() {
        _isRefreshing.value = true

        viewModelScope.launch {
            val teamRoster = basebalLRepository.fetchTeamRoster(selectedTeam.teamId)
            if (teamRoster != null) {
                _rosterUiState.update {
                    it.copy(
                        roster = teamRoster.roster,
                        teamName = selectedTeam.displayName
                    )
                }
                }
            }

        _isRefreshing.value = false
        }


    fun setSelectedTeam(teamId: Int) {
        val foundTeam = BaseballHelper.teamFromID(teamId)
        selectedTeam = foundTeam
        refresh()
    }
}

data class RosterUiState(
    //val teamDetails: TeamDetails  = TeamDetails(),
    val roster: List<PlayerRoster> = emptyList(),
    val teamName: String = ""
)