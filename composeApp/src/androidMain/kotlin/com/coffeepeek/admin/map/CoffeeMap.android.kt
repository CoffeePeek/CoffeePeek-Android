package com.coffeepeek.admin.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.mapview.MapView

private const val DEFAULT_LAT = 53.9045
private const val DEFAULT_LON = 27.5615
private const val DEFAULT_ZOOM = 12f
private const val MAP_INITIALIZED_TAG = "map_initialized"

@Composable
actual fun CoffeeMap(
    shops: List<MapShop>,
    selectedShopId: String?,
    onBoundsChanged: (MapBounds) -> Unit,
    onShopClick: (MapShop) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onBoundsChangedState = rememberUpdatedState(onBoundsChanged)
    val onShopClickState = rememberUpdatedState(onShopClick)

    val mapView = remember { MapView(context) }
    val placemarks = remember { mutableMapOf<String, PlacemarkMapObject>() }

    DisposableEffect(lifecycleOwner, mapView) {
        val startMap = {
            MapKitFactory.getInstance().onStart()
            mapView.onStart()
        }
        val stopMap = {
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> startMap()
                Lifecycle.Event.ON_STOP -> stopMap()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            startMap()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopMap()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                mapWindow.map.move(
                    CameraPosition(Point(DEFAULT_LAT, DEFAULT_LON), DEFAULT_ZOOM, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 0f),
                    null,
                )
            }
        },
        update = { view ->
            val map = view.mapWindow.map

            if (view.getTag(MAP_INITIALIZED_TAG.tagId()) != true) {
                map.addCameraListener(object : CameraListener {
                    override fun onCameraPositionChanged(
                        map: Map,
                        cameraPosition: CameraPosition,
                        cameraUpdateReason: CameraUpdateReason,
                        finished: Boolean,
                    ) {
                        if (finished) {
                            onBoundsChangedState.value(map.visibleRegion.toMapBounds())
                        }
                    }
                })
                view.setTag(MAP_INITIALIZED_TAG.tagId(), true)
                onBoundsChangedState.value(map.visibleRegion.toMapBounds())
            }

            syncPlacemarks(
                map = map,
                shops = shops,
                selectedShopId = selectedShopId,
                placemarks = placemarks,
                onShopClick = { shop ->
                    onShopClickState.value(shop)
                    map.move(
                        CameraPosition(
                            Point(shop.latitude, shop.longitude),
                            map.cameraPosition.zoom,
                            0f,
                            0f,
                        ),
                        Animation(Animation.Type.SMOOTH, 0.3f),
                        null,
                    )
                },
            )
        },
    )
}

private fun syncPlacemarks(
    map: Map,
    shops: List<MapShop>,
    selectedShopId: String?,
    placemarks: MutableMap<String, PlacemarkMapObject>,
    onShopClick: (MapShop) -> Unit,
) {
    val shopIds = shops.map { it.id }.toSet()
    placemarks.keys.filter { it !in shopIds }.toList().forEach { id ->
        placemarks.remove(id)?.let { map.mapObjects.remove(it) }
    }

    shops.forEach { shop ->
        val point = Point(shop.latitude, shop.longitude)
        val placemark = placemarks.getOrPut(shop.id) {
            map.mapObjects.addPlacemark(point).apply {
                addTapListener { _, _ ->
                    onShopClick(shop)
                    true
                }
            }
        }
        placemark.geometry = point
        placemark.zIndex = if (shop.id == selectedShopId) 2f else 0f
        placemark.setIconStyle(
            IconStyle().setScale(if (shop.id == selectedShopId) 1.3f else 1f),
        )
    }
}

private fun VisibleRegion.toMapBounds(): MapBounds {
    val lats = listOf(
        topLeft.latitude,
        topRight.latitude,
        bottomLeft.latitude,
        bottomRight.latitude,
    )
    val lons = listOf(
        topLeft.longitude,
        topRight.longitude,
        bottomLeft.longitude,
        bottomRight.longitude,
    )
    return MapBounds(
        minLat = lats.min(),
        minLon = lons.min(),
        maxLat = lats.max(),
        maxLon = lons.max(),
    )
}

private fun String.tagId(): Int = hashCode()
