package nicholos.tyler.philliesupdater

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.compose.rememberAsyncImagePainter
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.util.DebugLogger

class GameDetailPage {

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun GameDetailScreen(modifier: Modifier, gamePk: Long, baseballVM: BaseballViewModel) {
        val detailUiState by baseballVM.detailPageUiState.collectAsState()
        val isRefreshing by baseballVM.detailPageRefreshing.collectAsState()
        val groupedPlays by baseballVM.groupedPlaysByInning.collectAsState()

        LaunchedEffect(gamePk) {
            baseballVM.refreshDetailPage(gamePk)
        }

        DisposableEffect(Unit) {
            onDispose {
                baseballVM.disposeDetailPage()
            }
        }

        AnimatedContent(
            targetState = isRefreshing,
            label = "refresh",
        ) { loading ->



            if (loading || (detailUiState.gameData == null)) {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            } else {
                val plays = detailUiState.plays
                GameDetailList(modifier, groupedPlays)
            }
        }
    }

    @Composable
    fun GameDetailList(modifier: Modifier, plays: Map<Pair<Int, Boolean>, List<Play>>) {
        if (plays.isEmpty()) {
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
                modifier = modifier.height(300.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                plays.forEach { (key, inningPlays) ->
                    val (inning, isTop) = key
                    val label = if (isTop) "Top" else "Bottom"

                    item {
                        Text(
                            "Inning $inning - $label",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(
                        inningPlays,
                        key = { it.playId ?: it.playEndTime ?: "${key.hashCode()}" }) { play ->
                        val playerId = play.matchup?.batter?.id
                        val imageUrl = "https://img.mlbstatic.com/mlb-photos/image/upload/w_213,q_100,f_jpg/v1/people/$playerId/headshot/67/current"


                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            if (imageUrl != null) {
                                Log.d("GameDetailList not null", "Image URL not null: $imageUrl")
                                Log.d("GameDetailList", "Image URL: $imageUrl")

                                val context = LocalContext.current
                                val imageLoader = LocalContext.current.imageLoader
                                val placeholderPainter = rememberAsyncImagePainter(
                                    model = "https://img.mlbstatic.com/mlb-photos/image/upload/w_213,d_people:generic:headshot:silo:current.png,q_auto:best,f_auto/v1/people/0/headshot/67/current"
                                )

                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageUrl)
                                        .crossfade(true)
                                        .listener(
                                            onError = { _, throwable ->
                                                Log.e("Coil", "FAIL: ${throwable.throwable}", )
                                            }
                                        )
                                        .build(),
                                    imageLoader = LocalContext.current.imageLoader,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = "Player headshot",
                                    placeholder = placeholderPainter,
                                    error = placeholderPainter,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)

                                )
                            } else {
                                Log.d("GameDetailList null", "Image URL null: $imageUrl")

                            }

                            Text(
                                text = play.result?.description ?: "No description",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

}