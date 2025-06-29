package nicholos.tyler.philliesupdater.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import nicholos.tyler.philliesupdater.LiveGameData
import nicholos.tyler.philliesupdater.viewmodel.GamesViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text

import nicholos.tyler.philliesupdater.items


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GamesScreen(gamesViewModel: GamesViewModel = hiltViewModel()) {
    val gamesUiState by gamesViewModel.gamesUiState.collectAsState()

    if (gamesUiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    } else if (gamesUiState.exception != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Error loading games: ${gamesUiState.exception}")
        }
    } else {
        GamesScheduleList(gamesUiState.liveGamesData)
    }
}


@Composable
fun GamesScheduleList(liveGamesData: List<LiveGameData>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(
            liveGamesData,
            key = { it.game.gamePk ?: it.hashCode() }
        ) { game ->
            LiveGameScoreCard(
                modifier = Modifier,
                isTopInning = game.isTopInning,
                inning = game.inning,
                inningSuffix = game.inningSuffix,
                awayTeamName = game.awayTeamName,
                homeTeamName = game.homeTeamName,
                awayTeamScore = game.awayTeamScore,
                homeTeamScore = game.homeTeamScore,
                outs = game.outs,
                leftOnBase = game.runnersOnBase,
                isGameOver = game.isGameOver,
                status = game.status,
                onGameClick = { },
            )
        }
    }
}
