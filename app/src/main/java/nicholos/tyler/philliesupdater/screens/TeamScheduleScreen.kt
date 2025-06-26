package nicholos.tyler.philliesupdater.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nicholos.tyler.philliesupdater.Date
import nicholos.tyler.philliesupdater.Game
import nicholos.tyler.philliesupdater.MLBTeam
import nicholos.tyler.philliesupdater.Screen
import nicholos.tyler.philliesupdater.components.GameScheduleCard
import nicholos.tyler.philliesupdater.viewmodel.TeamScheduleViewModel.ScheduleGameData

@Composable
fun TeamScheduleScreen(selectedTeam: MLBTeam, scheduledGames: List<ScheduleGameData>, onGameClick: (Game) -> Unit) {
    TeamScheduleList(Modifier.fillMaxSize(), scheduledGames, selectedTeam.name, onGameClick)
}

@Composable
fun TeamScheduleList(modifier: Modifier, games: List<ScheduleGameData>, selectedTeamName: String, onGameClick: (Game) -> Unit ) {
    LazyColumn(modifier = modifier) {
        items(games, key = { game ->  "${game.game.gamePk}_${game.game.status?.detailedState}" }) { game ->
            GameScheduleCard(
                date = game.date,
                time = game.time,
                awayTeamName = game.awayTeamName,
                opponentLogoUrl = "",
                homeTeamName = game.homeTeamName,
                homeTeamScore = game.homeTeamScore,
                awayTeamScore = game.awayTeamScore,
                status = game.status,
                venue = game.venue,
                yourTeamName = selectedTeamName,
                onClick = {onGameClick(game.game)},

            )
        }
    }
}