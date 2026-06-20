package com.coffeepeek.admin.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop

private val DEFAULT_CENTER = LatLng(53.9045, 27.5615)
private const val DEFAULT_ZOOM = 12f

@Composable
actual fun CoffeeMap(
    shops: List<MapShop>,
    selectedShopId: String?,
    onBoundsChanged: (MapBounds) -> Unit,
    onShopClick: (MapShop) -> Unit,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_CENTER, DEFAULT_ZOOM)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
        ),
    ) {
        MapEffect(Unit) { map ->
            val notifyBounds = {
                val bounds = map.projection.visibleRegion.latLngBounds
                onBoundsChanged(
                    MapBounds(
                        minLat = bounds.southwest.latitude,
                        minLon = bounds.southwest.longitude,
                        maxLat = bounds.northeast.latitude,
                        maxLon = bounds.northeast.longitude,
                    )
                )
            }
            map.setOnCameraIdleListener { notifyBounds() }
            notifyBounds()
        }

        shops.forEach { shop ->
            val isSelected = shop.id == selectedShopId
            Marker(
                state = MarkerState(position = LatLng(shop.latitude, shop.longitude)),
                title = shop.title,
                icon = BitmapDescriptorFactory.defaultMarker(
                    if (isSelected) BitmapDescriptorFactory.HUE_ORANGE
                    else BitmapDescriptorFactory.HUE_RED,
                ),
                onClick = {
                    onShopClick(shop)
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLng(LatLng(shop.latitude, shop.longitude))
                    )
                    true
                },
            )
        }
    }
}
