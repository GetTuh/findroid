package dev.jdtech.jellyfin.presentation.film.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.presentation.theme.spacings
import dev.jdtech.jellyfin.ui.components.Direction
import dev.jdtech.jellyfin.ui.components.ItemCard

/** A titled row of item posters, as used by the collection, favorites and person screens. */
data class ItemSection(val key: Any, val title: String, val items: List<FindroidItem>)

@Composable
fun ItemSectionsLayout(
    title: String,
    sections: List<ItemSection>,
    isLoading: Boolean,
    emptyText: String?,
    onItemClick: (FindroidItem) -> Unit,
    header: (@Composable () -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val itemsPadding = PaddingValues(horizontal = MaterialTheme.spacings.default * 2)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = MaterialTheme.spacings.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.large),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "title") {
                Column(modifier = Modifier.padding(itemsPadding)) {
                    Text(text = title, style = MaterialTheme.typography.displayMedium)
                    header?.let {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacings.medium))
                        it()
                    }
                }
            }
            items(sections, key = { it.key }) { section ->
                val isFirst = section == sections.first()
                Column {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(itemsPadding),
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacings.medium))
                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(MaterialTheme.spacings.default),
                        contentPadding = itemsPadding,
                        modifier =
                            if (isFirst) Modifier.focusRequester(focusRequester) else Modifier,
                    ) {
                        items(section.items, key = { it.id }) { item ->
                            ItemCard(
                                item = item,
                                direction = Direction.VERTICAL,
                                onClick = onItemClick,
                            )
                        }
                    }
                }
            }
            if (!isLoading && sections.isEmpty() && emptyText != null) {
                item(key = "empty") {
                    Text(
                        text = emptyText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(itemsPadding),
                    )
                }
            }
        }
        if (isLoading && sections.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

    LaunchedEffect(sections.isNotEmpty()) {
        if (sections.isNotEmpty()) {
            focusRequester.requestFocus()
        }
    }
}
