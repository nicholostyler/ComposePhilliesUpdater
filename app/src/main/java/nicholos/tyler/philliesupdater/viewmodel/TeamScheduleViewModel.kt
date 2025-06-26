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
import nicholos.tyler.philliesupdater.BaseballHelper
import nicholos.tyler.philliesupdater.Game
import nicholos.tyler.philliesupdater.GameRoot
import nicholos.tyler.philliesupdater.MLBTeam
import nicholos.tyler.philliesupdater.SeasonManager
import nicholos.tyler.philliesupdater.data.BaseballRepository
import nicholos.tyler.philliesupdater.data.models.Season
import nicholos.tyler.philliesupdater.data.models.resolvedSeasons
import nicholos.tyler.philliesupdater.toScheduleGameDataList
import kotlin.collections.filterNotNull
import kotlin.collections.orEmpty

@HiltViewModel
class TeamScheduleViewModel @Inject constructor(
    private val baseballRepository: BaseballRepository,
    private val seasonManager: SeasonManager) : ViewModel() {

    private val _teamSchedule = MutableStateFlow<List<ScheduleGameData>>(emptyList())
    val teamSchedule: StateFlow<List<ScheduleGameData>> = _teamSchedule.asStateFlow()

    private val _selectedTeam = MutableStateFlow<MLBTeam>(MLBTeam.PHILLIES)
    val selectedTeam: StateFlow<MLBTeam> = _selectedTeam.asStateFlow()

    private val _refreshing = MutableStateFlow<Boolean>(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()


    init {
        viewModelScope.launch {
            val thisYear = java.time.Year.now().value

            val seasonResponse = baseballRepository.fetchSeason(1, thisYear)
            Log.d("seasonResponse = ", seasonResponse.toString())
            if (seasonResponse != null && seasonResponse.seasons != null) {
                val resolvedSeasons = seasonResponse.resolvedSeasons()
                val season = resolvedSeasons.first()
                val activeSeason = seasonManager.checkAndStoreSeasonIfNeeded(season)
                refresh(activeSeason)
            }
        }
    }

    fun setSelectedTeam(teamId: Int) {
        val foundTeam = BaseballHelper.teamFromID(teamId)
        _selectedTeam.value = foundTeam
    }

    suspend fun refresh(currentSeason: Season) {
        _refreshing.value = true
        val seasonYear = currentSeason.seasonId ?: java.time.LocalDate.now().year.toString()
        val seasonStart =
            currentSeason.seasonStartDate ?: java.time.LocalDate.of(seasonYear.toInt(), 3, 16)
                .toString()
        val seasonEnd =
            currentSeason.seasonEndDate ?: java.time.LocalDate.of(seasonYear.toInt(), 10, 16)
                .toString()
        val scheduleResponse =
            baseballRepository.fetchSchedule(1, seasonStart, seasonEnd, _selectedTeam.value.teamId)

        if (scheduleResponse != null) {
            _teamSchedule.update { mapScheduleDatesToGames(scheduleResponse) }
        }
    }

    fun mapScheduleDatesToGames(teamSchedule: GameRoot?): List<ScheduleGameData> {
        val games = getAllGames(teamSchedule)
        _selectedTeam.value.name.takeIf { it.isNotBlank() }?.let { teamName ->
            val scheduleGameDataList = games.toScheduleGameDataList(teamName)
            return scheduleGameDataList
        }
        return emptyList()
    }


    fun getAllGames(scheduleData: GameRoot?): List<Game> {
        return scheduleData
            ?.dates
            ?.filterNotNull()
            ?.flatMap { it.games.orEmpty().filterNotNull() }
            ?: emptyList()
    }

    data class ScheduleGameData(
    val date: String,
    val time: String,
    val awayTeamName: String,
    val opponentLogoUrl: String?,
    val homeTeamName: String,
    val homeTeamScore: Long,
    val awayTeamScore: Long,
    val status: String,
    val venue: String ,
    val prefix: String,
    val game: Game
)
}