package nicholos.tyler.philliesupdater.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nicholos.tyler.philliesupdater.BaseballHelper
import nicholos.tyler.philliesupdater.DateHelper
import nicholos.tyler.philliesupdater.Game
import nicholos.tyler.philliesupdater.Screen
import nicholos.tyler.philliesupdater.ui.theme.PhilliesUpdaterTheme
import nicholos.tyler.philliesupdater.viewmodel.HomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import nicholos.tyler.philliesupdater.MLBTeam
import nicholos.tyler.philliesupdater.TeamRecord
import nicholos.tyler.philliesupdater.components.GameScheduleCard
import nicholos.tyler.philliesupdater.viewmodel.LeaderSummary

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier, navController: NavController, baseballVM: HomeViewModel = hiltViewModel()) {

    val homePageUiState by baseballVM.homePageUiState.collectAsState()
    val isRefreshing by baseballVM.isRefreshing.collectAsState()
    val selectedTeam by baseballVM.selectedteam.collectAsState()
    val divisionRecords by baseballVM.divisionRecords.collectAsState()
    val teamMVPs = homePageUiState.teamMVPs

    val coroutineScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                baseballVM.refresh(selectedTeam)
            }
        },

        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {

        if (homePageUiState != null) {
            val liveGameData = homePageUiState.liveGameData
            var flatGames: List<Game> = emptyList()
            if (homePageUiState.tenDaySchedule.isNotEmpty()) {
                flatGames = homePageUiState.tenDaySchedule.flatMap { date ->
                    date.games ?: emptyList()
                }.filterNotNull()
            }


            Column(modifier = modifier.verticalScroll(rememberScrollState())) {
                LiveGameScoreCard(
                    modifier = Modifier,
                    isTopInning = liveGameData?.isTopInning ?: false,
                    inning = liveGameData?.inning ?: 0,
                    inningSuffix = liveGameData?.inningSuffix,
                    awayTeamName = liveGameData?.awayTeamName ?: "",
                    homeTeamName = liveGameData?.homeTeamName ?: "",
                    awayTeamScore = liveGameData?.awayTeamScore ?: 0,
                    homeTeamScore = liveGameData?.homeTeamScore ?: 0,
                    outs = liveGameData?.outs ?: 0,
                    leftOnBase = liveGameData?.runnersOnBase ?: "0",
                    isGameOver = liveGameData?.isGameOver ?: false,
                    status = liveGameData?.status ?: "No Game Today",
                    onGameClick = {
                        if (liveGameData?.game?.gamePk != null) {
                            navController.navigate(Screen.GameDetail.createRoute(gamePk = liveGameData.game.gamePk))
                        }
                    },
                    game = liveGameData?.game ?: Game()
                )

                TenDayStretch(
                    flatGames,
                    selectedTeam,
                    onGameClick = { game ->
                        if (game.gamePk != null) {
                            navController.navigate(Screen.GameDetail.createRoute(gamePk = game.gamePk))
                        }
                    },
                    onViewScheduleClick = {
                        navController.navigate(Screen.TeamSchedule.createRoute(teamId = selectedTeam.teamId.toLong()))
                    }
                )

                TeamDivisionStanding(teamRecord = divisionRecords, selectedTeam = selectedTeam)
                TeamMVPRowList(teamMVPs)


            }
            }

        }
    }

@Composable
fun TeamDivisionStanding(teamRecord: List<TeamRecord>, selectedTeam: MLBTeam) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Division Standing",
            style = MaterialTheme.typography.titleMedium
        )

        TextButton(
            onClick = {  }
        ) {
            Text("View Division")
        }
    }

    if (teamRecord.isNotEmpty()) {
        val divisionRecords = teamRecord.sortedBy { record ->
            record.divisionRank
        }
        for (record in divisionRecords) {
            if (record.team?.name != null) {
                if (record.team.name == selectedTeam.displayName) {
                    TeamStandingsSnippet(
                        modifier = Modifier.fillMaxWidth(),
                        standingInfo = TeamStandingInfo(
                            BaseballHelper.Companion.abbreviateTeamName(record.team.name.toString()),
                            teamFullName = record.team.name.toString(),
                            divisionRank = record.divisionRank?.toInt(),
                            gamesBehind = record.divisionGamesBack,
                            wins = record.wins,
                            losses = record.losses,
                            divisionName = selectedTeam.Divisions.displayName
                        )
                    )
                }
            }

        }

    }
}

