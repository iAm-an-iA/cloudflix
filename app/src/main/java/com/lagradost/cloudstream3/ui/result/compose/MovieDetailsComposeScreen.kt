package com.lagradost.cloudstream3.ui.result.compose

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagradost.cloudstream3.Actor
import com.lagradost.cloudstream3.ActorData
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.ui.result.ResultEpisode
import com.lagradost.cloudstream3.ui.result.ResumeWatchingStatus
import com.lagradost.cloudstream3.ui.result.compose.components.EpisodeRowItem
import com.lagradost.cloudstream3.ui.result.compose.components.MovieCardItem
import com.lagradost.cloudstream3.ui.result.compose.components.MovieCardType
import com.lagradost.cloudstream3.ui.result.compose.model.MovieDetailsAction
import com.lagradost.cloudstream3.ui.result.compose.model.MovieDetailsUiState
import com.lagradost.cloudstream3.ui.result.compose.model.MovieRecommendationRow
import com.lagradost.cloudstream3.ui.result.compose.model.MovieTrailerData
import com.lagradost.cloudstream3.ui.result.compose.model.getPlayButtonText
import com.lagradost.cloudstream3.ui.result.compose.model.resolveAiringSchedule
import kotlinx.collections.immutable.toPersistentList
import com.lagradost.cloudstream3.ui.result.compose.sections.AboutSection
import com.lagradost.cloudstream3.ui.result.compose.sections.CastAndCrewSection
import com.lagradost.cloudstream3.ui.result.compose.sections.EpisodesHeaderSection
import com.lagradost.cloudstream3.ui.result.compose.sections.HeroBannerSection
import com.lagradost.cloudstream3.ui.result.compose.sections.MovieInfoSynopsisSection
import com.lagradost.cloudstream3.ui.result.compose.sections.RecommendationRowView
import androidx.compose.material3.MaterialTheme
import com.lagradost.cloudstream4.theme.CloudStreamColorScheme
import com.lagradost.cloudstream4.theme.CloudStreamPreviewTheme
import com.lagradost.cloudstream4.theme.CloudStreamTheme

private fun LazyListScope.episodesSection(
    episodesToDisplay: List<ResultEpisode>,
    seasonOptions: List<String>,
    selectedSeasonText: String,
    onSeasonSelect: ((Int) -> Unit)?,
    onSeasonTextChange: (String) -> Unit,
    dubOptions: List<String>,
    selectedDubText: String,
    onDubSelect: ((Int) -> Unit)?,
    onDubTextChange: (String) -> Unit,
    rangeOptions: List<String>,
    selectedRangeText: String,
    onRangeSelect: ((Int) -> Unit)?,
    onRangeTextChange: (String) -> Unit,
    airingSchedule: com.lagradost.cloudstream3.ui.result.compose.model.AiringScheduleUiState?,
    onEpisodeClick: ((ResultEpisode) -> Unit)?,
    onEpisodeLongClick: ((ResultEpisode) -> Unit)?,
    showToast: (String) -> Unit
) {
    if (episodesToDisplay.isEmpty()) return

    item(key = "episodes_header", contentType = "episodes_header") {
        EpisodesHeaderSection(
            seasonOptions = seasonOptions,
            selectedSeasonText = selectedSeasonText,
            onSeasonSelect = onSeasonSelect,
            onSeasonTextChange = onSeasonTextChange,
            dubOptions = dubOptions,
            selectedDubText = selectedDubText,
            onDubSelect = onDubSelect,
            onDubTextChange = onDubTextChange,
            rangeOptions = rangeOptions,
            selectedRangeText = selectedRangeText,
            onRangeSelect = onRangeSelect,
            onRangeTextChange = onRangeTextChange,
            airingSchedule = airingSchedule
        )
    }

    items(
        items = episodesToDisplay,
        key = { ep -> "episode_${ep.id}" },
        contentType = { "episode_row" }
    ) { ep ->
        val onEpClick = remember(ep) {
            {
                if (onEpisodeClick != null) onEpisodeClick(ep)
                else showToast("Playing ${ep.headerName}: ${ep.name}")
            }
        }
        val onEpLongClick = remember(ep) {
            if (onEpisodeLongClick != null) {
                { onEpisodeLongClick(ep) }
            } else null
        }
        EpisodeRowItem(
            episode = ep,
            onClick = onEpClick,
            onLongClick = onEpLongClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
        )
    }
}

