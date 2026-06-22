package com.coffeepeek.admin.ui.screen.feed

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeeShopPlaceholderImage
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import androidx.compose.foundation.lazy.LazyColumn
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.City
import com.coffeepeek.domain.model.CoffeeShop
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(vm: FeedViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.shops.size - 5 && state.hasMore && !state.isLoadingMore
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) vm.loadMore()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = CpDimens.spacing4)
                        .padding(top = CpDimens.spacing3, bottom = CpDimens.spacing2),
                ) {
                    Text(
                        text = "Кофейни",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(CpDimens.spacing2))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                    ) {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = vm::onQueryChange,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(CpDimens.inputRadius),
                            placeholder = {
                                Text(
                                    "Поиск кофейни…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    CpIcons.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge,
                        )
                        BadgedBox(
                            badge = {
                                if (state.activeFilterCount > 0) {
                                    Badge { Text(state.activeFilterCount.toString()) }
                                }
                            },
                        ) {
                            IconButton(onClick = vm::toggleFilters) {
                                Icon(
                                    CpIcons.Filter,
                                    contentDescription = "Фильтры",
                                    tint = if (state.showFilters || state.activeFilterCount > 0) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                    AnimatedVisibility(visible = state.showFilters) {
                        FeedFiltersPanel(
                            state = state,
                            onCity = vm::setCity,
                            onPrice = vm::setPriceRange,
                            onRating = vm::setMinRating,
                            onToggleCatalog = vm::toggleFilterCatalog,
                            onClear = vm::clearFilters,
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { Navigator.navigate(Navigator.Screen.Map) },
                icon = {
                    Icon(
                        imageVector = CpIcons.Map,
                        contentDescription = null,
                    )
                },
                text = {
                    Text("Карта", style = MaterialTheme.typography.labelLarge)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(CpDimens.radius2xl),
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading && state.shops.isEmpty(),
            onRefresh = vm::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                state.isLoading && state.shops.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CoffeePeekLoader()
                    }
                }
                state.error != null && state.shops.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                state.error ?: "Ошибка загрузки",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(CpDimens.spacing3))
                            Button(
                                onClick = vm::refresh,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) { Text("Попробовать снова") }
                        }
                    }
                }
                state.shops.isEmpty() && !state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Ничего не найдено",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (state.activeFilterCount > 0) {
                                Spacer(Modifier.height(CpDimens.spacing2))
                                TextButton(onClick = vm::clearFilters) {
                                    Text("Сбросить фильтры")
                                }
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(CpDimens.spacing4),
                        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
                    ) {
                        items(state.shops, key = { it.id }) { shop ->
                            ShopCard(
                                shop = shop,
                                onClick = { Navigator.navigate(Navigator.Screen.ShopDetail(shop.id)) },
                                onToggleFavorite = { vm.toggleFavorite(shop) },
                            )
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(CpDimens.spacing4),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CoffeePeekLoader(
                                        size = CpDimens.loaderButton,
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopCard(
    shop: CoffeeShop,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(CpDimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            // ── Фото ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.45f)
                    .clip(RoundedCornerShape(topStart = CpDimens.cardRadius, topEnd = CpDimens.cardRadius))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                val photoUrl = shop.photoUrl
                if (!photoUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(photoUrl),
                        contentDescription = shop.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    CoffeeShopPlaceholderImage(
                        labelSize = 18.sp,
                        contentDescription = "Фото ${shop.title} отсутствует",
                    )
                }
                Row(
                    modifier = Modifier.align(Alignment.TopStart).padding(CpDimens.spacing2),
                    horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
                ) {
                    if (shop.isOpen) StatusBadge("Открыто", CpColor.Success)
                }
                FavoriteIconBadge(
                    isFavorite = shop.isFavorite,
                    onClick = onToggleFavorite,
                    modifier = Modifier.align(Alignment.TopEnd).padding(CpDimens.spacing2),
                )
                val priceRange = shop.priceRange
                if (!priceRange.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 46.dp, end = CpDimens.spacing2)
                            .clip(RoundedCornerShape(CpDimens.radiusSm))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = CpDimens.spacing2, vertical = 4.dp),
                    ) {
                        Text(
                            text = priceRange,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                        )
                    }
                }
            }

            // ── Инфо ─────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(CpDimens.spacing3)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = shop.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    val rating = shop.rating
                    if (rating != null && rating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                CpIcons.StarFilled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "%.1f".format(rating),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (shop.reviewCount > 0) {
                                Text(
                                    text = "(${shop.reviewCount})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                val address = shop.address
                if (!address.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            CpIcons.Location,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = address,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }

                if (shop.tags.isNotEmpty()) {
                    Spacer(Modifier.height(CpDimens.spacing2))
                    Row(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1)) {
                        shop.tags.take(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(CpDimens.radiusSm))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = CpDimens.spacing2, vertical = 3.dp),
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteIconBadge(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.92f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFavorite) CpIcons.FavoriteFilled else CpIcons.Favorite,
            contentDescription = if (isFavorite) "Убрать из избранного" else "Добавить в избранное",
            tint = if (isFavorite) CpColor.Error else Color.Black,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CpDimens.radiusSm))
            .background(color.copy(alpha = 0.9f))
            .padding(horizontal = CpDimens.spacing2, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FeedFiltersPanel(
    state: FeedUiState,
    onCity: (String?) -> Unit,
    onPrice: (Int?) -> Unit,
    onRating: (Double?) -> Unit,
    onToggleCatalog: (String, String) -> Unit,
    onClear: () -> Unit,
) {
    val filters = state.filters
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = CpDimens.spacing2),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Фильтры", style = MaterialTheme.typography.labelLarge)
            if (state.activeFilterCount > 0) {
                TextButton(onClick = onClear) { Text("Сбросить") }
            }
        }
        if (state.cities.isNotEmpty()) {
            FilterSection("Город") {
                CityChips(state.cities, filters.cityId, onCity)
            }
        }
        FilterSection("Цена") {
            CatalogChips(
                items = listOf("₽" to 1, "₽₽" to 2, "₽₽₽" to 3, "₽₽₽₽" to 4),
                selectedIds = filters.priceRange?.toString()?.let { setOf(it) } ?: emptySet(),
                onToggle = { id ->
                    val value = id.toIntOrNull()
                    onPrice(if (filters.priceRange == value) null else value)
                },
                idSelector = { it.second.toString() },
                labelSelector = { it.first },
            )
        }
        FilterSection("Рейтинг от") {
            CatalogChips(
                items = listOf("3.0+" to 3.0, "4.0+" to 4.0, "4.5+" to 4.5),
                selectedIds = filters.minRating?.toString()?.let { setOf(it) } ?: emptySet(),
                onToggle = { id ->
                    val value = id.toDoubleOrNull()
                    onRating(if (filters.minRating == value) null else value)
                },
                idSelector = { it.second.toString() },
                labelSelector = { it.first },
            )
        }
        if (state.roasters.isNotEmpty()) {
            FilterSection("Обжарщики") {
                CatalogFilterChips(state.roasters, filters.roasterIds, "roaster", onToggleCatalog)
            }
        }
        if (state.beans.isNotEmpty()) {
            FilterSection("Зёрна") {
                CatalogFilterChips(state.beans, filters.beanIds, "bean", onToggleCatalog)
            }
        }
        if (state.equipment.isNotEmpty()) {
            FilterSection("Оборудование") {
                CatalogFilterChips(state.equipment, filters.equipmentIds, "equipment", onToggleCatalog)
            }
        }
        if (state.brewMethods.isNotEmpty()) {
            FilterSection("Заваривание") {
                CatalogFilterChips(state.brewMethods, filters.brewMethodIds, "brew", onToggleCatalog)
            }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun CityChips(cities: List<City>, selectedId: String?, onSelect: (String?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        cities.forEach { city ->
            FilterChip(
                selected = selectedId == city.id,
                onClick = { onSelect(if (selectedId == city.id) null else city.id) },
                label = { Text(city.name, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}

@Composable
private fun CatalogFilterChips(
    items: List<CatalogItem>,
    selectedIds: Set<String>,
    type: String,
    onToggle: (String, String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        items.forEach { item ->
            FilterChip(
                selected = item.id in selectedIds,
                onClick = { onToggle(type, item.id) },
                label = { Text(item.name, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}

@Composable
private fun <T> CatalogChips(
    items: List<T>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    idSelector: (T) -> String,
    labelSelector: (T) -> String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        items.forEach { item ->
            val id = idSelector(item)
            FilterChip(
                selected = id in selectedIds,
                onClick = { onToggle(id) },
                label = { Text(labelSelector(item), style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}
