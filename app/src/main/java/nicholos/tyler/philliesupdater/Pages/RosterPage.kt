package nicholos.tyler.philliesupdater.Pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import nicholos.tyler.philliesupdater.RosterResponse
import nicholos.tyler.philliesupdater.screens.RosterScreen
import nicholos.tyler.philliesupdater.viewmodel.RosterViewModel
import androidx.compose.runtime.getValue


@Composable
fun RosterPage(viewModel: RosterViewModel, teamId: Int) {
    viewModel.setSelectedTeam(teamId)
    val rosterUiState by viewModel.rosterUiState.collectAsState()
    RosterScreen(uiState = rosterUiState)
}