package nicholos.tyler.philliesupdater.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nicholos.tyler.philliesupdater.PlayerRoster
import nicholos.tyler.philliesupdater.TeamDetails
import nicholos.tyler.philliesupdater.viewmodel.RosterUiState
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun RosterScreen(uiState: RosterUiState) {
    TeamRoster(uiState.roster)
}

@Composable
fun TeamHeader(team: TeamDetails) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(team.name ?: "Team Name", style = MaterialTheme.typography.headlineMedium)
        Text("${team.locationName} • ${team.division?.name} Division", style = MaterialTheme.typography.bodyLarge)
        team.record?.let {
            Text("W-L: ${it.wins}-${it.losses} (${it.winningPercentage})", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Composable
fun TeamRoster(roster: List<PlayerRoster>) {
    val groupedByPosition = roster.groupBy { it.position.name }

    LazyColumn {
        groupedByPosition.forEach { (positionName, players) ->
            item {
                Text(
                    text = positionName ?: "Unknown Position",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }

            items(players, key = { it.jerseyNumber}) { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(player.person.fullName)
                    Text("#${player.jerseyNumber}")
                }
            }
        }
    }
}