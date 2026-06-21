package com.coffeepeek.admin.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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

private data class PlacemarkEntry(
    val placemark: PlacemarkMapObject,
    var isSelected: Boolean,
    var latitude: Double,
    var longitude: Double,
)

@Composable
actual fun CoffeeMap(
    shops: List<MapShop>,
    selectedShopId: String?,
    onBoundsChanged: (MapBounds) -> Unit,
    onShopClick: (MapShop) -> Unit,
    modifier: Modifier,
    cameraTarget: Pair<Double, Double>?,
    cameraZoom: Float?,
    onCameraTargetApplied: () -> Unit,
    isDarkTheme: Boolean,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val onBoundsChangedState = rememberUpdatedState(onBoundsChanged)
    val onShopClickState = rememberUpdatedState(onShopClick)
    val onCameraTargetAppliedState = rememberUpdatedState(onCameraTargetApplied)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { /* MapKit handles absence gracefully after request */ }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    val mapView = remember { MapView(appContext) }
    val placemarks = remember { mutableMapOf<String, PlacemarkEntry>() }
    val defaultIcon = remember { MapMarkerIcons.provider(appContext, selected = false) }
    val selectedIcon = remember { MapMarkerIcons.provider(appContext, selected = true) }
    val iconStyle = remember {
        IconStyle()
            .setAnchor(MapMarkerIcons.anchor())
            .setScale(1f)
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val map = mapView.mapWindow.map
        val cameraListener = object : CameraListener {
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
        }
        map.addCameraListener(cameraListener)

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
            map.removeCameraListener(cameraListener)
            placemarks.values.forEach { entry ->
                map.mapObjects.remove(entry.placemark)
            }
            placemarks.clear()
            stopMap()
        }
    }

    LaunchedEffect(cameraTarget, cameraZoom, mapView) {
        val target = cameraTarget ?: return@LaunchedEffect
        val map = mapView.mapWindow.map
        map.move(
            CameraPosition(
                Point(target.first, target.second),
                cameraZoom ?: 16f,
                0f,
                0f,
            ),
            Animation(Animation.Type.SMOOTH, 0.45f),
            null,
        )
        onCameraTargetAppliedState.value()
    }

    LaunchedEffect(isDarkTheme, mapView) {
        mapView.mapWindow.map.isNightModeEnabled = isDarkTheme
    }

    LaunchedEffect(shops, selectedShopId, mapView) {
        val map = mapView.mapWindow.map
        syncPlacemarks(
            map = map,
            shops = shops,
            selectedShopId = selectedShopId,
            placemarks = placemarks,
            defaultIcon = defaultIcon,
            selectedIcon = selectedIcon,
            iconStyle = iconStyle,
            onShopClick = { shop -> onShopClickState.value(shop) },
        )
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                val initialTarget = cameraTarget
                    ?.let { Point(it.first, it.second) }
                    ?: Point(DEFAULT_LAT, DEFAULT_LON)
                mapWindow.map.move(
                    CameraPosition(
                        initialTarget,
                        cameraZoom ?: if (cameraTarget != null) 16f else DEFAULT_ZOOM,
                        0f,
                        0f,
                    ),
                    Animation(Animation.Type.SMOOTH, 0f),
                    null,
                )
            }
        },
    )
}

private fun syncPlacemarks(
    map: Map,
    shops: List<MapShop>,
    selectedShopId: String?,
    placemarks: MutableMap<String, PlacemarkEntry>,
    defaultIcon: com.yandex.runtime.image.ImageProvider,
    selectedIcon: com.yandex.runtime.image.ImageProvider,
    iconStyle: IconStyle,
    onShopClick: (MapShop) -> Unit,
) {
    val shopIds = shops.map { it.id }.toSet()
    placemarks.keys.filter { it !in shopIds }.toList().forEach { id ->
        placemarks.remove(id)?.let { entry ->
            map.mapObjects.remove(entry.placemark)
        }
    }

    shops.forEach { shop ->
        val point = Point(shop.latitude, shop.longitude)
        val isSelected = shop.id == selectedShopId
        val entry = placemarks.getOrPut(shop.id) {
            val placemark = map.mapObjects.addPlacemark(point).apply {
                setIcon(defaultIcon)
                setIconStyle(iconStyle)
                addTapListener { _, _ ->
                    onShopClick(shop)
                    true
                }
            }
            PlacemarkEntry(
                placemark = placemark,
                isSelected = isSelected,
                latitude = shop.latitude,
                longitude = shop.longitude,
            )
        }

        if (entry.latitude != shop.latitude || entry.longitude != shop.longitude) {
            entry.placemark.geometry = point
            entry.latitude = shop.latitude
            entry.longitude = shop.longitude
        }

        val targetZIndex = if (isSelected) 2f else 1f
        if (entry.placemark.zIndex != targetZIndex) {
            entry.placemark.zIndex = targetZIndex
        }

        if (entry.isSelected != isSelected) {
            entry.placemark.setIcon(if (isSelected) selectedIcon else defaultIcon)
            entry.isSelected = isSelected
        }
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
