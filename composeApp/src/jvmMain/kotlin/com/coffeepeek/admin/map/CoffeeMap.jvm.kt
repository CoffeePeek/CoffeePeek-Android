package com.coffeepeek.admin.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop

@Composable
actual fun CoffeeMap(
    shops: List<MapShop>,
    selectedShopId: String?,
    onBoundsChanged: (MapBounds) -> Unit,
    onShopClick: (MapShop) -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Карта доступна в Android-версии",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
