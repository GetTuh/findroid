package dev.jdtech.jellyfin.presentation.film

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ClickableSurfaceScale
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.core.presentation.dummy.dummyCollections
import dev.jdtech.jellyfin.film.presentation.media.MediaAction
import dev.jdtech.jellyfin.film.presentation.media.MediaState
import dev.jdtech.jellyfin.film.presentation.media.MediaViewModel
import dev.jdtech.jellyfin.models.CollectionType
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.spacings
import dev.jdtech.jellyfin.ui.components.Direction
import dev.jdtech.jellyfin.ui.components.ItemCard
import java.util.UUID

@Composable
fun MediaScreen(
    navigateToLibrary: (libraryId: UUID, libraryName: String, libraryType: CollectionType) -> Unit,
    navigateToFavorites: () -> Unit,
    isLoading: (Boolean) -> Unit,
    viewModel: MediaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) { viewModel.loadData() }

    LaunchedEffect(state.isLoading) { isLoading(state.isLoading) }

    LibrariesScreenLayout(
        state = state,
        onFavoritesClick = navigateToFavorites,
        onAction = { action ->
            when (action) {
                is MediaAction.OnItemClick -> {
                    navigateToLibrary(action.item.id, action.item.name, action.item.type)
                }
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@Composable
private fun LibrariesScreenLayout(
    state: MediaState,
    onFavoritesClick: () -> Unit,
    onAction: (MediaAction) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.libraries) { focusRequester.requestFocus() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.large),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.large),
        contentPadding =
            PaddingValues(
                start = MaterialTheme.spacings.large,
                top = MaterialTheme.spacings.small,
                end = MaterialTheme.spacings.large,
                bottom = MaterialTheme.spacings.large,
            ),
        modifier = Modifier.focusRequester(focusRequester),
    ) {
        item(key = "favorites") { FavoritesCard(onClick = onFavoritesClick) }
        items(state.libraries, key = { it.id }) { library ->
            ItemCard(
                item = library,
                direction = Direction.HORIZONTAL,
                onClick = { onAction(MediaAction.OnItemClick(library)) },
            )
        }
    }
}

@Composable
private fun FavoritesCard(onClick: () -> Unit) {
    Column(modifier = Modifier.width(260.dp)) {
        Surface(
            onClick = onClick,
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
            colors =
                ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            border =
                ClickableSurfaceDefaults.border(
                    focusedBorder =
                        Border(BorderStroke(4.dp, Color.White), shape = RoundedCornerShape(10.dp))
                ),
            scale = ClickableSurfaceScale.None,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.77f),
            ) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_heart),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacings.small))
        Text(
            text = stringResource(CoreR.string.title_favorite),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(device = "id:tv_1080p")
@Composable
private fun LibrariesScreenLayoutPreview() {
    FindroidTheme {
        LibrariesScreenLayout(
            state = MediaState(libraries = dummyCollections),
            onFavoritesClick = {},
            onAction = {},
        )
    }
}
