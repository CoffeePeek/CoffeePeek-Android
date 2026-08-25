package com.coffeepeek.admin.ui.screen.feed

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.coffeepeek.admin.ui.component.CoffeePeekPullToRefresh
import com.coffeepeek.admin.ui.component.LocalFloatingNavClearance
import com.coffeepeek.admin.ui.component.PriceBeansRow
import com.coffeepeek.admin.ui.component.priceRangeLevel
import com.coffeepeek.admin.ui.model.COFFEE_FOCUS_OPTIONS
import androidx.compose.foundation.lazy.LazyColumn
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
            if (state.shops.isEmpty()) return@derivedStateOf false
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.shops.size - 5 &&
                state.hasMore &&
                !state.isLoadingMore &&
                !state.isLoading &&
                !state.isRefreshing
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
                    Spacer(modifier = Modifier.height(CpDimens.spacing2))
                    FeedQuickFilterBar(
                        cities = state.cities,
                        selectedCityId = state.filters.cityId,
                        onCitySelect = vm::setCity,
                        quickMode = state.filters.quickMode,
                        onQuickMode = vm::setQuickMode,
                        coffeeFocusId = state.filters.coffeeFocus,
                        onCoffeeFocusChange = { id ->
                            vm.setCoffeeFocus(
                                if (state.filters.coffeeFocus == id) null else id,
                            )
                        },
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.showFilters) {
            ShopFiltersScreen(
                state = state,
                onDismiss = vm::closeFilters,
                onApply = vm::applyFilters,
            )
        }
        val contentModifier = Modifier.fillMaxSize().padding(padding)
        val navClearance = LocalFloatingNavClearance.current
        val listContentPadding = PaddingValues(
            start = CpDimens.spacing4,
            top = CpDimens.spacing4,
            end = CpDimens.spacing4,
            bottom = CpDimens.spacing4 + navClearance,
        )

        when {
            state.isLoading && state.shops.isEmpty() -> {
                Box(contentModifier, contentAlignment = Alignment.Center) {
                    CoffeePeekLoader()
                }
            }
            state.error != null && state.shops.isEmpty() -> {
                CoffeePeekPullToRefresh(
                    listState = listState,
                    isRefreshing = state.isRefreshing,
                    onRefresh = vm::refresh,
                    modifier = contentModifier,
                ) { scrollModifier ->
                    LazyColumn(
                        state = listState,
                        modifier = scrollModifier.fillMaxSize(),
                        contentPadding = listContentPadding,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillParentMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
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
                }
            }
            state.visibleShops.isEmpty() && !state.isLoading && !state.isRefreshing -> {
                CoffeePeekPullToRefresh(
                    listState = listState,
                    isRefreshing = state.isRefreshing,
                    onRefresh = vm::refresh,
                    modifier = contentModifier,
                ) { scrollModifier ->
                    LazyColumn(
                        state = listState,
                        modifier = scrollModifier.fillMaxSize(),
                        contentPadding = listContentPadding,
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillParentMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
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
                }
            }
            else -> {
                CoffeePeekPullToRefresh(
                    listState = listState,
                    isRefreshing = state.isRefreshing,
                    onRefresh = vm::refresh,
                    modifier = contentModifier,
                ) { scrollModifier ->
                    LazyColumn(
                        state = listState,
                        modifier = scrollModifier.fillMaxSize(),
                        contentPadding = listContentPadding,
                        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
                    ) {
                        items(state.visibleShops, key = { it.id }) { shop ->
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
                priceRangeLevel(shop.priceRange)?.let { level ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 46.dp, end = CpDimens.spacing2)
                            .clip(RoundedCornerShape(CpDimens.radiusSm))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = CpDimens.spacing2, vertical = 4.dp),
                    ) {
                        PriceBeansRow(
                            level = level,
                            iconSize = 12.dp,
                            activeTint = Color.White,
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
private fun FeedQuickFilterBar(
    cities: List<City>,
    selectedCityId: String?,
    onCitySelect: (String?) -> Unit,
    quickMode: FeedQuickMode,
    onQuickMode: (FeedQuickMode) -> Unit,
    coffeeFocusId: String?,
    onCoffeeFocusChange: (String) -> Unit,
) {
    val selectedCity = cities.firstOrNull { it.id == selectedCityId }
        ?: cities.firstOrNull()
    var cityMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        Box {
            DesignFilterChip(
                label = selectedCity?.name ?: "Город",
                selected = false,
                onClick = { if (cities.isNotEmpty()) cityMenuOpen = true },
                leadingIcon = CpIcons.Location,
                trailingIcon = CpIcons.ChevronDown,
            )
            DropdownMenu(
                expanded = cityMenuOpen,
                onDismissRequest = { cityMenuOpen = false },
            ) {
                cities.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city.name) },
                        onClick = {
                            onCitySelect(if (selectedCityId == city.id) null else city.id)
                            cityMenuOpen = false
                        },
                        trailingIcon = if (selectedCityId == city.id) {
                            { Icon(CpIcons.Check, contentDescription = null, tint = CpColor.Primary) }
                        } else {
                            null
                        },
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .width(1.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        )

        DesignFilterChip(
            label = "Все",
            selected = quickMode == FeedQuickMode.ALL,
            onClick = { onQuickMode(FeedQuickMode.ALL) },
            leadingIcon = CpIcons.Grid,
        )
        DesignFilterChip(
            label = "Открыто",
            selected = quickMode == FeedQuickMode.OPEN,
            onClick = { onQuickMode(FeedQuickMode.OPEN) },
            leadingIcon = CpIcons.Time,
        )
        DesignFilterChip(
            label = "Новые",
            selected = quickMode == FeedQuickMode.NEW,
            onClick = { onQuickMode(FeedQuickMode.NEW) },
            leadingIcon = CpIcons.Sparkle,
        )
        DesignFilterChip(
            label = "Уже был",
            selected = quickMode == FeedQuickMode.VISITED,
            onClick = { onQuickMode(FeedQuickMode.VISITED) },
            leadingIcon = CpIcons.CheckCircle,
        )
        DesignFilterChip(
            label = "Избранное",
            selected = quickMode == FeedQuickMode.FAVORITES,
            onClick = { onQuickMode(FeedQuickMode.FAVORITES) },
            leadingIcon = CpIcons.Favorite,
        )

        COFFEE_FOCUS_OPTIONS.forEach { option ->
            DesignFilterChip(
                label = option.label,
                selected = coffeeFocusId == option.id,
                onClick = { onCoffeeFocusChange(option.id) },
            )
        }
    }
}

@Composable
private fun DesignFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(999.dp)
    val bg = if (selected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.surface
    }
    val fg = if (selected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderColor = if (selected) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    }

    Row(
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = CpColor.Primary,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            maxLines = 1,
        )
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = fg.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
