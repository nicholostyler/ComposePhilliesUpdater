package nicholos.tyler.philliesupdater

import android.app.Application
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import nicholos.tyler.philliesupdater.Pages.DivisionPage
import nicholos.tyler.philliesupdater.Pages.LeaguePage
import nicholos.tyler.philliesupdater.Pages.RosterPage
import nicholos.tyler.philliesupdater.Pages.SettingsPage
import nicholos.tyler.philliesupdater.Pages.TeamSchedulePage
import nicholos.tyler.philliesupdater.screens.GamesScreen
import nicholos.tyler.philliesupdater.screens.HomeScreen
import nicholos.tyler.philliesupdater.ui.theme.PhilliesUpdaterTheme
import nicholos.tyler.philliesupdater.viewmodel.HomeViewModel

val items = listOf(
    Screen.Home,
    Screen.Games,
    Screen.League,
    Screen.Settings
)

@HiltAndroidApp
class PhilliesUpdaterApp : Application() {

}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhilliesUpdaterTheme {
                val baseballViewModel : BaseballViewModel = viewModel()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val selectedTeam by baseballViewModel.selectedTeam.collectAsState()

                val currentScreen = items.find { screen ->
                    currentDestination?.hierarchy?.any { it.route == screen.route } == true
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (selectedTeam != null) {
                            val resolvedTitle = when {
                                currentDestination?.route?.startsWith("game_detail/") == true -> {
                                    "Game Details"
                                }
                                currentDestination?.route == Screen.League.route -> {
                                    selectedTeam?.name ?: "League"
                                }
                                currentDestination?.route?.startsWith("team_schedule/") == true -> {
                                    "Team Schedule"
                                }
                                currentDestination?.route?.startsWith("team_schedule/") == true -> {
                                    "Team Roster"
                                }
                                else -> {
                                    currentScreen?.label ?: "Phillies Updater"
                                }
                            }


                            TopAppBar(
                                title = {
                                    Text(text = resolvedTitle)
                                },
                                navigationIcon = {
                                    val isGameDetail = currentDestination?.route?.startsWith("game_detail/") == true
                                    val isTeamSchedule = currentDestination?.route?.startsWith("team_schedule/") == true
                                    val isTeamRoster = currentDestination?.route?.startsWith("team_roster/") == true

                                    if (isGameDetail || isTeamSchedule || isTeamRoster) {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
                                    }
                                },
                            )
                        }


                    },
                            bottomBar = {
                                val isGameDetail = currentDestination?.route?.startsWith("game_detail/") == true
                                val isTeamSchedule = currentDestination?.route?.startsWith("team_schedule/") == true
                                val isTeamRoster = currentDestination?.route?.startsWith("team_roster/") == true


                                if (!isGameDetail && !isTeamSchedule && !isTeamRoster) {
                                    NavigationBar {
                                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                                        val currentDestination = navBackStackEntry?.destination

                                        items.forEach { screen ->
                                            NavigationBarItem(
                                                icon = {
                                                    Icon(
                                                        screen.icon,
                                                        contentDescription = screen.label
                                                    )
                                                },
                                                label = { Text(screen.label) },
                                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                                onClick = {
                                                    navController.navigate(screen.route) {

                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding),

                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                modifier = Modifier.fillMaxSize(),
                                navController
                            )
                        }

                        composable(
                            route = Screen.Games.route,

                        ) {
                            GamesScreen()
                            //TeamSchedulePage(modifier = Modifier.fillMaxSize(), baseballViewModel, navController)
                        }
                        composable(
                            route = Screen.League.route,

                        ) {

                            LeaguePage()
                        }
                        composable(
                            route = Screen.Settings.route,

                        ) {
                            SettingsPage()
                        }
                        composable(Screen.GameDetail.route) { navBackStackEntry ->
                            val gamePk = navBackStackEntry.arguments?.getString("gamePk")?.toLongOrNull()
                            val detailUiState by baseballViewModel.detailPageUiState.collectAsState()

                            if (gamePk == null) {
                                Text("Invalid game!")
                                return@composable
                            } else {
                                GameDetailPage().GameDetailScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    gamePk = gamePk,
                                    baseballVM = baseballViewModel
                                )
                            }
                        }
                        composable(Screen.TeamSchedule.route) { navBackStackEntry ->
                            val teamId = navBackStackEntry.arguments?.getString("teamId")?.toIntOrNull()

                            if (teamId == null) {
                                Text("Invalid team!")
                                return@composable
                            } else {
                                TeamSchedulePage(
                                    viewModel = hiltViewModel(),
                                    teamId = teamId,
                                    onGameSelected = { game ->
                                        navController.navigate("game_detail/${game.gamePk}")
                                    })
                            }
                        }
                        composable(Screen.TeamRoster.route) { navBackStackEntry ->
                            val teamId = navBackStackEntry.arguments?.getString("teamId")?.toIntOrNull()

                            if (teamId == null) {
                                Text("Invalid team!")
                                return@composable
                            } else {
                                RosterPage(
                                    viewModel = hiltViewModel(),
                                    teamId = teamId,
                                    )
                            }
                        }
                        composable(Screen.Division.route) { navBackStackEntry ->
                            val parentEntry = remember(navBackStackEntry) {
                                navController.getBackStackEntry(Screen.Home.route)
                            }
                            val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)
                            val divisionRecords by homeViewModel.divisionStandings.collectAsState()

                            val isDivisionReady = remember(divisionRecords) {
                                divisionRecords.division != null && divisionRecords.teamRecords?.isNotEmpty() == true
                            }



                            if (divisionRecords == null) {
                                Text("Invalid division!")
                                return@composable
                            } else {
                                DivisionPage(
                                    viewModel = hiltViewModel(),
                                    divisionRecords = divisionRecords,
                                )
                            }
                        }

                    }
                }
            }
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