package com.lagradost.cloudstream3.ui.result

import androidx.compose.ui.unit.dp
import com.lagradost.cloudstream3.ui.result.compose.components.MovieBadgeType
import com.lagradost.cloudstream3.ui.result.compose.components.MovieCardDefaults
import com.lagradost.cloudstream3.ui.result.compose.components.MovieCardSize
import com.lagradost.cloudstream3.ui.result.compose.components.MovieCardType
import com.lagradost.cloudstream3.ui.result.compose.components.MovieDetailsTokens
import com.lagradost.cloudstream3.ui.result.compose.components.RatingGreen
import com.lagradost.cloudstream3.ui.result.compose.components.RatingRed
import com.lagradost.cloudstream3.ui.result.compose.components.RatingYellow
import com.lagradost.cloudstream3.ui.result.compose.components.getRatingScoreColor
import com.lagradost.cloudstream3.ui.result.compose.model.MovieDetailsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieDetailsComposeTest {

    @Test
    fun testMovieDetailsUiStateDefaults() {
        val state = MovieDetailsUiState()
        assertEquals("", state.title)
        assertFalse(state.isLoaded)
        assertFalse(state.isMovie)
        assertFalse(state.isInWatchList)
        assertFalse(state.isFavorite)
        assertTrue(state.episodes.isEmpty())
        assertTrue(state.recommendations.isEmpty())
    }

    @Test
    fun testRatingScoreColors() {
        assertEquals(RatingGreen, getRatingScoreColor("98%"))
        assertEquals(RatingGreen, getRatingScoreColor("8.5"))
        assertEquals(RatingYellow, getRatingScoreColor("60%"))
        assertEquals(RatingYellow, getRatingScoreColor("5.5"))
        assertEquals(RatingRed, getRatingScoreColor("40%"))
        assertEquals(RatingRed, getRatingScoreColor("3.2"))
        assertEquals(RatingGreen, getRatingScoreColor("New"))
        assertEquals(RatingGreen, getRatingScoreColor(null))
    }

    @Test
    fun testMovieCardDefaultsAndTokens() {
        assertEquals(1.05f, MovieDetailsTokens.FOCUS_SCALE_FACTOR)
        assertEquals(
            218.dp,
            MovieCardDefaults.cardWidth(
                MovieCardType.DEFAULT,
                MovieCardSize.MEDIUM
            )
        )
        assertEquals(
            128.dp,
            MovieCardDefaults.cardWidth(
                MovieCardType.DEFAULT,
                MovieCardSize.SMALL
            )
        )
        assertEquals(
            140.dp,
            MovieCardDefaults.cardWidth(
                MovieCardType.POSTER,
                MovieCardSize.MEDIUM
            )
        )
        assertTrue(MovieBadgeType.TOP_10.stringRes != 0)
        assertTrue(MovieBadgeType.RECENTLY_ADDED.stringRes != 0)
        assertTrue(MovieBadgeType.NEW_SEASON.stringRes != 0)
    }
}
