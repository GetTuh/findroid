package dev.jdtech.jellyfin.presentation.film

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jdtech.jellyfin.core.Constants
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.core.presentation.dummy.dummyMovies
import dev.jdtech.jellyfin.film.presentation.collection.CollectionState
import dev.jdtech.jellyfin.film.presentation.collection.CollectionViewModel
import dev.jdtech.jellyfin.models.CollectionSection
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.models.UiText
import dev.jdtech.jellyfin.presentation.film.components.ItemSection
import dev.jdtech.jellyfin.presentation.film.components.ItemSectionsLayout
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import java.util.UUID

@Composable
fun CollectionScreen(
    collectionId: UUID,
    collectionName: String,
    onItemClick: (FindroidItem) -> Unit,
    viewModel: CollectionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(true) { viewModel.loadItems(collectionId) }

    CollectionScreenLayout(
        title = collectionName,
        state = state,
        emptyText = null,
        onItemClick = onItemClick,
    )
}

/** Shared by the collection and favorites screens, which both produce a [CollectionState]. */
@Composable
fun CollectionScreenLayout(
    title: String,
    state: CollectionState,
    emptyText: String?,
    onItemClick: (FindroidItem) -> Unit,
) {
    ItemSectionsLayout(
        title = title,
        sections =
            state.sections.map { section ->
                ItemSection(
                    key = section.id,
                    title = section.name.asString(),
                    items = section.items,
                )
            },
        isLoading = state.isLoading,
        emptyText = emptyText,
        onItemClick = onItemClick,
    )
}

@Preview(device = "id:tv_1080p")
@Composable
private fun CollectionScreenLayoutPreview() {
    FindroidTheme {
        CollectionScreenLayout(
            title = "Marvel",
            state =
                CollectionState(
                    sections =
                        listOf(
                            CollectionSection(
                                Constants.FAVORITE_TYPE_MOVIES,
                                UiText.StringResource(CoreR.string.movies_label),
                                dummyMovies,
                            )
                        )
                ),
            emptyText = null,
            onItemClick = {},
        )
    }
}
