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
import nicholos.tyler.philliesupdater.screens.TeamRoster

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



