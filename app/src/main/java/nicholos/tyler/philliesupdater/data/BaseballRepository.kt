package nicholos.tyler.philliesupdater.data

import nicholos.tyler.philliesupdater.RosterResponse
import nicholos.tyler.philliesupdater.StandingsResponse
import nicholos.tyler.philliesupdater.TeamResponse

import nicholos.tyler.philliesupdater.BaseballService
import nicholos.tyler.philliesupdater.GameDetailResponse
import nicholos.tyler.philliesupdater.GameRoot
import nicholos.tyler.philliesupdater.data.models.SeasonResponse
import nicholos.tyler.philliesupdater.data.models.TeamLeadersResponse
import nicholos.tyler.philliesupdater.data.models.TeamLeadersResponseRaw
import nicholos.tyler.philliesupdater.data.models.toDomain
import retrofit2.Response
import javax.inject.Inject

class BaseballRepository @Inject constructor(
    private val api: BaseballService
) {
    suspend fun fetchSchedule(
        sportId: Int,
        startDate: String,
        endDate: String,
        teamId: Int
    ): GameRoot? = tryApi {
        api.getMlbSchedule(sportId, startDate, endDate, teamId)
    }

    suspend fun fetchGameDetails(gamePk: Long): GameDetailResponse? = tryApi {
        api.getGameDetails(gamePk)
    }

    suspend fun fetchStandings(leagueId: Int): StandingsResponse? = tryApi {
        api.getStandings(leagueId)
    }

    suspend fun fetchTeamRoster(teamId: Int): RosterResponse? = tryApi {
        api.getTeamRoster(teamId)
    }

    suspend fun fetchTeam(teamId: Int): TeamResponse? = tryApi {
        api.getTeam(teamId)
    }

    suspend fun fetchSeason(sportId: Int, season: Int): SeasonResponse? = tryApi {
        api.getSeason(sportId, season)
    }

    suspend fun fetchTeamLeaders(teamId: Int, category: String, season: Int): TeamLeadersResponse {
        return try {
            val response = api.getTeamLeaders(teamId, category, season)
            response.body()?.toDomain() ?: TeamLeadersResponse("", emptyList())
        } catch (e: Exception) {
            println("API exception: ${e.message}")
            TeamLeadersResponse("", emptyList())
        }
    }



    private inline fun <T> tryApi(apiCall: () -> Response<T>): T? {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()
            } else {
                println("API error: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            println("API exception: ${e.message}")
            null
        }
    }
}