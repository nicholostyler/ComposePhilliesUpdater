package nicholos.tyler.philliesupdater.Pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import nicholos.tyler.philliesupdater.viewmodel.TeamScheduleViewModel
import androidx.compose.runtime.getValue
import nicholos.tyler.philliesupdater.Game
import nicholos.tyler.philliesupdater.screens.TeamScheduleScreen


@Composable
fun TeamSchedulePage(viewModel: TeamScheduleViewModel, teamId: Int, onGameSelected: (Game) -> Unit) {
    viewModel.setSelectedTeam(teamId = teamId)
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    val scheduledGames by viewModel.teamSchedule.collectAsState()
    TeamScheduleScreen(selectedTeam, scheduledGames, onGameSelected)
}