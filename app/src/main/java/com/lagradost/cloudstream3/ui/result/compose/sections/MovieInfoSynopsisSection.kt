package com.lagradost.cloudstream3.ui.result.compose.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream4.theme.CloudStreamTheme
import com.lagradost.cloudstream3.ui.result.compose.components.AiringCountdownBadge
import com.lagradost.cloudstream3.ui.result.compose.components.MaturityRatingBadge
import com.lagradost.cloudstream3.ui.result.compose.components.MovieDetailsTokens
import com.lagradost.cloudstream3.ui.result.compose.components.OngoingStatusBadge
import com.lagradost.cloudstream3.ui.result.compose.components.VideoQualityBadge
import com.lagradost.cloudstream3.ui.result.compose.components.getRatingScoreColor
import com.lagradost.cloudstream3.ui.result.compose.model.AiringScheduleUiState

@Composable
private fun PrimaryMetadataRow(
    matchScore: String?,
    releaseYear: String?,
    seasonsCount: String?,
    quality: String?,
    airingSchedule: AiringScheduleUiState?
) {
    val hasContent = !matchScore.isNullOrBlank() || !releaseYear.isNullOrBlank() ||
            !seasonsCount.isNullOrBlank() || !quality.isNullOrBlank() ||
            airingSchedule != null
    if (!hasContent) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!matchScore.isNullOrBlank()) {
            Text(
                text = matchScore,
                color = getRatingScoreColor(matchScore),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (!releaseYear.isNullOrBlank()) {
            Text(
                text = releaseYear,
                color = CloudStreamTheme.colors.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (!seasonsCount.isNullOrBlank()) {
            Text(
                text = seasonsCount,
                color = CloudStreamTheme.colors.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (!quality.isNullOrBlank()) {
            VideoQualityBadge(quality = quality)
        }

        if (!airingSchedule?.statusText.isNullOrBlank()) {
            OngoingStatusBadge(
                statusText = airingSchedule.statusText,
                isOngoing = airingSchedule.isOngoing
            )
        }

        if (airingSchedule?.hasAiringInfo == true) {
            AiringCountdownBadge(airingSchedule = airingSchedule)
        }
    }
}

@Composable
private fun MaturityAdvisoriesRow(maturityRating: String?, advisories: String?) {
    if (maturityRating.isNullOrBlank() && advisories.isNullOrBlank()) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!maturityRating.isNullOrBlank()) {
            MaturityRatingBadge(rating = maturityRating)
        }
        if (!advisories.isNullOrBlank()) {
            Text(
                text = advisories,
                color = CloudStreamTheme.colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun Top10RankBadge(top10RankText: String?) {
    if (top10RankText.isNullOrBlank()) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(MovieDetailsTokens.ShapeCardSmall)
                .background(CloudStreamTheme.colors.primary)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = stringResource(id = R.string.top_10_badge_text),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
        Text(
            text = top10RankText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = CloudStreamTheme.colors.onBackground
        )
    }
}

@Composable
private fun InteractiveSynopsis(synopsis: String) {
    if (synopsis.isBlank()) return

    val synopsisInteractionSource = remember { MutableInteractionSource() }
    val isSynopsisFocused by synopsisInteractionSource.collectIsFocusedAsState()

    val background = if (isSynopsisFocused) CloudStreamTheme.colors.surfaceContainer.copy(alpha = 0.5f) else Color.Transparent
    val borderModifier = if (isSynopsisFocused) {
        Modifier.border(BorderStroke(2.dp, CloudStreamTheme.colors.primary), MovieDetailsTokens.ShapeCardSmall)
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MovieDetailsTokens.ShapeCardSmall)
            .background(background)
            .then(borderModifier)
            .focusable(interactionSource = synopsisInteractionSource)
            .padding(8.dp)
    ) {
        Text(
            text = synopsis,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSynopsisFocused) CloudStreamTheme.colors.onBackground else CloudStreamTheme.colors.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun MovieSideInfoItem(labelRes: Int, items: List<String>, maxLines: Int = Int.MAX_VALUE) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(id = labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = CloudStreamTheme.colors.onSurfaceVariant
        )
        Text(
            text = items.joinToString(", "),
            style = MaterialTheme.typography.bodySmall,
            color = CloudStreamTheme.colors.onBackground,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MovieSideInfoColumn(
    castList: List<String>,
    genres: List<String>,
    moodTags: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MovieSideInfoItem(labelRes = R.string.cast_label, items = castList, maxLines = 3)
        MovieSideInfoItem(labelRes = R.string.genres_label, items = genres)
        MovieSideInfoItem(labelRes = R.string.mood_tags_label, items = moodTags)
    }
}

@Composable
fun MovieInfoSynopsisSection(
    modifier: Modifier = Modifier,
    matchScore: String?,
    releaseYear: String?,
    seasonsCount: String?,
    quality: String?,
    maturityRating: String?,
    advisories: String?,
    top10RankText: String?,
    synopsis: String,
    castList: List<String>,
    genres: List<String>,
    moodTags: List<String>,
    airingSchedule: AiringScheduleUiState? = null
) {
    val hasRightColumn = castList.isNotEmpty() || genres.isNotEmpty() || moodTags.isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(if (hasRightColumn) 0.65f else 1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryMetadataRow(
                matchScore = matchScore,
                releaseYear = releaseYear,
                seasonsCount = seasonsCount,
                quality = quality,
                airingSchedule = airingSchedule
            )
            MaturityAdvisoriesRow(
                maturityRating = maturityRating,
                advisories = advisories
            )
            Top10RankBadge(top10RankText = top10RankText)
            InteractiveSynopsis(synopsis = synopsis)
        }

        if (hasRightColumn) {
            MovieSideInfoColumn(
                castList = castList,
                genres = genres,
                moodTags = moodTags,
                modifier = Modifier.weight(0.35f)
            )
        }
    }
}
