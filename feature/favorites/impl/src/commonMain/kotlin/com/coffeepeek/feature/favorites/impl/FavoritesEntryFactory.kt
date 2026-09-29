package com.coffeepeek.feature.favorites.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.coffeepeek.feature.favorites.api.FavoritesEntry
import com.coffeepeek.feature.favorites.api.FavoritesRoute
import com.coffeepeek.feature.favorites.domain.FavoritesRepository
import com.coffeepeek.feature.favorites.impl.ui.FavoritesScreen
import com.coffeepeek.feature.favorites.impl.ui.FavoritesViewModel

/** Manual construction: UI does not fetch anything from Koin or application globals. */
fun createFavoritesEntry(repository: FavoritesRepository): FavoritesEntry = object : FavoritesEntry {
    @Composable
    override fun Content(onOpenShop: (String) -> Unit, onBack: () -> Unit, distanceForShop: (String) -> String?) {
        val vm = viewModel { FavoritesViewModel(repository) }
        val state by vm.state.collectAsStateWithLifecycle()
        FavoritesScreen(state, vm::retry, vm::remove, onOpenShop, onBack, distanceForShop)
    }
}

/** Root owns back stack, shop routes and NavDisplay decorators; feature registers only its entry. */
fun EntryProviderScope<NavKey>.favoritesEntry(
    screen: FavoritesEntry,
    onOpenShop: (String) -> Unit,
    onBack: () -> Unit,
    distanceForShop: (String) -> String? = { null },
) {
    entry<FavoritesRoute> { screen.Content(onOpenShop, onBack, distanceForShop) }
}
