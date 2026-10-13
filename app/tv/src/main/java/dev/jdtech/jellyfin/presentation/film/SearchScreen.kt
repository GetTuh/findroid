package dev.jdtech.jellyfin.presentation.film

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.core.presentation.dummy.dummyMovies
import dev.jdtech.jellyfin.film.presentation.search.SearchAction
import dev.jdtech.jellyfin.film.presentation.search.SearchState
import dev.jdtech.jellyfin.film.presentation.search.SearchViewModel
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.spacings
import dev.jdtech.jellyfin.ui.components.Direction
import dev.jdtech.jellyfin.ui.components.ItemCard
import dev.jdtech.jellyfin.ui.components.LoadingIndicator
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    onItemClick: (FindroidItem) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    SearchScreenLayout(
        state = state,
        onAction = { action ->
            when (action) {
                is SearchAction.OnItemClick -> onItemClick(action.item)
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@Composable
private fun SearchScreenLayout(state: SearchState, onAction: (SearchAction) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by rememberSaveable { mutableStateOf("") }

    // Debounce typing so every keystroke on the on-screen keyboard does not hit the server
    LaunchedEffect(query) {
        delay(400)
        onAction(SearchAction.Search(query))
    }

    LaunchedEffect(true) { focusRequester.requestFocus() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.default),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.default),
        contentPadding =
            PaddingValues(
                horizontal = MaterialTheme.spacings.default * 2,
                vertical = MaterialTheme.spacings.large,
            ),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(this.maxLineSpan) }) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_search),
                            contentDescription = null,
                        )
                    },
                    trailingIcon = { if (state.loading) LoadingIndicator() },
                    placeholder = { Text(text = stringResource(CoreR.string.search_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions =
                        KeyboardActions(
                            onSearch = {
                                onAction(SearchAction.Search(query))
                                keyboardController?.hide()
                            }
                        ),
                    modifier =
                        Modifier.widthIn(max = 640.dp)
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                )
            }
        }
        if (query.isNotBlank() && !state.loading && state.items.isEmpty()) {
            item(span = { GridItemSpan(this.maxLineSpan) }) {
                Text(
                    text = stringResource(CoreR.string.no_search_results),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = MaterialTheme.spacings.large),
                )
            }
        }
        items(state.items, key = { it.id }) { item ->
            ItemCard(
                item = item,
                direction = Direction.VERTICAL,
                onClick = { onAction(SearchAction.OnItemClick(item)) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Preview(device = "id:tv_1080p")
@Composable
private fun SearchScreenLayoutPreview() {
    FindroidTheme { SearchScreenLayout(state = SearchState(items = dummyMovies), onAction = {}) }
}
