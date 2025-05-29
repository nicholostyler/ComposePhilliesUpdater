package nicholos.tyler.philliesupdater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nicholos.tyler.philliesupdater.ui.theme.PhilliesUpdaterTheme
import androidx.lifecycle.viewmodel.compose.viewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhilliesUpdaterTheme {
                val baseballViewModel : BaseballViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomePage(modifier = Modifier.padding(innerPadding), baseballViewModel)
                }
            }
        }
    }
}

@Composable
fun HomePage(modifier: Modifier, baseballVM: BaseballViewModel) {
    val scheduleData by baseballVM.baseballScheduleData.collectAsState()
    val gameDetails by baseballVM.baseballGameData.collectAsState()
    val selectedGame by baseballVM.selectedGame.collectAsState()

    LaunchedEffect(Unit) {
        baseballVM.fetchBaseballSchedule()
    }



    if (scheduleData != null) {
        LaunchedEffect(Unit) {
            baseballVM.setSelectedGame(scheduleData!!.dates?.get(0)?.games?.get(0)!!)
        }

        Column(modifier = modifier.fillMaxSize()) {
            ScheduleCardList(baseballVM, scheduleData!!.dates, Modifier.fillMaxWidth())
            ScoreCard(Modifier.fillMaxWidth(), selectedGame)

            if (gameDetails != null) {
                gameDetails?.liveData?.plays?.let {
                    it.allPlays?.let { it1 ->
                        GameDetailList(
                            Modifier.fillMaxSize(),
                            it1
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }


}

@Composable
fun ScheduleCard(baseballVM: BaseballViewModel, game: Game, modifier: Modifier = Modifier) {
    Card(
                modifier = modifier
                    .width(200.dp)
                    .height(100.dp)
                    .then(modifier)
                    .clickable {
                            baseballVM.setSelectedGame(game)
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize() // Fill the card
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween, // Example arrangement
                    horizontalAlignment = Alignment.Start
                ) {
                    DateHelper.formatIsoDateToDisplayString(game.gameDate.toString())?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = game.teams?.away?.team?.name.toString() + " @ " + game.teams?.home?.team?.name.toString(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
}

@Composable
fun ScheduleCardList(baseballVM: BaseballViewModel, dates: List<Date>?, modifier: Modifier = Modifier) {
    val validGames = dates
        ?.flatMap { date ->
            date.games
                ?.filter { it?.status?.detailedState != "Postponed" }
                ?.mapNotNull { game -> game }
                ?: emptyList()
        } ?: emptyList()

    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // Padding around the list
        horizontalArrangement = Arrangement.spacedBy(12.dp) // Space between cards
    ) {
        items(
            items = validGames,
            key = { it.gamePk!! },
        ) { game ->
                ScheduleCard(baseballVM, game, modifier)
        }
    }
}

@Composable
fun ScoreCard(modifier: Modifier, game: Game?) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (game == null) {
                Text("Loading game...", style = MaterialTheme.typography.bodyMedium)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(game.teams?.away?.team?.name.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                        Text(game.teams?.away?.score?.toString().takeUnless { it.isNullOrEmpty() } ?: "0", style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(game.teams?.home?.team?.name.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                        Text(game.teams?.home?.score?.toString().takeUnless { it.isNullOrEmpty() } ?: "0", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}


@Composable
fun GameDetailList(modifier: Modifier, plays: List<Play>) {
    if (plays.isNullOrEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Game has not happened yet",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(), // LazyColumn takes the full available space
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ), // Padding around the whole list
            verticalArrangement = Arrangement.spacedBy(8.dp) // Padding around the list
        ) {
            item {
                Text(
                    text = "Game Details",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(items = plays!!, key = { it.result?.description.toString() }) { play ->
                play.result?.description?.let { Text(text = it) }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PhilliesUpdaterTheme {
        val game = Game(teams = Teams(away = Away(team = Team(name = "Atlanta Braves"), score = 1), home = Home(team = Team(name = "Philadelphia Phillies"), score = 5)))
        val dates = listOf(Date(games = listOf(game)))
        val selectedGame = game
        Column(modifier = Modifier) {
            //ScheduleCardList(dates, Modifier)
            ScoreCard(Modifier, game)
        }

    }
}