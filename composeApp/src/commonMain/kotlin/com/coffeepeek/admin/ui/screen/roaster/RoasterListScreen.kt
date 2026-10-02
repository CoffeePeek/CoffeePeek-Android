package com.coffeepeek.admin.ui.screen.roaster

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.di.platformViewModel
import com.coffeepeek.admin.location.formatDistance
import com.coffeepeek.admin.location.rememberPermittedUserLocation
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.component.CoffeePeekPullToRefresh
import com.coffeepeek.admin.ui.component.CoffeeShopImage
import com.coffeepeek.admin.ui.component.LocalFloatingNavClearance
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.admin.ui.component.SearchHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoasterListScreen(onSelectShops: () -> Unit, vm: RoasterListViewModel = platformViewModel()) {
    val state by vm.state.collectAsState()
    val origin = rememberPermittedUserLocation()
    val listState = rememberLazyListState()
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val visible = state.visibleItems(origin)
    val clearance = LocalFloatingNavClearance.current
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing3)) {
                SearchHeader(
                    query = state.query, onQueryChange = vm::onQueryChange,
                    roastersSelected = true, onSelectRoasters = { if (!it) onSelectShops() },
                    filterCount = (if (state.nearbyOnly) 1 else 0) + (if (state.popularFirst) 1 else 0),
                    onFilters = { showFilters = true },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (origin != null) FilterChip(
                        selected = state.nearbyOnly, onClick = vm::toggleNearby,
                        label = { Text("Рядом") }, leadingIcon = { Icon(CpIcons.Distance, null, Modifier.size(18.dp)) },
                    )
                    FilterChip(
                        selected = state.popularFirst, onClick = vm::togglePopular,
                        label = { Text("Популярные") }, leadingIcon = { Icon(CpIcons.Star, null, Modifier.size(18.dp)) },
                    )
                }
            }
        },
    ) { padding ->
        CoffeePeekPullToRefresh(
            listState = listState, isRefreshing = state.isLoading,
            onRefresh = vm::refresh, modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
        ) { scrollModifier ->
            LazyColumn(
                state = listState, modifier = scrollModifier.fillMaxSize(),
                contentPadding = PaddingValues(start = CpDimens.spacing4, end = CpDimens.spacing4, bottom = clearance + CpDimens.spacing4),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
            ) {
                items(visible, key = { it.catalog.id }) { item ->
                    RoasterCard(item, formatDistance(item.distance(origin)))
                }
                if (state.isLoading) item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CoffeePeekLoader() }
                }
                if (state.error != null) item {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error.orEmpty())
                        TextButton(onClick = vm::refresh) { Text("Попробовать снова") }
                    }
                } else if (visible.isEmpty() && !state.isLoading) item {
                    Text("Обжарщики не найдены", modifier = Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    if (showFilters) ModalBottomSheet(onDismissRequest = { showFilters = false }) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(CpDimens.spacing4)) {
            Text("Фильтры обжарщиков", style = MaterialTheme.typography.titleLarge)
            if (origin != null) FilterChip(selected = state.nearbyOnly, onClick = vm::toggleNearby, label = { Text("Рядом") })
            FilterChip(selected = state.popularFirst, onClick = vm::togglePopular, label = { Text("Популярные — по числу кофеен") })
            TextButton(onClick = vm::resetFilters) { Text("Сбросить") }
            Button(onClick = { showFilters = false }, modifier = Modifier.fillMaxWidth()) { Text("Показать результаты") }
        }
    }
}

@Composable
private fun RoasterCard(item: RoasterListItem, distance: String?) {
    Card(
        onClick = { item.routeId?.let { Navigator.navigate(Navigator.Screen.RoasterDetail(it)) } },
        enabled = item.routeId != null,
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(CpDimens.radiusXl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(CpDimens.spacing3), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    val photo = item.catalog.photoUrl ?: item.details?.photos?.firstOrNull()?.fullUrl
                    if (!photo.isNullOrBlank()) CoffeeShopImage(
                        imageUrl = photo, contentDescription = item.catalog.name,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
                    ) else Icon(CpIcons.Factory, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.catalog.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    item.details?.location?.address?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    distance?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(CpIcons.Coffee, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    item.details?.let { roasterShopCountLabel(it.shops.size) } ?: "Подробнее об обжарщике",
                    modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(CpIcons.ChevronRight, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

internal fun roasterShopCountLabel(count: Int): String {
    val noun = when {
        count % 100 in 11..14 -> "кофеен используют"
        count % 10 == 1 -> "кофейня использует"
        count % 10 in 2..4 -> "кофейни используют"
        else -> "кофеен используют"
    }
    return "$count $noun это зерно"
}
