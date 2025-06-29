package nicholos.tyler.philliesupdater.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import nicholos.tyler.philliesupdater.DivisionRecord
import nicholos.tyler.philliesupdater.StandingsRecord
import nicholos.tyler.philliesupdater.TeamRecord
import nicholos.tyler.philliesupdater.data.BaseballRepository

@HiltViewModel
class DivisionViewModel @Inject constructor(
    private val baseballRepository: BaseballRepository
) : ViewModel() {
    private val _uiState: MutableStateFlow<DivisionUiState> = MutableStateFlow(DivisionUiState())
    val uiState: MutableStateFlow<DivisionUiState> = _uiState


    fun setup(divisionRecord: StandingsRecord) {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        var teamRecords = emptyList<TeamRecord>()
        viewModelScope.launch {
            divisionRecord.teamRecords?.let {
                teamRecords = it
            }

            _uiState.value = _uiState.value.copy(divisionRecords = teamRecords, isRefreshing = false, teamName = divisionRecord.division?.name.toString())
        }
    }
}

data class DivisionUiState(
    val divisionRecords: List<TeamRecord> = emptyList(),
    val isRefreshing: Boolean = false,
    val teamName: String = "",
)
