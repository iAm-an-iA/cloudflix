package com.lagradost.cloudstream3.ui.result.compose.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagradost.cloudstream3.ActorData
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream4.theme.CloudStreamTheme
import com.lagradost.cloudstream3.ui.result.compose.components.CastMemberCard

@Composable
fun CastAndCrewSection(
    actors: List<ActorData>,
    onActorClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    onActorLongClick: ((String) -> Unit)? = null
) {
    if (actors.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        Text(
            text = stringResource(id = R.string.cast_label).trimEnd(':'),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 22.sp,
            color = CloudStreamTheme.colors.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(
                items = actors,
                key = { it.actor.name }
            ) { actorData ->
                CastMemberCard(
                    actorData = actorData,
                    onActorClick = { name ->
                        onActorClick?.invoke(name)
                    },
                    onActorLongClick = { name ->
                        if (onActorLongClick != null) {
                            onActorLongClick(name)
                        } else {
                            onActorClick?.invoke(name)
                        }
                    }
                )
            }
        }
    }
}
