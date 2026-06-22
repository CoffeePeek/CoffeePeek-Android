package com.coffeepeek.admin.ui.screen.favorites

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.domain.model.CoffeeShopDetails
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(vm: FavoritesViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранное") },
                navigationIcon = {
                    IconButton(onClick = { Navigator.popBack() }) {
                        Icon(CpIcons.Back, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CoffeePeekLoader()
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Ошибка", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            state.shops.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Пока нет избранных кофеен", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(CpDimens.spacing4),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
            ) {
                items(state.shops, key = { it.shop.id }) { details ->
                    FavoriteShopCard(
                        details = details,
                        onClick = { Navigator.navigate(Navigator.Screen.ShopDetail(details.shop.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteShopCard(details: CoffeeShopDetails, onClick: () -> Unit) {
    val shop = details.shop
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(CpDimens.spacing3)) {
            Text(
                text = shop.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            details.location?.address?.let { address ->
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Icon(CpIcons.Location, null, modifier = Modifier.padding(end = 4.dp).then(Modifier), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            shop.rating?.takeIf { it > 0 }?.let { rating ->
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Icon(CpIcons.StarFilled, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 2.dp).then(Modifier))
                    Text("%.1f".format(rating), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
