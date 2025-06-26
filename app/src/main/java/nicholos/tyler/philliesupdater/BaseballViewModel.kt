package nicholos.tyler.philliesupdater

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nicholos.tyler.philliesupdater.data.BaseballRepository
import nicholos.tyler.philliesupdater.data.SettingsRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.coroutines.cancellation.CancellationException

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

data class TeamPageUiState(
    val teamDetails: TeamDetails  = TeamDetails(),
    val roster: List<PlayerRoster> = emptyList(),
    val divisionRank: Int = 0,
    val divisionWins: Int = 0,
    val divisionLosses: Int = 0,
    val divisionWinPercentage: Double = 0.0
)

data class SchedulePageUiState(
    val scheduledGames: List<Game> = emptyList(),
    //val gameCardList: List<ScheduleGameData> = emptyList()
)

data class DetailPageUiState(
    val plays: List<Play> = emptyList(),
    val gameData: GameData? = null
)

@HiltViewModel
class BaseballViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val baseballRepository: BaseballRepository
) : ViewModel() {

    private val _baseballScheduleData = MutableStateFlow<GameRoot?>(null)
    val baseballScheduleData: MutableStateFlow<GameRoot?> = _baseballScheduleData

    private val _baseballGameData = MutableStateFlow<GameDetailResponse?>(null)
    val baseballGameData: MutableStateFlow<GameDetailResponse?> = _baseballGameData

    private val _selectedGame = MutableStateFlow<Game?>(null)
    val selectedGame: StateFlow<Game?> = _selectedGame

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: MutableStateFlow<String?> = _errorMessage

    private val _homePageRefreshing = MutableStateFlow(false)
    val homePageRefreshing: StateFlow<Boolean> = _homePageRefreshing.asStateFlow()

    private val _schedulePageRefreshing = MutableStateFlow(false)
    val schedulePageRefreshing: StateFlow<Boolean> = _schedulePageRefreshing.asStateFlow()

    private val _teamPageRereshing = MutableStateFlow(false)
    val teamPageRereshing: StateFlow<Boolean> = _teamPageRereshing.asStateFlow()

    private val _detailPageRefreshing = MutableStateFlow(false)
    val detailPageRefreshing: StateFlow<Boolean> = _detailPageRefreshing.asStateFlow()

    private val _scheduleUiState = MutableStateFlow(SchedulePageUiState())
    val scheduleUiState: MutableStateFlow<SchedulePageUiState> = _scheduleUiState

    private val _detailPageUiState = MutableStateFlow(DetailPageUiState())
    val detailPageUiState: MutableStateFlow<DetailPageUiState> = _detailPageUiState

    private val _groupedPlaysByInning = MutableStateFlow<Map<Pair<Int, Boolean>, List<Play>>>(emptyMap())
    val groupedPlaysByInning: StateFlow<Map<Pair<Int, Boolean>, List<Play>>> = _groupedPlaysByInning


    private val _teamPageUiState = MutableStateFlow(TeamPageUiState())
    val teamPageUiState: MutableStateFlow<TeamPageUiState> = _teamPageUiState

    private val _division = MutableStateFlow<List<StandingsRecord>>(emptyList())
    val division: MutableStateFlow<List<StandingsRecord>> = _division

    private val _teamRoster = MutableStateFlow<List<PlayerRoster>>(emptyList())
    val teamRoster: MutableStateFlow<List<PlayerRoster>> = _teamRoster

    val selectedTeam: MutableStateFlow<MLBTeam?> = MutableStateFlow(MLBTeam.PHILLIES)

    init {
        Log.d("BaseballViewModel", "ViewModel initialized")
        selectedTeam.value = MLBTeam.PHILLIES

    }

    fun setSelectedGame(game: Game?) {
        if (game != null) {
            _selectedGame.value = game
            //_selectedGame.value!!.gamePk?.let { fetchGameDetails(it.toLong()) }
        }
    }

    fun disposeDetailPage() {
        _detailPageUiState.value = DetailPageUiState()
    }

    fun refreshDetailPage(gamePk: Long, onComplete: (() -> Unit)? = null) {
        _detailPageRefreshing.value = true
        viewModelScope.launch {
            if (gamePk != null) {
                val gameDetailResponse = baseballRepository.fetchGameDetails(gamePk)
                if (gameDetailResponse != null) {
                    val allPlays = gameDetailResponse.liveData?.plays?.allPlays
                    if (allPlays != null) {
                        _detailPageUiState.update {
                            DetailPageUiState(
                                plays = allPlays,
                                gameData = gameDetailResponse.gameData
                            )
                        }

                        val grouped = allPlays
                            .mapNotNull { play ->
                                val inning = play.about?.inning
                                val isTop = play.about?.isTopInning
                                if (inning != null && isTop != null) {
                                    (inning to isTop) to play
                                } else null
                            }
                            .groupBy({ it.first }, { it.second })


                        _groupedPlaysByInning.value = grouped

                        _detailPageRefreshing.value = false
                        onComplete?.invoke()

                    }
                }
            }
        }

    }


    fun refreshTeamPage() {
        _teamPageRereshing.value = true

        viewModelScope.launch {
            try {
                selectedTeam.value?.teamId?.let { teamId ->
                    val teamRoster = baseballRepository.fetchTeamRoster(teamId)
                    val teamResponse = baseballRepository.fetchTeam(teamId)
                    val roster = teamRoster?.roster ?: emptyList()

                    val team = teamResponse?.teams?.first() ?: TeamDetails()

                    _teamPageUiState.update {
                        it.copy(
                            roster = roster,
                            teamDetails = team
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("BaseballViewModel", "Error in refreshHomePage", e)
            }
        }

        _teamPageRereshing.value = false
    }

    fun getTodaysGame(): Game? {
        val scheduleRoot = _baseballScheduleData.value ?: return null;

        if (scheduleRoot.dates.isNullOrEmpty()) {
            Log.d("BaseballViewModel", "No dates available in schedule data.")
            return null;
        }

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val todayFormattedString = today.format(formatter)

        val todaysScheduleDate: Date? = scheduleRoot.dates.find { scheduleDate ->
            scheduleDate.date == todayFormattedString
        }

        if (todaysScheduleDate == null) {
            Log.d(
                "BaseballViewModel",
                "No schedule entry found for today's date: $todayFormattedString"
            )
            return null
        }

        if (todaysScheduleDate.games.isNullOrEmpty()) {
            Log.d(
                "BaseballViewModel",
                "No games listed for today's date: $todayFormattedString, even though date entry exists."
            )
            return null
        }

        var firstGameToday: Game? = todaysScheduleDate.games.firstOrNull()


        if (firstGameToday?.status?.detailedState == "Final" && todaysScheduleDate.games.size > 1) {
            firstGameToday = todaysScheduleDate.games[1]
        }

        if (firstGameToday == null) {
            Log.d(
                "BaseballViewModel",
                "Games list for today is present but empty, or first game is null."
            )
        } else {
            Log.d(
                "BaseballViewModel",
                "Found first game for today. GamePK: ${firstGameToday.gamePk}"
            )
        }

        return firstGameToday
    }

//    fun getSchedulePage() {
//        _schedulePageRefreshing.value = true
//        if (_baseballScheduleData.value != null) {
//            if (_baseballScheduleData.value?.dates?.isEmpty() == false) {
//                val games = getAllGames(_baseballScheduleData.value)
//                selectedTeam.value?.name?.takeIf { it.isNotBlank() }?.let { teamName ->
//                    val scheduleGameDataList = games.toScheduleGameDataList(teamName)
//                    if (games.isNotEmpty()) {
//                        _scheduleUiState.update {
//                            it.copy(scheduledGames = games, gameCardList = scheduleGameDataList)
//                        }
//                    }
//                }
//
//            }
//        }
//        _schedulePageRefreshing.value = false
//    }



    fun getDatesWithinTenDayRange(): List<Date> {
        val allDates = _baseballScheduleData.value?.dates ?: return emptyList()

        val today = LocalDate.now()
        val startDate = today.minusDays(5)
        val endDate = today.plusDays(5)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        return allDates.filterNotNull().filter { dateEntry ->
            val parsedDate = try {
                LocalDate.parse(dateEntry.date, formatter)
            } catch (e: Exception) {
                null
            }

            parsedDate != null && !parsedDate.isBefore(startDate) && !parsedDate.isAfter(endDate)
        }
    }




    fun getAllGames(scheduleData: GameRoot?): List<Game> {
        return scheduleData
            ?.dates
            ?.filterNotNull()
            ?.flatMap { it.games.orEmpty().filterNotNull() }
            ?: emptyList()
    }

    fun mapTeamDetails(raw: TeamDetails?): TeamDetails {
        return TeamDetails(
            springLeague = raw?.springLeague ?: League(-1, "Unknown", "", ""),
            allStarStatus = raw?.allStarStatus ?: "N",
            id = raw?.id ?: -1,
            name = raw?.name ?: "Unnamed Team",
            link = raw?.link ?: "",
            season = raw?.season ?: 0,
            venue = raw?.venue ?: Venue(-1, "Unknown", ""),
            springVenue = raw?.springVenue ?: SpringVenue(-1, ""),
            teamCode = raw?.teamCode ?: "XXX",
            fileCode = raw?.fileCode ?: "unknown",
            abbreviation = raw?.abbreviation ?: "UNK",
            teamName = raw?.teamName ?: "Unknown",
            locationName = raw?.locationName ?: "Unknown",
            firstYearOfPlay = raw?.firstYearOfPlay ?: "N/A",
            league = raw?.league ?: League(-1, "Unknown", "", ""),
            division = raw?.division ?: Division(-1, "Unknown", ""),
            sport = raw?.sport ?: Sport(-1, "Unknown", ""),
            shortName = raw?.shortName ?: "N/A",
            record = raw?.record ?: Record(0, "0"),
            franchiseName = raw?.franchiseName ?: "N/A",
            clubName = raw?.clubName ?: "N/A",
            active = raw?.active ?: false
        )
    }

}