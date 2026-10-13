package dev.jdtech.jellyfin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ClickableSurfaceScale
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.core.presentation.dummy.dummyPerson
import dev.jdtech.jellyfin.models.FindroidItemPerson
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.spacings
import java.util.UUID

@Composable
fun PersonCard(
    person: FindroidItemPerson,
    onClick: (FindroidItemPerson) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.width(120.dp)) {
        Surface(
            onClick = { onClick(person) },
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
            border =
                ClickableSurfaceDefaults.border(
                    focusedBorder =
                        Border(BorderStroke(4.dp, Color.White), shape = RoundedCornerShape(10.dp))
                ),
            scale = ClickableSurfaceScale.None,
        ) {
            AsyncImage(
                model = person.image.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier.fillMaxWidth()
                        .aspectRatio(0.66f)
                        .background(MaterialTheme.colorScheme.surface),
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacings.small))
        Text(
            text = person.name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = person.role,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CastSection(
    actors: List<FindroidItemPerson>,
    itemsPadding: PaddingValues,
    onPersonClick: (personId: UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(CoreR.string.cast_amp_crew),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(itemsPadding),
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacings.medium))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.default),
            contentPadding = itemsPadding,
        ) {
            items(actors, key = { it.id }) { person ->
                PersonCard(person = person, onClick = { onPersonClick(it.id) })
            }
        }
    }
}

@Preview
@Composable
private fun PersonCardPreview() {
    FindroidTheme { PersonCard(person = dummyPerson, onClick = {}) }
}
