package nicholos.tyler.philliesupdater.data.models

import kotlinx.serialization.Serializable

@Serializable
data class TeamLeadersResponseRaw(
    val copyright: String?,
    val teamLeaders: List<TeamLeaderRaw>?
)
@Serializable
data class TeamLeaderRaw(
    val leaderCategory: String?,
    val season: String?,
    val gameType: GameTypeRaw?,
    val leaders: List<LeaderRaw>?,
    val statGroup: String?,
    val team: Team2Raw?,
    val totalSplits: Long?
)
@Serializable
data class GameTypeRaw(val id: String?, val description: String?)
@Serializable
data class LeaderRaw(
    val rank: Long?,
    val value: String?,
    val team: TeamRaw?,
    val league: LeagueRaw?,
    val person: PersonRaw?,
    val sport: SportRaw?,
    val season: String?
)
@Serializable
data class TeamRaw(val id: Long?, val name: String?, val link: String?)
@Serializable
data class LeagueRaw(val id: Long?, val name: String?, val link: String?)
@Serializable
data class PersonRaw(val id: Long?, val fullName: String?, val link: String?, val firstName: String?, val lastName: String?)
@Serializable
data class SportRaw(val id: Long?, val link: String?, val abbreviation: String?)
@Serializable
data class Team2Raw(val id: Long?, val name: String?, val link: String?)


data class TeamLeadersResponse(
    val copyright: String,
    val teamLeaders: List<TeamLeader>
)

data class TeamLeader(
    val leaderCategory: String,
    val season: String,
    val gameType: GameType,
    val leaders: List<Leader>,
    val statGroup: String,
    val team: Team2,
    val totalSplits: Long
)

data class GameType(val id: String, val description: String)
data class Leader(
    val rank: Long,
    val value: String,
    val team: Team,
    val league: League,
    val person: Person,
    val sport: Sport,
    val season: String
)

data class Team(val id: Long, val name: String, val link: String)
data class League(val id: Long, val name: String, val link: String)
data class Person(val id: Long, val fullName: String, val link: String, val firstName: String, val lastName: String)
data class Sport(val id: Long, val link: String, val abbreviation: String)
data class Team2(val id: Long, val name: String, val link: String)


fun TeamLeadersResponseRaw.toDomain() = TeamLeadersResponse(
    copyright = copyright.orEmpty(),
    teamLeaders = teamLeaders?.map { it.toDomain() } ?: emptyList()
)

fun TeamLeaderRaw.toDomain() = TeamLeader(
    leaderCategory = leaderCategory.orEmpty(),
    season = season.orEmpty(),
    gameType = gameType?.toDomain() ?: GameType("", ""),
    leaders = leaders?.map { it.toDomain() } ?: emptyList(),
    statGroup = statGroup.orEmpty(),
    team = team?.toDomain() ?: Team2(0, "", ""),
    totalSplits = totalSplits ?: 0L
)

fun GameTypeRaw.toDomain() = GameType(
    id = id.orEmpty(),
    description = description.orEmpty()
)

fun LeaderRaw.toDomain() = Leader(
    rank = rank ?: 0L,
    value = value.orEmpty(),
    team = team?.toDomain() ?: Team(0, "", ""),
    league = league?.toDomain() ?: League(0, "", ""),
    person = person?.toDomain() ?: Person(0, "", "", "", ""),
    sport = sport?.toDomain() ?: Sport(0, "", ""),
    season = season.orEmpty()
)

fun TeamRaw.toDomain() = Team(id ?: 0L, name.orEmpty(), link.orEmpty())
fun LeagueRaw.toDomain() = League(id ?: 0L, name.orEmpty(), link.orEmpty())
fun PersonRaw.toDomain() = Person(id ?: 0L, fullName.orEmpty(), link.orEmpty(), firstName.orEmpty(), lastName.orEmpty())
fun SportRaw.toDomain() = Sport(id ?: 0L, link.orEmpty(), abbreviation.orEmpty())
fun Team2Raw.toDomain() = Team2(id ?: 0L, name.orEmpty(), link.orEmpty())

