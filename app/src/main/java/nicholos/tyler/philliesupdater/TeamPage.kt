package nicholos.tyler.philliesupdater

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamPage(modifier: Modifier, baseballVM: BaseballViewModel, navController: NavController) {
    LaunchedEffect(Unit) {
        baseballVM.refreshTeamPage()
    }

    val teamPageUiState by baseballVM.teamPageUiState.collectAsState()

    if (teamPageUiState.roster.isNotEmpty()) {
        TeamRoster(roster = teamPageUiState.roster)
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    }
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

            items(players) { player ->
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

