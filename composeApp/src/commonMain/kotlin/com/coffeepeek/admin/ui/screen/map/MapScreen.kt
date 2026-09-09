package com.coffeepeek.admin.ui.screen.map

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.coffeepeek.admin.map.CoffeeMap
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeeShopImage
import com.coffeepeek.admin.ui.component.CoffeeShopPlaceholderImage
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.component.LocalFloatingNavClearance
import com.coffeepeek.admin.ui.model.COFFEE_FOCUS_OPTIONS
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.MapShop
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MapScreen(vm: MapViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val pendingFocus by Navigator.pendingMapFocus.collectAsState()
    val pendingFocusShop = pendingFocus?.let { focus ->
        MapShop(
            id = focus.shopId,
            title = focus.title,
            latitude = focus.latitude,
            longitude = focus.longitude,
        )
    }
    val mapShops = pendingFocusShop?.let { shop ->
        (state.shops + shop).distinctBy { it.id }
    } ?: state.shops
    val selectedShopId = state.selectedShop?.id ?: pendingFocus?.shopId
    val cameraTarget = state.cameraTarget
        ?: pendingFocus?.let { it.latitude to it.longitude }
    val cameraZoom = state.cameraZoom ?: if (pendingFocus != null) 16f else null
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val navClearance = LocalFloatingNavClearance.current

    LaunchedEffect(pendingFocus) {
        pendingFocus?.let { focus ->
            vm.focusOnShop(focus)
            Navigator.consumeMapFocus()
        }
    }

    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = vm::clearError,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Ошибка", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    err,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = vm::clearError) {
                    Text("Понятно", style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = RoundedCornerShape(CpDimens.radius2xl),
        )
    }

    Box(Modifier.fillMaxSize()) {
        CoffeeMap(
            shops = mapShops,
            selectedShopId = selectedShopId,
            onBoundsChanged = vm::onBoundsChanged,
            onShopClick = vm::onShopSelected,
            modifier = Modifier.fillMaxSize(),
            cameraTarget = cameraTarget,
            cameraZoom = cameraZoom,
            onCameraTargetApplied = vm::onCameraTargetApplied,
            isDarkTheme = isDarkTheme,
            myLocationRequestKey = state.myLocationRequest,
            onMyLocationFound = vm::onMyLocationApplied,
            onLocationPermissionDenied = vm::onLocationPermissionDenied,
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(CpDimens.spacing4),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            horizontalAlignment = Alignment.End,
        ) {
            MapControlButton(onClick = vm::toggleFilters) {
                BadgedBox(
                    badge = {
                        if (state.activeFilterCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                                Text(state.activeFilterCount.toString())
                            }
                        }
                    },
                ) {
                    Icon(
                        CpIcons.Filter,
                        contentDescription = "Фильтры",
                        tint = if (state.showFilters || state.activeFilterCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
            MapControlButton(onClick = vm::requestMyLocation) {
                Icon(CpIcons.MyLocation, contentDescription = "Моё местоположение")
            }
        }

        if (state.isLoading) {
            CoffeePeekLoader(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = CpDimens.spacing4),
                strokeWidth = 2.dp,
            )
        }

        if (state.showSearchArea) {
            Button(
                onClick = vm::searchCurrentArea,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = navClearance + if (state.selectedShop == null) 32.dp else 148.dp,
                    ),
                shape = RoundedCornerShape(CpDimens.radius2xl),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                ),
                contentPadding = PaddingValues(horizontal = CpDimens.spacing5, vertical = CpDimens.spacing3),
            ) {
                Text("Искать в этой области", style = MaterialTheme.typography.labelLarge)
            }
        }

        state.selectedShop?.let { shop ->
            MapShopBottomSheet(
                shop = shop,
                details = state.selectedShopDetails,
                isLoadingDetails = state.isLoadingShopDetails,
                onOpen = { Navigator.navigate(Navigator.Screen.ShopDetail(shop.id)) },
                onDismiss = vm::clearSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = CpDimens.spacing4,
                        end = CpDimens.spacing4,
                        bottom = navClearance + CpDimens.spacing4,
                    ),
            )
        }

        if (state.showFilters) {
            MapFiltersDialog(
                state = state,
                onDismiss = vm::dismissFilters,
                onQueryChange = vm::onQueryChange,
                onPrice = vm::setPriceRange,
                onCoffeeFocusChange = vm::setCoffeeFocus,
                onToggleCatalog = vm::toggleFilterCatalog,
                onClear = vm::clearFilters,
                onApply = vm::applyFilters,
            )
        }
    }
}