@Composable
fun TenDayStretchList(
    games: List<Game>,
    selectedTeam: MLBTeam,
    onGameClick: (Game) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(games) { game ->
            if (game != null) {
                var status = ""
                if (game.status?.detailedState == "Final") {
                    status = "Final"
                } else {
                    status = "Scheduled"
                }
                val homeTeamName = game.teams?.home?.team?.name ?: "N/A"
                val awayTeamName = game.teams?.away?.team?.name ?: "N/A"
                val homeTeamScore = game.teams?.home?.score ?: 0
                val awayTeamScore = game.teams?.away?.score ?: 0
                val yourTeamName = selectedTeam.displayName
                val prefix = if (homeTeamName == yourTeamName) "vs" else "@"

                GameScheduleCard(
                    modifier = Modifier
                        .width(300.dp)
                        .height(100.dp)
                        .clickable { onGameClick(game) },
                    date = DateHelper.Companion.formatIsoDateToDisplayString(game.gameDate!!)
                        .toString(), //
                    time = DateHelper.Companion.formatIsoDateToTimeString(game.gameDate.toString()),
                    awayTeamName = awayTeamName,
                    opponentLogoUrl = null,
                    homeTeamName = homeTeamName,
                    homeTeamScore = homeTeamScore,
                    awayTeamScore = awayTeamScore,
                    status = status,
                    venue = game.venue?.name ?: "N/A",
                    onClick = { onGameClick(game) },
                    yourTeamName = selectedTeam.displayName
                )
            }
        }
    }
}

@Composable
fun TenDayStretch(games: List<Game>, selectedTeam: MLBTeam, onGameClick: (Game) -> Unit, onViewScheduleClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "10-Day Stretch",
            style = MaterialTheme.typography.titleMedium
        )

        TextButton(
            onClick = onViewScheduleClick
        ) {
            Text("View Schedule")
        }
    }

    TenDayStretchList(games = games, selectedTeam = selectedTeam, onGameClick = onGameClick)
}




@Preview(showBackground = true)
@Composable
fun HomepagePreview() {

}

data class TeamStandingInfo(
    val teamAbbreviation: String,
    val teamFullName: String,
    val divisionRank: Int?,
    val gamesBehind: String?,
    val wins: Int?,
    val losses: Int?,
    val divisionName: String
)

// Example of how you might get this for the homepage (simplified)
data class DivisionStandings(
    val divisionName: String,
    val teams: List<TeamStandingInfo>
)

