package nicholos.tyler.philliesupdater.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nicholos.tyler.philliesupdater.BaseballHelper
import nicholos.tyler.philliesupdater.MLBTeam
import nicholos.tyler.philliesupdater.StandingsRecord
import nicholos.tyler.philliesupdater.TeamRecord
import nicholos.tyler.philliesupdater.viewmodel.DivisionUiState

@Composable
fun DivisionScreen(uiState: DivisionUiState) {
    val divisionName = uiState.teamName
    val teamRecords = uiState.divisionRecords
    DivisionStandings(teamRecords, divisionName)
}

@Composable
fun DivisionStandings(teamRecord: List<TeamRecord>, divisionName: String) {
    if (teamRecord.isNotEmpty()) {
        Column {
            val divisionRecords = teamRecord.sortedBy { it.divisionRank }
            for (record in divisionRecords) {
                if (record.team?.name != null) {
                    TeamStandingsSnippet(
                        modifier = Modifier.fillMaxWidth(),
                        standingInfo = TeamStandingInfo(
                            teamAbbreviation = BaseballHelper.abbreviateTeamName(record.team.name ?: ""),
                            teamFullName = record.team.name ?: "",
                            divisionRank = record.divisionRank?.toInt(),
                            gamesBehind = record.divisionGamesBack,
                            wins = record.wins,
                            losses = record.losses,
                            divisionName = divisionName
                        )
                    )
                }
            }
        }
    }
}