@Composable
private fun MapControlButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        onClick = onClick,
        content = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                content()
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MapFiltersDialog(
    state: MapUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onPrice: (Int?) -> Unit,
    onCoffeeFocusChange: (String?) -> Unit,
    onToggleCatalog: (String, String) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
) {
    val filters = state.filters
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing3),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .heightIn(max = 760.dp),
                shape = RoundedCornerShape(CpDimens.radius4xl),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 0.dp,
                shadowElevation = 12.dp,
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = CpDimens.spacing5, top = CpDimens.spacing3, end = CpDimens.spacing2, bottom = CpDimens.spacing3),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Фильтры",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = CpIcons.Close,
                                contentDescription = "Закрыть фильтры",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = CpDimens.spacing5, vertical = CpDimens.spacing4),
                        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing4),
                    ) {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = onQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Поиск кофейни…") },
                            leadingIcon = { Icon(CpIcons.Search, contentDescription = null) },
                            shape = RoundedCornerShape(CpDimens.inputRadius),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                        FilterSection("Цена") {
                            CompactPriceFilter(
                                selected = filters.priceRange,
                                onSelect = onPrice,
                            )
                        }
                        FilterSection("Формат точки") {
                            SingleSelectMenu(
                                placeholder = "Любой формат",
                                items = COFFEE_FOCUS_OPTIONS.map { it.id to it.label },
                                selectedId = filters.coffeeFocus,
                                onSelect = onCoffeeFocusChange,
                            )
                        }
                        if (state.roasters.isNotEmpty()) {
                            CatalogMultiSelectMenu("Обжарщики", state.roasters, filters.roasterIds) {
                                onToggleCatalog("roaster", it)
                            }
                        }
                        if (state.beans.isNotEmpty()) {
                            CatalogMultiSelectMenu("Зёрна", state.beans, filters.beanIds) {
                                onToggleCatalog("bean", it)
                            }
                        }
                        if (state.equipment.isNotEmpty()) {
                            CatalogMultiSelectMenu("Оборудование", state.equipment, filters.equipmentIds) {
                                onToggleCatalog("equipment", it)
                            }
                        }
                        if (state.brewMethods.isNotEmpty()) {
                            CatalogMultiSelectMenu("Заваривание", state.brewMethods, filters.brewMethodIds) {
                                onToggleCatalog("brew", it)
                            }
                        }
                        if (state.shopTags.isNotEmpty()) {
                            CatalogMultiSelectMenu("Особенности", state.shopTags, filters.tagIds) {
                                onToggleCatalog("tag", it)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CpDimens.spacing4),
                        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onClear) {
                            Text(
                                text = "Сбросить",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Button(
                            onClick = onApply,
                            shape = RoundedCornerShape(CpDimens.buttonRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            contentPadding = PaddingValues(horizontal = CpDimens.spacing5, vertical = CpDimens.spacing3),
                        ) {
                            Text("Готово", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        content()
    }
}

@Composable
private fun SingleSelectMenu(
    placeholder: String,
    items: List<Pair<String, String>>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val menuShape = RoundedCornerShape(CpDimens.selectRadius)
    val selectedLabel = items.firstOrNull { it.first == selectedId }?.second
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CpDimens.selectMinHeight)
                .onGloballyPositioned { coordinates ->
                    anchorWidth = with(density) { coordinates.size.width.toDp() }
                },
            shape = RoundedCornerShape(CpDimens.selectRadius),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            contentPadding = PaddingValues(horizontal = CpDimens.spacing4),
        ) {
            Text(
                text = selectedLabel ?: placeholder,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedId == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                CpIcons.ChevronDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(anchorWidth.coerceAtLeast(280.dp))
                .clip(menuShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = menuShape,
                ),
            shape = menuShape,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            val defaultSelected = selectedId == null
            DropdownMenuItem(
                text = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        color = if (defaultSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
                trailingIcon = if (defaultSelected) {
                    {
                        Icon(
                            CpIcons.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else null,
            )
            items.forEach { (id, label) ->
                val selected = id == selectedId
                DropdownMenuItem(
                    text = {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    },
                    trailingIcon = if (selected) {
                        {
                            Icon(
                                CpIcons.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else null,
                )
            }
        }
    }
}

@Composable
private fun CatalogMultiSelectMenu(
    title: String,
    items: List<CatalogItem>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val menuShape = RoundedCornerShape(CpDimens.selectRadius)
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CpDimens.selectMinHeight)
                .onGloballyPositioned { coordinates ->
                    anchorWidth = with(density) { coordinates.size.width.toDp() }
                },
            shape = RoundedCornerShape(CpDimens.selectRadius),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            contentPadding = PaddingValues(horizontal = CpDimens.spacing4),
        ) {
            Text(
                text = if (selectedIds.isEmpty()) title else "$title · ${selectedIds.size}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedIds.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                CpIcons.ChevronDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(anchorWidth.coerceAtLeast(280.dp))
                .heightIn(max = 320.dp)
                .clip(menuShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = menuShape,
                ),
            shape = menuShape,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            items.forEach { item ->
                val selected = item.id in selectedIds
                DropdownMenuItem(
                    text = {
                        Text(
                            item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = { onToggle(item.id) },
                    trailingIcon = if (selected) {
                        {
                            Icon(
                                CpIcons.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else null,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactPriceFilter(
    selected: Int?,
    onSelect: (Int?) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        listOf(null to "Любая", 1 to "до 8", 2 to "≈ 8", 3 to "> 8").forEach { (value, label) ->
            val isSelected = selected == value
            Surface(
                onClick = { onSelect(value) },
                shape = RoundedCornerShape(CpDimens.radiusLg),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = CpDimens.spacing4, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun MapShopBottomSheet(
    shop: MapShop,
    details: CoffeeShopDetails?,
    isLoadingDetails: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoUrl = details?.photos?.firstOrNull() ?: details?.shop?.photoUrl
    val rating = details?.shop?.rating
    val reviewCount = details?.shop?.reviewCount ?: 0
    val hours = details?.schedules?.let { formatMapHoursSummary(it) }
    val isOpen = details?.shop?.isOpen == true

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CpDimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(CpDimens.spacing3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(CpDimens.radiusMd))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    !photoUrl.isNullOrBlank() -> {
                        CoffeeShopImage(
                            imageUrl = photoUrl,
                            contentDescription = shop.title,
                            contentScale = ContentScale.Crop,
                            placeholderLabelSize = 7.sp,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    isLoadingDetails -> {
                        CoffeePeekLoader(
                            size = CpDimens.loaderButton,
                            strokeWidth = 2.dp,
                        )
                    }
                    else -> {
                        CoffeeShopPlaceholderImage(
                            labelSize = 7.sp,
                            contentDescription = "Фото ${shop.title} отсутствует",
                        )
                    }
                }
            }

            Spacer(Modifier.width(CpDimens.spacing3))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (rating != null && rating > 0) {
                        Icon(
                            CpIcons.StarFilled,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(rating),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (reviewCount > 0) {
                            Text(
                                text = " ($reviewCount)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Text(
                            text = if (reviewCount > 0) "$reviewCount отзывов" else "Нет отзывов",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    text = shop.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )

                when {
                    hours != null -> {
                        Text(
                            text = buildString {
                                append(if (isOpen) "Открыто" else "Закрыто")
                                append(" · ")
                                append(hours)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    isLoadingDetails -> {
                        Text(
                            text = "Загрузка…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = CpIcons.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
