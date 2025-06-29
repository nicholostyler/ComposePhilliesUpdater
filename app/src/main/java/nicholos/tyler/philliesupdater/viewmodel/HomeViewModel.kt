package nicholos.tyler.philliesupdater.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nicholos.tyler.philliesupdater.Date
import nicholos.tyler.philliesupdater.Game
import nicholos.tyler.philliesupdater.LiveGameData
import nicholos.tyler.philliesupdater.MLBTeam
import nicholos.tyler.philliesupdater.StandingsRecord
import nicholos.tyler.philliesupdater.StandingsResponse
import nicholos.tyler.philliesupdater.TeamRecord
import nicholos.tyler.philliesupdater.data.BaseballRepository
import nicholos.tyler.philliesupdater.data.SettingsRepository
import nicholos.tyler.philliesupdater.toLiveGameData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.filterNotNull
import kotlin.collections.orEmpty

data class HomePageUiState(
    val liveGameData: LiveGameData?  = null,
    //val scheduledGames: List<Date> = emptyList(),
    val division: List<TeamRecord> = emptyList(),
    val tenDaySchedule: List<Date> = emptyList(),
    val teamMVPs: List<LeaderSummary> = emptyList()
)

data class LiveGameData(
    val homeTeamName: String,
    val awayTeamName: String,
    val homeTeamScore: Long,
    val awayTeamScore: Long,
    val inning: Int,
    val inningSuffix: String,
    val isTopInning: Boolean,
    val outs: Int ,
    val runnersOnBase: String,
    val isGameOver: Boolean,
    val status: String,
    val game: Game
)

data class LeaderSummary(
    val category: String,
    val playerName: String,
    val playerId: Long
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val baseballRepository: BaseballRepository
) : ViewModel() {

    private val _homePageUiState = MutableStateFlow(HomePageUiState())
    val homePageUiState: MutableStateFlow<HomePageUiState> = _homePageUiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _teamScheduleData = MutableStateFlow<List<Date>>(emptyList())
    val teamScheduleData: StateFlow<List<Date>> = _teamScheduleData.asStateFlow()

    private val _tenDayScheduleData = MutableStateFlow<List<Date>>(emptyList())
    val tenDayScheduleData: StateFlow<List<Date>> = _tenDayScheduleData.asStateFlow()

    private val _selectedteam = MutableStateFlow<MLBTeam>(MLBTeam.PHILLIES)
    val selectedteam: StateFlow<MLBTeam> = _selectedteam.asStateFlow()

    private val _divisionRecords = MutableStateFlow<List<TeamRecord>>(emptyList())
    val divisionRecords: StateFlow<List<TeamRecord>> = _divisionRecords.asStateFlow()

    private val _divisionResponse = MutableStateFlow<StandingsRecord>(StandingsRecord())
    val divisionStandings: StateFlow<StandingsRecord> = _divisionResponse.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.selectedTeam
                .distinctUntilChanged()
                .collect { selectedTeam ->
                    _selectedteam.value = selectedTeam
                    refresh(selectedTeam)
                }
        }


    }

    fun refresh(selectedTeam: MLBTeam) {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // get the selected team

                // get past 5 days of games
                _tenDayScheduleData.value = getGamesWithinFiveDayRange(baseballRepository, 1, selectedTeam.teamId)
                val todayGame = getTodaysGameFromSchedule(_tenDayScheduleData.value)

                val leagueId = if (selectedTeam.isNationalLeague == true) 104 else 103

                // Fetch standings
                val leagueStandings: StandingsResponse? = baseballRepository.fetchStandings(leagueId)
                val standingsResponse = leagueStandings?.records
                    ?.firstOrNull { it.division?.id == selectedTeam.Divisions.divisionId }

                if (standingsResponse != null) {
                    _divisionResponse.value = standingsResponse
                }


                _divisionRecords.value = leagueStandings?.records
                    ?.firstOrNull { it.division?.id == selectedTeam.Divisions.divisionId }
                    ?.teamRecords
                    ?.filterNotNull()
                    .orEmpty()

                val response = leagueStandings?.records
                    ?.firstOrNull { it.division?.id == selectedTeam.Divisions.divisionId }

                if (response != null) {
                    //_divisionResponse.value = response
                }



                    val mappedLiveData = if (todayGame?.gamePk != null) {
                        val details = baseballRepository.fetchGameDetails(todayGame.gamePk)
                        details?.toLiveGameData(todayGame)
                    } else null

                val seasonYear = LocalDate.now().year

                val teamMVPs = getTeamMVPs(baseballRepository, selectedTeam.teamId, seasonYear)

                    // Commit full UI state update
                    _homePageUiState.value = HomePageUiState(
                        tenDaySchedule = _tenDayScheduleData.value,
                        division = _divisionRecords.value,
                        liveGameData = mappedLiveData,
                        teamMVPs = teamMVPs
                    )

            } catch (e: Exception) {
                Log.e("BaseballViewModel", "Error in refreshHomePage", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun getTodaysGameFromSchedule(schedule: List<Date>): Game? {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val todaysDateEntry = schedule.find { it.date == today } ?: return null
        val gamesToday = todaysDateEntry.games?.filterNotNull().orEmpty()

        if (gamesToday.isEmpty()) return null

        val firstGame = gamesToday.first()
        return if (firstGame.status?.detailedState == "Final" && gamesToday.size > 1) {
            gamesToday.getOrNull(1)
        } else {
            firstGame
        }
    }


    suspend fun getGamesWithinFiveDayRange(
        repository: BaseballRepository,
        sportId: Int,
        teamId: Int
    ): List<Date> = withContext(Dispatchers.IO) {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()
        val startDate = today.minusDays(5).format(formatter)
        val endDate = today.plusDays(5).format(formatter)

        val gameRoot = repository.fetchSchedule(
            sportId = sportId,
            startDate = startDate,
            endDate = endDate,
            teamId = teamId
        )

        gameRoot?.dates?.filterNotNull() ?: emptyList()
    }
}

private suspend fun getTeamMVPs(
    baseballRespository: BaseballRepository,
    teamId: Int,
    season: Int
): List<LeaderSummary> {
    val categories = listOf("runs", "homeRuns", "battingAverage") // extend as needed
    val mvps = mutableListOf<LeaderSummary>()

    for (category in categories) {
        val result = baseballRespository.fetchTeamLeaders(teamId, category, season)

        val matchedLeader = result.teamLeaders
            .firstOrNull { it.leaderCategory.equals(category, ignoreCase = true) }

        val topLeader = matchedLeader?.leaders?.firstOrNull()

        if (topLeader != null && topLeader.person.fullName.isNotBlank()) {
            mvps.add(
                LeaderSummary(
                    category = category,
                    playerName = topLeader.person.fullName,
                    playerId = topLeader.person.id
                )
            )
        } else {
            println("No leaders found for category: $category")
        }
    }

    return mvps
}


