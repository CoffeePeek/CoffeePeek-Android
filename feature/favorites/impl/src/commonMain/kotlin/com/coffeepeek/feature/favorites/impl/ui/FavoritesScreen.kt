package com.coffeepeek.feature.favorites.impl.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.coffeepeek.core.designsystem.component.AppButton
import com.coffeepeek.core.designsystem.component.CoffeePeekLoader
import com.coffeepeek.core.designsystem.component.CpTopBar
import com.coffeepeek.core.designsystem.icons.CpIcons
import com.coffeepeek.feature.favorites.domain.FavoriteShop
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
internal fun FavoritesScreen(
    state: FavoritesUiState,
    onRetry: () -> Unit,
    onRemove: (String) -> Unit,
    onOpenShop: (String) -> Unit,
    onBack: () -> Unit,
    distanceForShop: (String) -> String? = { null },
) {
    Scaffold(topBar = { CpTopBar("Избранное", "Назад", onBack = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            when {
                state.isLoading && state.shops.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CoffeePeekLoader("Загрузка избранного")
                }
                state.loadFailed && state.shops.isEmpty() -> Message("Не удалось загрузить избранное", onRetry)
                state.shops.isEmpty() -> Message("Пока нет избранных кофеен")
                else -> {
                    if (state.actionFailed || state.loadFailed) {
                        Text("Не удалось обновить избранное", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                        AppButton("Повторить", onRetry, Modifier.padding(horizontal = 16.dp))
                    }
                    LazyColumn(
                        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.shops, key = { it.id }) { shop ->
                            FavoriteCard(shop, shop.id in state.removing, distanceForShop(shop.id),
                                { onOpenShop(shop.id) }, { onRemove(shop.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Message(text: String, onRetry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
            onRetry?.let { AppButton("Повторить", it) }
        }
    }
}

/** Feature-owned rendering: never imports feed implementation or legacy shop aggregates. */
@Composable
private fun FavoriteCard(shop: FavoriteShop, removing: Boolean, distance: String?, onOpen: () -> Unit, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpen),
        shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box(Modifier.fillMaxWidth().aspectRatio(2f), contentAlignment = Alignment.Center) {
            val photoUrl = shop.photoUrl
            if (!photoUrl.isNullOrBlank()) {
                KamelImage(resource = { asyncPainterResource(photoUrl) }, contentDescription = shop.title,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                    onFailure = { Text("Фото недоступно") })
            } else Text("Фото отсутствует", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(shop.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onRemove, enabled = !removing) {
                    Icon(CpIcons.FavoriteFilled, "Удалить из избранного: ${shop.title}")
                }
            }
            listOfNotNull(shop.cityName, shop.address, distance).takeIf { it.isNotEmpty() }?.let {
                Text(it.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            }
            shop.rating?.let { Text("Рейтинг: $it · Отзывов: ${shop.reviewCount}", style = MaterialTheme.typography.bodySmall) }
            Text(if (shop.isOpen) "Открыто" else "Закрыто", style = MaterialTheme.typography.labelSmall)
            shop.priceRange?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            (shop.tags + shop.brewMethods).takeIf { it.isNotEmpty() }?.let {
                Text(it.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
