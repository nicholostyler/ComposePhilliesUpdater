package nicholos.tyler.philliesupdater.Pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nicholos.tyler.philliesupdater.Divisions
import nicholos.tyler.philliesupdater.MLBTeam
import androidx.compose.foundation.lazy.items

@Composable
fun LeaguePage() {
    val divisions = Divisions.entries

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = divisions) { division ->
            val teamsInDivision = MLBTeam.values().filter { it.Divisions == division }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = division.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    teamsInDivision.forEach { team ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = team.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