private fun LazyListScope.recommendationsSection(
    chunkedRecommendations: List<MovieRecommendationRow>,
    dynamicRecommendations: List<SearchResponse>?,
    onRecommendationClick: ((SearchResponse) -> Unit)?,
    showToast: (String) -> Unit,
    colors: CloudStreamColorScheme
) {
    if (chunkedRecommendations.isEmpty()) return

    item(key = "recommendations_header", contentType = "recommendations_header") {
        Text(
            text = stringResource(id = R.string.more_like_this),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 22.sp,
            color = colors.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 16.dp)
        )
    }

    items(
        items = chunkedRecommendations,
        key = { row -> "rec_row_${row.rowIndex}" },
        contentType = { "recommendation_row" }
    ) { row ->
        RecommendationRowView(
            row = row,
            onCardClick = { cardItem ->
                val rec = dynamicRecommendations?.find { it.name == cardItem.title }
                if (rec != null && onRecommendationClick != null) {
                    onRecommendationClick(rec)
                } else {
                    showToast("Opened recommendation: ${cardItem.title}")
                }
            }
        )
    }
}

private fun LazyListScope.castSection(
    dynamicActors: List<ActorData>?,
    onActorClick: ((String) -> Unit)?
) {
    if (dynamicActors.isNullOrEmpty()) return

    item(key = "cast_and_crew_section", contentType = "cast_section") {
        CastAndCrewSection(
            actors = dynamicActors,
            onActorClick = onActorClick,
            onActorLongClick = onActorClick
        )
    }
}