enum class GameStatus {
    SCHEDULED,
    IN_PROGRESS,
    FINAL
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiveGameScoreCard(
    modifier: Modifier = Modifier,
    isTopInning: Boolean,
    inning: Int,
    inningSuffix: String?,
    awayTeamName: String,
    homeTeamName: String,
    awayTeamScore: Long,
    homeTeamScore: Long,
    outs: Int,
    leftOnBase: String,
    isGameOver: Boolean,
    status: String?,
    game: Game,
    onGameClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable {
                onGameClick()
            },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,

        ),

    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Inning / Status Banner ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isGameOver && status == "In Progress") {
                    Icon(
                        imageVector = if (isTopInning) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = when {
                        isGameOver -> "Final"
                        status == "In Progress" -> "${if (isTopInning) "Top" else "Bot"} $inning${inningSuffix.orEmpty()}"
                        else -> status ?: "No Game"
                    },
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Score Display ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamScoreColumn(
                    teamName = awayTeamName,
                    score = awayTeamScore,
                    isBold = isTopInning && !isGameOver
                )

                Text(
                    text = "vs",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TeamScoreColumn(
                    teamName = homeTeamName,
                    score = homeTeamScore,
                    isBold = !isTopInning && !isGameOver
                )
            }

            // ── Game Detail (In-Progress Only) ──
            if (status == "In Progress") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$outs Out${if (outs != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (leftOnBase.isNotBlank()) {
                        Text(" • ", modifier = Modifier.padding(horizontal = 6.dp))
                        Text(
                            text = leftOnBase,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun InningStatusText(
    status: String?, // e.g., "In Progress", "Final", "Scheduled", "Preview"
    isTopInning: Boolean,
    inning: Int,
    inningSuffix: String?, // "st", "nd", "rd", "th" -
) {
    val textToShow = when (status) {
        "In Progress" -> "${if (isTopInning) "Top" else "Bot"} ${inning}${inningSuffix ?: ""}"
        "Final" -> "Final"
        "No Game Today" -> "No Game Today"
        else -> "Today"
    }

    Text(
        text = textToShow,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TeamScoreColumn(
    teamName: String,
    score: Long,
    isBold: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = teamName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp), // Larger score
            fontWeight = FontWeight.ExtraBold,
            color = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
fun TeamStandingsSnippet(
    modifier: Modifier = Modifier,
    standingInfo: TeamStandingInfo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Team Info ──
            Column {
                Text(
                    text = "${standingInfo.teamFullName} (${standingInfo.wins}-${standingInfo.losses})",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = standingInfo.divisionName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Ranking & GB ──
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatRank(standingInfo.divisionRank),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = when {
                        standingInfo.divisionRank == 1 && (standingInfo.gamesBehind == "-" || standingInfo.gamesBehind == "0.0") -> "Leading"
                        standingInfo.gamesBehind == "0.0" -> "Tied"
                        else -> "${standingInfo.gamesBehind} GB"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TeamMVPRowList(mvps: List<LeaderSummary>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Team MVPs",
                style = MaterialTheme.typography.titleMedium
            )

            TextButton(
                onClick = {  }
            ) {
                Text("View Roster")
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mvps) { mvp ->
                TeamMVPCardRowItem(mvp)
            }
        }
    }
}



@Composable
fun TeamMVPCardRowItem(mvp: LeaderSummary) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(100.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = mvp.category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = mvp.playerName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Text(
                text = "ID: ${mvp.playerId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}




fun formatRank(rank: Int?): String {
    return when (rank) {
        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"
        else -> "${rank}th"
    }
}



@Preview(showBackground = true, name = "Standings Snippet - 1st Place")
@Composable
fun StandingsSnippetFirstPlacePreview() {
    PhilliesUpdaterTheme {
        TeamStandingsSnippet(
            standingInfo = TeamStandingInfo(
                teamAbbreviation = "PHI",
                teamFullName = "Philadelphia Phillies",
                divisionRank = 1,
                gamesBehind = "-", // Or "0.0"
                wins = 70,
                losses = 45,
                divisionName = "NL East"
            )
        )
    }
}

@Preview(showBackground = true, name = "Standings Snippet - 2nd Place")
@Composable
fun StandingsSnippetSecondPlacePreview() {
    PhilliesUpdaterTheme {
        TeamStandingsSnippet(
            standingInfo = TeamStandingInfo(
                teamAbbreviation = "ATL",
                teamFullName = "Atlanta Braves",
                divisionRank = 2,
                gamesBehind = "3.5",
                wins = 66,
                losses = 48,
                divisionName = "NL East"
            )
        )
    }
}

@Preview(showBackground = true, name = "Standings Snippet - Further Back")
@Composable
fun StandingsSnippetFurtherBackPreview() {
    PhilliesUpdaterTheme {
        TeamStandingsSnippet(
            standingInfo = TeamStandingInfo(
                teamAbbreviation = "NYM",
                teamFullName = "New York Mets",
                divisionRank = 4,
                gamesBehind = "10.0",
                wins = 60,
                losses = 55,
                divisionName = "NL East"
            )
        )
    }
}






