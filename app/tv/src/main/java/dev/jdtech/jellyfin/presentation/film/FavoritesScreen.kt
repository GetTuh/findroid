package dev.jdtech.jellyfin.presentation.film

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.film.presentation.favorites.FavoritesViewModel
import dev.jdtech.jellyfin.models.FindroidItem

@Composable
fun FavoritesScreen(
    onItemClick: (FindroidItem) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Reload on every visit so items (un)favorited on a detail screen are reflected
    LaunchedEffect(true) { viewModel.loadItems() }

    CollectionScreenLayout(
        title = stringResource(CoreR.string.title_favorite),
        state = state,
        emptyText = stringResource(CoreR.string.no_favorites),
        onItemClick = onItemClick,
    )
}