private fun LazyListScope.aboutSection(
    title: String,
    creator: String?,
    castList: List<String>,
    writers: List<String>,
    genres: List<String>,
    moodTags: List<String>,
    maturityRating: String?,
    advisories: String?
) {
    val hasAboutContent = !creator.isNullOrBlank() || castList.isNotEmpty() ||
            writers.isNotEmpty() || genres.isNotEmpty() || moodTags.isNotEmpty() ||
            !maturityRating.isNullOrBlank()
    if (!hasAboutContent) return

    item(key = "about_section", contentType = "about_section") {
        AboutSection(
            title = title,
            creator = creator,
            castList = castList,
            writers = writers,
            genres = genres,
            moodTags = moodTags,
            maturityRating = maturityRating,
            advisories = advisories
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieDetailsComposeScreen(
    state: MovieDetailsUiState,
    onAction: (MovieDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val heroHeight = remember(screenHeight) { (screenHeight * 0.65f).coerceAtLeast(380.dp) }

    val colors = CloudStreamTheme.colors
    val lazyListState = rememberLazyListState()

    val playInteractionSource = remember { MutableInteractionSource() }
    val inMyListInteractionSource = remember { MutableInteractionSource() }
    val likeInteractionSource = remember { MutableInteractionSource() }
    val trailerInteractionSource = remember { MutableInteractionSource() }
    val searchInteractionSource = remember { MutableInteractionSource() }

    val playButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        lazyListState.scrollToItem(0)
        playButtonFocusRequester.requestFocus()
    }

    val seasonOptions = state.seasons
    var selectedSeasonText by remember(state.selectedSeasonIndex, seasonOptions) {
        mutableStateOf(seasonOptions.getOrElse(state.selectedSeasonIndex) { seasonOptions.firstOrNull() ?: "" })
    }

    val dubOptions = state.dubs
    var selectedDubText by remember(state.selectedDubIndex, dubOptions) {
        mutableStateOf(dubOptions.getOrElse(state.selectedDubIndex) { dubOptions.firstOrNull() ?: "" })
    }

    val rangeOptions = state.ranges
    var selectedRangeText by remember(state.selectedRangeIndex, rangeOptions) {
        mutableStateOf(rangeOptions.getOrElse(state.selectedRangeIndex) { rangeOptions.firstOrNull() ?: "" })
    }

    val showToast: (String) -> Unit = remember(context) {
        { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val episodesToDisplay = state.episodes

    val airingSchedule = remember(
        state.statusText,
        state.isOngoing,
        state.nextAiringUnixTime,
        state.nextAiringEpisode,
        state.nextAiringDate,
        episodesToDisplay,
        context
    ) {
        resolveAiringSchedule(
            context = context,
            statusText = state.statusText,
            isOngoing = state.isOngoing,
            nextAiringUnixTime = state.nextAiringUnixTime,
            nextAiringEpisode = state.nextAiringEpisode,
            nextAiringDate = state.nextAiringDate,
            episodes = episodesToDisplay
        )
    }

    val playButtonText = remember(state.resumeStatus, episodesToDisplay, state.isMovie, context) {
        getPlayButtonText(context, state.resumeStatus, episodesToDisplay, state.isMovie)
    }

    val resumeProgressFraction = remember(state.resumeStatus) {
        val prog = state.resumeStatus?.progress
        if (prog != null && prog.maxProgress > 0) {
            prog.progress.toFloat() / prog.maxProgress.toFloat()
        } else {
            null
        }
    }

    val castList = remember(state.actors) {
        state.actors.map { it.actor.name }.filter { it.isNotBlank() }
    }

    val chunkedRecommendations = remember(state.recommendations) {
        if (state.recommendations.isEmpty()) {
            emptyList()
        } else {
            state.recommendations.map { rec ->
                MovieCardItem(
                    title = rec.name,
                    type = MovieCardType.POSTER,
                    posterUrl = rec.posterUrl,
                    showLogo = false,
                    showBottomTitle = true
                )
            }.chunked(6).mapIndexed { idx, list ->
                MovieRecommendationRow(idx, list.toPersistentList())
            }
        }
    }

    val tvBringIntoViewSpec = remember {
        object : BringIntoViewSpec {
            override fun calculateScrollDistance(
                offset: Float,
                size: Float,
                containerSize: Float
            ): Float {
                val parentFraction = 0.30f
                val targetOffset = parentFraction * containerSize
                return offset - targetOffset
            }
        }
    }

    CompositionLocalProvider(LocalBringIntoViewSpec provides tvBringIntoViewSpec) {
        LazyColumn(
            state = lazyListState,
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            item(key = "hero_banner", contentType = "hero_banner") {
                HeroBannerSection(
                    title = state.title,
                    providerName = state.providerName,
                    backdropUrl = state.backdropUrl,
                    logoUrl = state.logoUrl,
                    heroHeight = heroHeight,
                    playButtonText = playButtonText,
                    resumeProgressFraction = resumeProgressFraction,
                    isInWatchList = state.isInWatchList,
                    isFavorite = state.isFavorite,
                    hasTrailers = state.trailers.isNotEmpty(),
                    playButtonFocusRequester = playButtonFocusRequester,
                    playInteractionSource = playInteractionSource,
                    inMyListInteractionSource = inMyListInteractionSource,
                    likeInteractionSource = likeInteractionSource,
                    trailerInteractionSource = trailerInteractionSource,
                    searchInteractionSource = searchInteractionSource,
                    onPlayClick = { onAction(MovieDetailsAction.PlayPrimary) },
                    onPlayLongClick = { onAction(MovieDetailsAction.PlayPrimaryLong) },
                    onAddToListClick = { onAction(MovieDetailsAction.ToggleBookmark) },
                    onLikeClick = { onAction(MovieDetailsAction.ToggleFavorite) },
                    onTrailerClick = { onAction(MovieDetailsAction.ClickTrailer) },
                    onSearchClick = { onAction(MovieDetailsAction.ClickSearch) }
                )
            }

            item(key = "movie_info_synopsis", contentType = "movie_info") {
                MovieInfoSynopsisSection(
                    matchScore = state.matchScore,
                    releaseYear = state.releaseYear,
                    seasonsCount = state.seasonsCount,
                    quality = null,
                    maturityRating = state.maturityRating,
                    advisories = if (state.advisories.isNotEmpty()) state.advisories.joinToString(", ") else null,
                    top10RankText = null,
                    synopsis = state.synopsis,
                    castList = castList,
                    genres = state.genres,
                    moodTags = emptyList(),
                    airingSchedule = airingSchedule
                )
            }

            episodesSection(
                episodesToDisplay = episodesToDisplay,
                seasonOptions = seasonOptions,
                selectedSeasonText = selectedSeasonText,
                onSeasonSelect = { idx ->
                    onAction(MovieDetailsAction.SelectSeason(idx))
                },
                onSeasonTextChange = { selectedSeasonText = it },
                dubOptions = dubOptions,
                selectedDubText = selectedDubText,
                onDubSelect = { idx ->
                    onAction(MovieDetailsAction.SelectDub(idx))
                },
                onDubTextChange = { selectedDubText = it },
                rangeOptions = rangeOptions,
                selectedRangeText = selectedRangeText,
                onRangeSelect = { idx ->
                    onAction(MovieDetailsAction.SelectRange(idx))
                },
                onRangeTextChange = { selectedRangeText = it },
                airingSchedule = airingSchedule,
                onEpisodeClick = { ep ->
                    onAction(MovieDetailsAction.ClickEpisode(ep))
                },
                onEpisodeLongClick = { ep ->
                    onAction(MovieDetailsAction.LongClickEpisode(ep))
                },
                showToast = showToast
            )

            recommendationsSection(
                chunkedRecommendations = chunkedRecommendations,
                dynamicRecommendations = state.recommendations,
                onRecommendationClick = { rec ->
                    onAction(MovieDetailsAction.ClickRecommendation(rec))
                },
                showToast = showToast,
                colors = colors
            )

            castSection(
                dynamicActors = state.actors,
                onActorClick = { actorName ->
                    onAction(MovieDetailsAction.ClickActor(ActorData(Actor(actorName))))
                }
            )

            aboutSection(
                title = state.title,
                creator = null,
                castList = castList,
                writers = emptyList(),
                genres = state.genres,
                moodTags = emptyList(),
                maturityRating = state.maturityRating,
                advisories = if (state.advisories.isNotEmpty()) state.advisories.joinToString(", ") else null
            )
        }
    }
}

@Preview(
    name = "TV 1080p Landscape",
    device = "spec:width=1920dp,height=1080dp,dpi=320,orientation=landscape",
    showBackground = true,
    backgroundColor = 0xFF141414
)
@Composable
private fun MovieDetailsComposeScreenPreview() {
    CloudStreamPreviewTheme {
        MovieDetailsComposeScreen(
            state = MovieDetailsUiState(
                title = "Stranger Things",
                providerName = "Netflix",
                matchScore = "98% Match",
                releaseYear = "2024",
                seasonsCount = "4 Seasons",
                maturityRating = "16+",
                advisories = kotlinx.collections.immutable.persistentListOf("fear", "language", "violence"),
                statusText = "Ongoing",
                isOngoing = true,
                nextAiringEpisode = "Episode 5",
                nextAiringDate = "2d 14h",
                synopsis = "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.",
                genres = kotlinx.collections.immutable.persistentListOf("Sci-Fi", "Horror", "Drama")
            ),
            onAction = {}
        )
    }
}
