package nicholos.tyler.philliesupdater.Pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import nicholos.tyler.philliesupdater.screens.SettingsScreen
import nicholos.tyler.philliesupdater.viewmodel.SettingsViewModel

@Composable
fun SettingsPage(viewModel: SettingsViewModel = hiltViewModel()) {
    val selectedTeam by viewModel.selectedTeam.collectAsState()

    SettingsScreen(
        selectedTeam = selectedTeam,
        onTeamSelected = viewModel::onTeamSelected
    )
}