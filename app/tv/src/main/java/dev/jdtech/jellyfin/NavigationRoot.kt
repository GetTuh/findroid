package dev.jdtech.jellyfin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.jdtech.jellyfin.models.CollectionType
import dev.jdtech.jellyfin.models.FindroidBoxSet
import dev.jdtech.jellyfin.models.FindroidCollection
import dev.jdtech.jellyfin.models.FindroidEpisode
import dev.jdtech.jellyfin.models.FindroidFolder
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.models.FindroidMovie
import dev.jdtech.jellyfin.models.FindroidSeason
import dev.jdtech.jellyfin.models.FindroidShow
import dev.jdtech.jellyfin.navigation.navigateOrReuse
import dev.jdtech.jellyfin.navigation.resumedAction
import dev.jdtech.jellyfin.presentation.film.CollectionScreen
import dev.jdtech.jellyfin.presentation.film.FavoritesScreen
import dev.jdtech.jellyfin.presentation.film.LibraryScreen
import dev.jdtech.jellyfin.presentation.film.PersonScreen
import dev.jdtech.jellyfin.presentation.film.SeasonScreen
import dev.jdtech.jellyfin.presentation.film.ShowScreen
import dev.jdtech.jellyfin.presentation.settings.SettingsScreen
import dev.jdtech.jellyfin.presentation.settings.SettingsSubScreen
import dev.jdtech.jellyfin.presentation.setup.addserver.AddServerScreen
import dev.jdtech.jellyfin.presentation.setup.login.LoginScreen
import dev.jdtech.jellyfin.presentation.setup.servers.ServersScreen
import dev.jdtech.jellyfin.presentation.setup.users.UsersScreen
import dev.jdtech.jellyfin.presentation.setup.welcome.WelcomeScreen
import dev.jdtech.jellyfin.ui.MainScreen
import dev.jdtech.jellyfin.ui.MovieScreen
import dev.jdtech.jellyfin.ui.PlayerScreen
import java.util.UUID
import org.jellyfin.sdk.model.api.BaseItemKind

@Composable
fun NavigationRoot(
    hasServers: Boolean,
    hasCurrentServer: Boolean,
    hasCurrentUser: Boolean,
) {
    val setupComplete = hasServers && hasCurrentServer && hasCurrentUser

    val setupStartRoute: NavKey =
        when {
            hasServers && hasCurrentServer -> UsersRoute
            hasServers -> ServersRoute
            else -> WelcomeRoute
        }

    val backStack = rememberNavBackStack(if (setupComplete) MainRoute else setupStartRoute)

    // The entry decorators hold the per-entry SavedState and ViewModel stores. They must be
    // hoisted here so the retained state survives the setup/main switch.
    val saveableStateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()
    val entryDecorators =
        remember(saveableStateHolderDecorator, viewModelStoreDecorator) {
            listOf(saveableStateHolderDecorator, viewModelStoreDecorator)
        }

    // Live setup condition: reset the stack when the app enters or leaves the setup flow. The
    // reset happens during composition (rather than in a LaunchedEffect, which only runs after
    // the first frame has been composed) so the first frame of the new mode already shows the
    // right screen.
    val appliedSetupMode = remember { AppliedSetupMode(setupComplete) }
    if (appliedSetupMode.setupComplete != setupComplete) {
        backStack.clear()
        backStack.add(if (setupComplete) MainRoute else setupStartRoute)
        appliedSetupMode.setupComplete = setupComplete
    }

    val navigate: (NavKey) -> Unit = backStack::add
    val navigateToMain: () -> Unit = {
        backStack.clear()
        backStack.add(MainRoute)
    }
    val navigateOrReuse: (NavKey) -> Unit = backStack::navigateOrReuse
    val navigateToItem: (FindroidItem) -> Unit = { item -> itemRoute(item)?.let(navigate) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = entryDecorators,
        entryProvider =
            entryProvider {
                tvEntries(
                    inMainMode = setupComplete,
                    navigate = navigate,
                    navigateToMain = navigateToMain,
                    navigateOrReuse = navigateOrReuse,
                    navigateToItem = navigateToItem,
                )
            },
    )
}

/**
 * Remembers the setup mode that the back stack was last reconciled for. Deliberately not observable
 * state: it is written during composition, which must not schedule recompositions.
 */
private class AppliedSetupMode(var setupComplete: Boolean)

