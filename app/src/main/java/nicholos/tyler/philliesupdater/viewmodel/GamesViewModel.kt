package nicholos.tyler.philliesupdater.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nicholos.tyler.philliesupdater.Game
import nicholos.tyler.philliesupdater.LiveGameData
import nicholos.tyler.philliesupdater.data.BaseballRepository
import nicholos.tyler.philliesupdater.toLiveGameData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val baseballRepository: BaseballRepository
) : ViewModel() {
    private val _gamesUiState = MutableStateFlow(GamesUiState())
    val gamesUiState: StateFlow<GamesUiState> = _gamesUiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _gamesUiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            Log.d("GamesViewModel", "launch started")
            try {
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val todayGamesScheduleRoot = baseballRepository.fetchSchedule(1, today, today, null)

                val gamesFromApi: List<Game?> = todayGamesScheduleRoot?.dates?.firstOrNull()?.games.orEmpty()

                val todaysGames: List<Game> = gamesFromApi.filterNotNull()

                if (todaysGames.isNotEmpty()) {
                    val mappedGames = todaysGames.mapNotNull { game ->
                        game.gamePk?.let { gamePk ->
                            val gameDetails = baseballRepository.fetchGameDetails(gamePk)
                            gameDetails?.toLiveGameData(game)
                        }
                    }
                    _gamesUiState.update {
                        it.copy(
                            todaysSchedule = todaysGames,
                            liveGamesData = mappedGames,
                            isLoading = false
                        )
                    }
                } else {
                    _gamesUiState.update {
                        it.copy(
                            todaysSchedule = emptyList(),
                            liveGamesData = emptyList(),
                            isLoading = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _gamesUiState.update {
                    it.copy(
                        isLoading = false,
                        exception = "Failed to load game data: ${e.localizedMessage}"
                    )
                }
                Log.e("RefreshGames", "Error refreshing game data", e)
            }
        }
    }



}

data class GamesUiState(
    val todaysSchedule: List<Game> = emptyList(),
    val liveGamesData: List<LiveGameData> = emptyList(),
    val isLoading: Boolean = false,
    val exception: String? = null
)