package com.coffeepeek.feature.favorites.impl

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.test.core.app.ActivityScenario
import com.coffeepeek.core.designsystem.theme.CoffeePeekTheme
import com.coffeepeek.feature.favorites.api.FavoritesRoute
import com.coffeepeek.feature.favorites.domain.FavoriteShop
import com.coffeepeek.feature.favorites.domain.FavoritesRepository
import com.coffeepeek.feature.favorites.impl.ui.FavoritesScreen
import com.coffeepeek.feature.favorites.impl.ui.FavoritesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FavoritesTestActivity : ComponentActivity()

class FavoritesScreenTest {
    @get:Rule val compose = createEmptyComposeRule()
    private fun render(content: @Composable () -> Unit) =
        ActivityScenario.launch(FavoritesTestActivity::class.java).also { scenario ->
            scenario.onActivity { activity -> activity.setContent { CoffeePeekTheme { content() } } }
        }

    @Test fun loadingAndEmptyAreDistinctAndHaveNoNetworkDependencies() {
        val state = mutableStateOf(FavoritesUiState())
        render { FavoritesScreen(state.value, {}, {}, {}, {}) }.use {
            compose.onNodeWithContentDescription("Загрузка избранного").assertIsDisplayed()
            compose.runOnIdle { state.value = FavoritesUiState(isLoading = false) }
            compose.onNodeWithText("Пока нет избранных кофеен").assertIsDisplayed()
            compose.onNodeWithContentDescription("Загрузка избранного").assertDoesNotExist()
        }
    }

    @Test fun errorRetryAndBackEmitCallerActions() {
        var retries = 0; var backs = 0
        render { FavoritesScreen(FavoritesUiState(isLoading = false, loadFailed = true),
            { retries++ }, {}, {}, { backs++ }) }.use {
            compose.onNodeWithText("Не удалось загрузить избранное").assertIsDisplayed()
            compose.onNodeWithText("Повторить").performClick()
            compose.onNodeWithContentDescription("Назад").performClick()
            compose.runOnIdle { assertEquals(1, retries); assertEquals(1, backs) }
        }
    }

    @Test fun removeIsSeparateFromCardNavigationAndPendingDisablesIt() {
        var removed: String? = null; var opened: String? = null
        val state = mutableStateOf(FavoritesUiState(listOf(FavoriteShop("id", "Кофейня")), isLoading = false))
        render { FavoritesScreen(state.value, {}, { removed = it }, { opened = it }, {}) }.use {
            compose.onNodeWithContentDescription("Удалить из избранного: Кофейня").performClick()
            compose.runOnIdle { assertEquals("id", removed); assertEquals(null, opened) }
            compose.onNodeWithText("Кофейня").performClick()
            compose.runOnIdle { assertEquals("id", opened); state.value = state.value.copy(removing = setOf("id")) }
            compose.onNodeWithContentDescription("Удалить из избранного: Кофейня").assertIsNotEnabled()
        }
    }

    @Test fun distanceUsesSavedCoordinatesAndSkipsIncompleteLocations() {
        val requested = mutableListOf<Pair<Double, Double>>()
        val shops = listOf(
            FavoriteShop("located", "Кофейня рядом", latitude = 53.9, longitude = 27.56),
            FavoriteShop("missing", "Кофейня без координат", latitude = 53.9),
        )
        render {
            FavoritesScreen(
                state = FavoritesUiState(shops = shops, isLoading = false),
                onRetry = {}, onRemove = {}, onOpenShop = {}, onBack = {},
                distanceForCoordinates = { latitude, longitude ->
                    requested += latitude to longitude
                    "950 м"
                },
            )
        }.use {
            compose.onNodeWithText("950 м", substring = true).assertIsDisplayed()
            compose.runOnIdle { assertEquals(setOf(53.9 to 27.56), requested.toSet()) }
        }
    }

    private data class ShopKey(val id: String) : NavKey

    @Test fun navigation3EntryConstructsManualVmAndDelegatesShopRouteToHost() {
        val values = MutableStateFlow(Result.success(listOf(FavoriteShop("id", "Кофейня"))))
        val repo = object : FavoritesRepository {
            override fun observe() = values
            override suspend fun read() = values.value
            override suspend fun save(shop: FavoriteShop) = Result.success(Unit)
            override suspend fun clear() = Result.success(Unit)
            override suspend fun remove(shopId: String) = Result.success(Unit)
        }
        val screen = createFavoritesEntry(repo)
        val stack = mutableStateListOf<NavKey>(FavoritesRoute)
        render {
            NavDisplay(backStack = stack, onBack = { if (stack.size > 1) stack.removeLast() },
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
                entryProvider = entryProvider {
                    favoritesEntry(screen, { stack.add(ShopKey(it)) }, {})
                    entry<ShopKey> { Text("Shop ${it.id}") }
                })
        }.use {
            compose.onNodeWithText("Кофейня").assertIsDisplayed().performClick()
            compose.onNodeWithText("Shop id").assertIsDisplayed()
            compose.runOnIdle { assertEquals(ShopKey("id"), stack.last()) }
            compose.runOnIdle { stack.removeLast() }
            compose.onNodeWithText("Кофейня").assertIsDisplayed()
        }
    }
}
