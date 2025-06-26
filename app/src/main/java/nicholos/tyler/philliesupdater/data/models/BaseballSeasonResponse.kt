package nicholos.tyler.philliesupdater.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SeasonResponse(
    val copyright: String?,
    val seasons: List<Season?>,
)

@Serializable
data class Season(
    val seasonId: String? = null,
    val hasWildcard: Boolean? = null,
    val preSeasonStartDate: String? = null,
    val preSeasonEndDate: String? = null,
    val seasonStartDate: String? = null,
    val springStartDate: String? = null,
    val springEndDate: String? = null,
    val regularSeasonStartDate: String? = null,
    val lastDate1stHalf: String? = null,
    val allStarDate: String? = null,
    val firstDate2ndHalf: String? = null,
    val regularSeasonEndDate: String? = null,
    val postSeasonStartDate: String? = null,
    val postSeasonEndDate: String? = null,
    val seasonEndDate: String? = null,
    val offseasonStartDate: String? = null,
    val offSeasonEndDate: String? = null,
    val seasonLevelGamedayType: String? = null,
    val gameLevelGamedayType: String? = null,
    val qualifierPlateAppearances: Double? = null,
    val qualifierOutsPitched: Double? = null
)

fun defaultSeason(): Season = Season().resolveDefaults()

fun SeasonResponse.resolveDefaults(): List<Season> {
    return seasons.map { it?.resolveDefaults() ?: defaultSeason() }
}


fun SeasonResponse.resolvedSeasons(): List<Season> =
    seasons.map { it?.resolveDefaults() ?: defaultSeason() }



fun Season.resolveDefaults(): Season = Season(
    seasonId = seasonId ?: "default_season_id",
    hasWildcard = hasWildcard ?: false,
    preSeasonStartDate = preSeasonStartDate ?: "2025-01-01",
    preSeasonEndDate = preSeasonEndDate ?: "2025-01-31",
    seasonStartDate = seasonStartDate ?: "2025-02-01",
    springStartDate = springStartDate ?: "2025-03-01",
    springEndDate = springEndDate ?: "2025-03-31",
    regularSeasonStartDate = regularSeasonStartDate ?: "2025-04-01",
    lastDate1stHalf = lastDate1stHalf ?: "2025-06-30",
    allStarDate = allStarDate ?: "2025-07-10",
    firstDate2ndHalf = firstDate2ndHalf ?: "2025-07-15",
    regularSeasonEndDate = regularSeasonEndDate ?: "2025-09-30",
    postSeasonStartDate = postSeasonStartDate ?: "2025-10-01",
    postSeasonEndDate = postSeasonEndDate ?: "2025-10-31",
    seasonEndDate = seasonEndDate ?: "2025-11-01",
    offseasonStartDate = offseasonStartDate ?: "2025-11-02",
    offSeasonEndDate = offSeasonEndDate ?: "2025-12-31",
    seasonLevelGamedayType = seasonLevelGamedayType ?: "Regular",
    gameLevelGamedayType = gameLevelGamedayType ?: "Standard",
    qualifierPlateAppearances = qualifierPlateAppearances ?: 502.0,
    qualifierOutsPitched = qualifierOutsPitched ?: 486.0
)

