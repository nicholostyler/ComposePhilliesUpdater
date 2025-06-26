package nicholos.tyler.philliesupdater.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nicholos.tyler.philliesupdater.BaseballHelper
import nicholos.tyler.philliesupdater.Game

@Composable
fun GameScheduleCard(
    modifier: Modifier = Modifier,
    date: String, // e.g., "MON, JUL 29"
    time: String?, // e.g., "7:05 PM ET", null if game is final
    awayTeamName: String,
    opponentLogoUrl: String?, // Optional
    homeTeamName: String, // The user's selected team
    homeTeamScore: Long,
    awayTeamScore: Long?,
    status: String,
    venue: String, // e.g. "Citizens Bank Park"
    yourTeamName: String,
    onClick: () -> Unit
) {
    val isPastGame = status == "Final"
    val isFutureGame = status == "Scheduled"
    val opponentScore = if (homeTeamName == yourTeamName) awayTeamScore else homeTeamScore
    val yourTeamScore = if (homeTeamName == yourTeamName) homeTeamScore else awayTeamScore
    var win: Boolean = false

    if (homeTeamScore != null && awayTeamScore != null) {
        win = yourTeamScore!! > opponentScore!!
    }
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable {
                onClick()
            },

        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date & Time Column (Left)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.25f) // Give it some defined space
            ) {
                Text(
                    text = date.toString(), // e.g., "MON"
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isFutureGame && time != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .height(50.dp) // Adjust height as needed
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            // Game Info Column (Center)
            Column(
                modifier = Modifier
                    .weight(0.5f) // Main content area
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Team Logo (Optional)
                /*
                AsyncImage(
                    model = game.opponentLogoUrl,
                    contentDescription = "${game.opponentName} Logo",
                    modifier = Modifier
                        .size(24.dp) // Adjust size as needed
                        .padding(bottom = 4.dp),
                    // placeholder = painterResource(id = R.drawable.default_logo), // Optional
                    // error = painterResource(id = R.drawable.default_logo) // Optional
                )
                */
                Text(
                    text = BaseballHelper.Companion.abbreviateMatchup(
                        homeTeamName,
                        awayTeamName,
                        yourTeamName
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = venue,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Score/Outcome ──
            Column(horizontalAlignment = Alignment.End) {
                if (isPastGame && yourTeamScore != null && opponentScore != null) {
                    Text(
                        text = "$yourTeamScore - $opponentScore",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (win == true) "Win" else "Loss",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (win == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

        }
    }
}