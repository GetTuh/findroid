package dev.jdtech.jellyfin.presentation.film

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.core.presentation.dummy.dummyMovies
import dev.jdtech.jellyfin.core.presentation.dummy.dummyPersonDetail
import dev.jdtech.jellyfin.film.presentation.person.PersonState
import dev.jdtech.jellyfin.film.presentation.person.PersonViewModel
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.presentation.film.components.ItemSection
import dev.jdtech.jellyfin.presentation.film.components.ItemSectionsLayout
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.spacings
import java.util.UUID

@Composable
fun PersonScreen(
    personId: UUID,
    onItemClick: (FindroidItem) -> Unit,
    viewModel: PersonViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(true) { viewModel.loadPerson(personId) }

    PersonScreenLayout(state = state, onItemClick = onItemClick)
}

@Composable
private fun PersonScreenLayout(state: PersonState, onItemClick: (FindroidItem) -> Unit) {
    val person = state.person
    if (person == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    val sections = buildList {
        if (state.starredInMovies.isNotEmpty()) {
            add(
                ItemSection(
                    key = "movies",
                    title = stringResource(CoreR.string.movies_label),
                    items = state.starredInMovies,
                )
            )
        }
        if (state.starredInShows.isNotEmpty()) {
            add(
                ItemSection(
                    key = "shows",
                    title = stringResource(CoreR.string.shows_label),
                    items = state.starredInShows,
                )
            )
        }
    }

    ItemSectionsLayout(
        title = person.name,
        sections = sections,
        isLoading = false,
        emptyText = null,
        onItemClick = onItemClick,
        header = {
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.default)) {
                AsyncImage(
                    model = person.images.primary,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier.width(120.dp)
                            .aspectRatio(0.66f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface),
                )
                Text(
                    text = person.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(640.dp),
                )
            }
        },
    )
}

@Preview(device = "id:tv_1080p")
@Composable
private fun PersonScreenLayoutPreview() {
    FindroidTheme {
        PersonScreenLayout(
            state = PersonState(person = dummyPersonDetail, starredInMovies = dummyMovies),
            onItemClick = {},
        )
    }
}
