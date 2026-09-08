package com.lagradost.cloudstream3.ui.result.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.material3.MaterialTheme
import com.lagradost.cloudstream4.theme.CloudStreamTheme

enum class DetailsLogoVariant {
    FULL_COLOR,
    WHITE_MONO,
    MINIMAL_ICON
}

@Composable
fun DetailsLogoView(
    modifier: Modifier = Modifier,
    logoUrl: String? = null,
    titleFallback: String = "",
    variant: DetailsLogoVariant = DetailsLogoVariant.FULL_COLOR,
    height: Dp = 48.dp
) {
    val colors = CloudStreamTheme.colors

    if (!logoUrl.isNullOrBlank()) {
        AsyncImage(
            model = logoUrl,
            contentDescription = titleFallback,
            contentScale = ContentScale.Fit,
            alignment = Alignment.CenterStart,
            modifier = modifier
                .height(height)
                .fillMaxWidth(0.6f)
        )
    } else {
        Text(
            text = titleFallback,
            style = MaterialTheme.typography.headlineLarge,
            color = when (variant) {
                DetailsLogoVariant.FULL_COLOR -> colors.primary
                DetailsLogoVariant.WHITE_MONO -> colors.onBackground
                DetailsLogoVariant.MINIMAL_ICON -> colors.onBackground
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    }
}
