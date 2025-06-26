//package nicholos.tyler.philliesupdater
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
//import androidx.compose.material3.LoadingIndicator
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.navigation.NavController
//import androidx.navigation.NavHostController
//import nicholos.tyler.philliesupdater.screens.GameScheduleCard
//
//data class ScheduleGameData(
//    val date: String,
//    val time: String,
//    val awayTeamName: String,
//    val opponentLogoUrl: String?,
//    val homeTeamName: String,
//    val homeTeamScore: Long,
//    val awayTeamScore: Long,
//    val status: String,
//    val venue: String ,
//    val prefix: String,
//    val game: Game
//)
//
//@OptIn(ExperimentalMaterial3ExpressiveApi::class)
//@Composable
//fun SchedulePage(modifier: Modifier, baseballVM: BaseballViewModel, navController: NavHostController) {
//    val scheduleUIPage by baseballVM.scheduleUiState.collectAsState()
//    val isRefreshing by baseballVM.schedulePageRefreshing.collectAsState()
//
//
//    LaunchedEffect(Unit) {
//        baseballVM.getSchedulePage()
//    }
//
//    if (scheduleUIPage.gameCardList.isNotEmpty()) {
//        //GameScheduleList(games = scheduleUIPage.gameCardList, modifier = modifier, navController = navController)
//    } else {
//        Box(
//            modifier = modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            LoadingIndicator()
//        }
//    }
//
//
//}
//
//@Composable
//fun GameScheduleList(games: List<ScheduleGameData>, modifier: Modifier = Modifier, navController: NavController) {
//    LazyColumn(modifier = modifier) {
//        items(games, key = { game ->  "${game.game.gamePk}_${game.game.status?.detailedState}" }) { game ->
//            GameScheduleCard(
//                date = game.date,
//                time = game.time,
//                awayTeamName = game.awayTeamName,
//                opponentLogoUrl = "",
//                homeTeamName = game.homeTeamName,
//                homeTeamScore = game.homeTeamScore,
//                awayTeamScore = game.awayTeamScore,
//                status = game.status,
//                venue = game.venue,
//                navController = navController,
//                game = game.game
//            )
//        }
//    }
//}
//
//
//
