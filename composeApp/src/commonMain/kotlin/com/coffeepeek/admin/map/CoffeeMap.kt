package com.coffeepeek.admin.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop

@Composable
expect fun CoffeeMap(
    shops: List<MapShop>,
    selectedShopId: String?,
    onBoundsChanged: (MapBounds) -> Unit,
    onShopClick: (MapShop) -> Unit,
    modifier: Modifier = Modifier,
    cameraTarget: Pair<Double, Double>? = null,
    cameraZoom: Float? = null,
    onCameraTargetApplied: () -> Unit = {},
    isDarkTheme: Boolean = false,
    myLocationRequestKey: Int = 0,
    onMyLocationFound: (Double, Double) -> Unit = { _, _ -> },
)
