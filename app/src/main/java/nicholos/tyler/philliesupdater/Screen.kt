package nicholos.tyler.philliesupdater

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Games : Screen("games", "Games", Icons.Filled.DateRange)
    object League : Screen("league", "League", Icons.Filled.People)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Division : Screen("division", "Division", Icons.Filled.Info)

    object GameDetail : Screen("game_detail/{gamePk}", "Game Detail", Icons.Filled.Info) {
        fun createRoute(gamePk: Long) = "game_detail/$gamePk"
    }

    object TeamSchedule : Screen("team_schedule/{teamId}", "Team Schedule", Icons.Filled.Info) {
        fun createRoute(teamId: Long) = "team_schedule/$teamId"
    }

    object TeamRoster : Screen("team_roster/{teamId}", "Team Roster", Icons.Filled.Info) {
        fun createRoute(teamId: Long) = "team_roster/$teamId"
    }
}

