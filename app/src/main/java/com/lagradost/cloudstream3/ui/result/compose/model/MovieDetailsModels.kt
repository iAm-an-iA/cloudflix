package com.lagradost.cloudstream3.ui.result.compose.model

import android.content.Context
import androidx.compose.runtime.Immutable
import com.lagradost.cloudstream3.ActorData
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.ui.result.ResultEpisode
import com.lagradost.cloudstream3.ui.result.ResumeWatchingStatus
import com.lagradost.cloudstream3.ui.result.compose.components.MovieCardItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class MovieDetailsUiState(
    val title: String = "",
    val providerName: String? = null,
    val backdropUrl: String? = null,
    val posterUrl: String? = null,
    val logoUrl: String? = null,
    val matchScore: String? = null,
    val releaseYear: String? = null,
    val seasonsCount: String? = null,
    val maturityRating: String? = null,
    val advisories: PersistentList<String> = persistentListOf(),
    val statusText: String? = null,
    val isOngoing: Boolean = false,
    val nextAiringUnixTime: Long? = null,
    val nextAiringEpisode: String? = null,
    val nextAiringDate: String? = null,
    val synopsis: String = "",
    val genres: PersistentList<String> = persistentListOf(),
    val actors: PersistentList<ActorData> = persistentListOf(),
    val episodes: PersistentList<ResultEpisode> = persistentListOf(),
    val recommendations: PersistentList<SearchResponse> = persistentListOf(),
    val seasons: PersistentList<String> = persistentListOf(),
    val selectedSeasonIndex: Int = 0,
    val dubs: PersistentList<String> = persistentListOf(),
    val selectedDubIndex: Int = 0,
    val ranges: PersistentList<String> = persistentListOf(),
    val selectedRangeIndex: Int = 0,
    val resumeStatus: ResumeWatchingStatus? = null,
    val isMovie: Boolean = false,
    val isInWatchList: Boolean = false,
    val isFavorite: Boolean = false,
    val trailers: PersistentList<MovieTrailerData> = persistentListOf(),
    val isLoaded: Boolean = false,
    val comingSoon: Boolean = false
)

sealed interface MovieDetailsAction {
    data class SelectSeason(val index: Int) : MovieDetailsAction
    data class SelectDub(val index: Int) : MovieDetailsAction
    data class SelectRange(val index: Int) : MovieDetailsAction
    data class ClickEpisode(val episode: ResultEpisode) : MovieDetailsAction
    data class LongClickEpisode(val episode: ResultEpisode) : MovieDetailsAction
    data object PlayPrimary : MovieDetailsAction
    data object PlayPrimaryLong : MovieDetailsAction
    data object ClickTrailer : MovieDetailsAction
    data object ToggleBookmark : MovieDetailsAction
    data object ToggleFavorite : MovieDetailsAction
    data object ClickSearch : MovieDetailsAction
    data class ClickRecommendation(val response: SearchResponse) : MovieDetailsAction
    data class ClickActor(val actor: ActorData) : MovieDetailsAction
}

@Immutable
data class MovieTrailerData(
    val title: String,
    val runtime: String,
    val rawTrailer: Any? = null
)

@Immutable
data class MovieRecommendationRow(
    val rowIndex: Int,
    val items: PersistentList<MovieCardItem> = persistentListOf()
)

private fun formatEpisodeCode(context: Context, season: Int?, episode: Int?): String {
    val s = season ?: 0
    val e = episode ?: 0
    val sShort = context.getString(R.string.season_short)
    val eShort = context.getString(R.string.episode_short)
    return when {
        s > 0 && e > 0 -> "$sShort$s:$eShort$e"
        e > 0 -> "$eShort$e"
        else -> ""
    }
}

private fun formatWithEpisodeCode(prefix: String, epCode: String): String {
    return if (epCode.isNotBlank()) "$prefix $epCode" else prefix
}

private fun resolveResumeButtonText(context: Context, resumeStatus: ResumeWatchingStatus): String {
    val prefix = if (resumeStatus.progress != null) {
        context.getString(R.string.resume)
    } else {
        context.getString(R.string.play_movie_button)
    }
    if (resumeStatus.isMovie) {
        return prefix
    }
    val resumeEp = resumeStatus.result
    val epCode = formatEpisodeCode(context, resumeEp.season, resumeEp.episode)
    return formatWithEpisodeCode(prefix, epCode)
}

private fun resolveFirstEpisodeButtonText(
    context: Context,
    episodesToDisplay: List<ResultEpisode>,
    isMovie: Boolean
): String {
    val playStr = context.getString(R.string.play_movie_button)
    val firstEp = episodesToDisplay.firstOrNull()
    if (firstEp != null && !isMovie) {
        val epCode = formatEpisodeCode(context, firstEp.season, firstEp.episode)
        return formatWithEpisodeCode(playStr, epCode)
    }
    return playStr
}

fun getPlayButtonText(
    context: Context,
    resumeStatus: ResumeWatchingStatus?,
    episodesToDisplay: List<ResultEpisode>,
    isMovie: Boolean
): String {
    if (resumeStatus != null) {
        return resolveResumeButtonText(context, resumeStatus)
    }
    return resolveFirstEpisodeButtonText(context, episodesToDisplay, isMovie)
}