private fun EntryProviderScope<NavKey>.tvEntries(
    inMainMode: Boolean,
    navigate: (NavKey) -> Unit,
    navigateToMain: () -> Unit,
    navigateOrReuse: (NavKey) -> Unit,
    navigateToItem: (FindroidItem) -> Unit,
) {
    entry<WelcomeRoute> {
        WelcomeScreen(onContinueClick = dropUnlessResumed { navigate(ServersRoute) })
    }
    entry<ServersRoute> {
        ServersScreen(
            navigateToUsers = dropUnlessResumed { navigate(UsersRoute) },
            onAddClick = dropUnlessResumed { navigate(AddServerRoute) },
        )
    }
    entry<AddServerRoute> {
        AddServerScreen(onSuccess = dropUnlessResumed { navigate(UsersRoute) })
    }
    entry<UsersRoute> {
        UsersScreen(
            navigateToHome =
                if (inMainMode) {
                    // Switching users does not change the setup condition: navigate to Main.
                    dropUnlessResumed { navigateToMain() }
                } else {
                    // Selecting a user completes the setup: the live condition switches to Main.
                    {}
                },
            onChangeServerClick = dropUnlessResumed { navigateOrReuse(ServersRoute) },
            onAddClick = dropUnlessResumed { navigate(LoginRoute()) },
            onPublicUserClick =
                resumedAction { username -> navigate(LoginRoute(username = username)) },
        )
    }
    entry<LoginRoute> { key ->
        LoginScreen(
            onSuccess =
                if (inMainMode) {
                    dropUnlessResumed { navigateToMain() }
                } else {
                    // A successful login completes the setup: the live condition switches to Main.
                    {}
                },
            onChangeServerClick = dropUnlessResumed { navigateOrReuse(ServersRoute) },
            prefilledUsername = key.username,
        )
    }
    entry<MainRoute> {
        MainScreen(
            navigateToSettings = dropUnlessResumed { navigate(SettingsRoute) },
            navigateToLibrary =
                resumedAction { libraryId, libraryName, libraryType ->
                    navigate(
                        LibraryRoute(
                            libraryId = libraryId.toString(),
                            libraryName = libraryName,
                            libraryType = libraryType,
                        )
                    )
                },
            navigateToFavorites = dropUnlessResumed { navigate(FavoritesRoute) },
            onItemClick = resumedAction(navigateToItem),
        )
    }
    entry<LibraryRoute> { key ->
        LibraryScreen(
            libraryId = UUID.fromString(key.libraryId),
            libraryName = key.libraryName,
            libraryType = key.libraryType,
            navigateToLibrary =
                resumedAction { libraryId, libraryName, libraryType ->
                    navigate(
                        LibraryRoute(
                            libraryId = libraryId.toString(),
                            libraryName = libraryName,
                            libraryType = libraryType,
                        )
                    )
                },
            onItemClick = resumedAction(navigateToItem),
        )
    }
    entry<CollectionRoute> { key ->
        CollectionScreen(
            collectionId = UUID.fromString(key.collectionId),
            collectionName = key.collectionName,
            onItemClick = resumedAction(navigateToItem),
        )
    }
    entry<FavoritesRoute> { FavoritesScreen(onItemClick = resumedAction(navigateToItem)) }
    entry<PersonRoute> { key ->
        PersonScreen(
            personId = UUID.fromString(key.personId),
            onItemClick = resumedAction(navigateToItem),
        )
    }
    entry<MovieRoute> { key ->
        MovieScreen(
            movieId = UUID.fromString(key.itemId),
            navigateToPlayer =
                resumedAction { itemId ->
                    navigate(
                        PlayerRoute(
                            itemId = itemId.toString(),
                            itemKind = BaseItemKind.MOVIE.serialName,
                        )
                    )
                },
            navigateToPerson =
                resumedAction { personId -> navigate(PersonRoute(personId.toString())) },
        )
    }
    entry<ShowRoute> { key ->
        ShowScreen(
            showId = UUID.fromString(key.itemId),
            navigateToItem = resumedAction(navigateToItem),
            navigateToPlayer =
                resumedAction { itemId ->
                    navigate(
                        PlayerRoute(
                            itemId = itemId.toString(),
                            itemKind = BaseItemKind.SERIES.serialName,
                        )
                    )
                },
            navigateToPerson =
                resumedAction { personId -> navigate(PersonRoute(personId.toString())) },
        )
    }
    entry<SeasonRoute> { key ->
        SeasonScreen(
            seasonId = UUID.fromString(key.seasonId),
            navigateToPlayer =
                resumedAction { itemId ->
                    navigate(
                        PlayerRoute(
                            itemId = itemId.toString(),
                            itemKind = BaseItemKind.SEASON.serialName,
                        )
                    )
                },
        )
    }
    entry<PlayerRoute> { key ->
        PlayerScreen(
            itemId = UUID.fromString(key.itemId),
            itemKind = key.itemKind,
            startFromBeginning = false,
        )
    }
    entry<SettingsRoute> {
        SettingsScreen(
            navigateToUsers = dropUnlessResumed { navigate(UsersRoute) },
            navigateToServers = dropUnlessResumed { navigate(ServersRoute) },
            navigateToSubSettings =
                resumedAction { indexes -> navigate(SettingsSubRoute(indexes = indexes)) },
        )
    }
    entry<SettingsSubRoute> { key ->
        SettingsSubScreen(
            indexes = key.indexes,
            navigateToUsers = dropUnlessResumed { navigate(UsersRoute) },
            navigateToServers = dropUnlessResumed { navigate(ServersRoute) },
            navigateToSubSettings =
                resumedAction { indexes -> navigate(SettingsSubRoute(indexes = indexes)) },
        )
    }
}

/** The route that opens [item], or null when the TV app has no screen for that kind of item. */
private fun itemRoute(item: FindroidItem): NavKey? =
    when (item) {
        is FindroidBoxSet ->
            CollectionRoute(collectionId = item.id.toString(), collectionName = item.name)
        is FindroidMovie -> MovieRoute(itemId = item.id.toString())
        is FindroidShow -> ShowRoute(itemId = item.id.toString())
        is FindroidSeason -> SeasonRoute(seasonId = item.id.toString())
        // There is no episode details screen on TV yet: play the episode directly
        is FindroidEpisode ->
            PlayerRoute(itemId = item.id.toString(), itemKind = BaseItemKind.EPISODE.serialName)
        is FindroidCollection ->
            LibraryRoute(
                libraryId = item.id.toString(),
                libraryName = item.name,
                libraryType = item.type,
            )
        is FindroidFolder ->
            LibraryRoute(
                libraryId = item.id.toString(),
                libraryName = item.name,
                libraryType = CollectionType.Folders,
            )
        else -> null
    }
