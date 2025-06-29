package nicholos.tyler.philliesupdater.Pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import nicholos.tyler.philliesupdater.screens.DivisionScreen
import nicholos.tyler.philliesupdater.viewmodel.DivisionViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import nicholos.tyler.philliesupdater.Screen
import nicholos.tyler.philliesupdater.StandingsRecord
import nicholos.tyler.philliesupdater.viewmodel.HomeViewModel


@Composable
fun DivisionPage(viewModel: DivisionViewModel, divisionRecords: StandingsRecord) {


    viewModel.setup(divisionRecords)
    val uiState by viewModel.uiState.collectAsState()
    DivisionScreen(uiState = uiState)
}