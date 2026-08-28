package com.coffeepeek.admin.map

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.animation.DecelerateInterpolator
import android.view.animation.PathInterpolator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
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
private const val Z_PIN = 0f
private const val Z_CLUSTER = 400f
private const val Z_PULSE = 999f
private const val Z_SELECTED = 1000f

private data class ShopMark(
    var placemark: PlacemarkMapObject,
    var isSelected: Boolean,
    var latitude: Double,
    var longitude: Double,
    var shop: MapShop,
    var type: String,
)

private data class ClusterMark(
    var placemark: PlacemarkMapObject,
    var latitude: Double,
    var longitude: Double,
    var count: Int,
    var shops: List<MapShop>,
)

private class MarkerAnimations {
    var pop: ValueAnimator? = null
    var pulse: ValueAnimator? = null
    var pulseMark: PlacemarkMapObject? = null

    fun cancel() {
        pop?.cancel()
        pop = null
        pulse?.cancel()
        pulse = null
    }
}

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
    myLocationRequestKey: Int,
    onMyLocationFound: (Double, Double) -> Unit,
    onLocationPermissionDenied: () -> Unit,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val onBoundsChangedState = rememberUpdatedState(onBoundsChanged)
    val onShopClickState = rememberUpdatedState(onShopClick)
    val onCameraTargetAppliedState = rememberUpdatedState(onCameraTargetApplied)
    val onMyLocationFoundState = rememberUpdatedState(onMyLocationFound)
    val onLocationPermissionDeniedState = rememberUpdatedState(onLocationPermissionDenied)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (!granted) {
            onLocationPermissionDeniedState.value()
        }
    }

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
    val shopMarks = remember { mutableMapOf<String, ShopMark>() }
    val clusterMarks = remember { mutableMapOf<String, ClusterMark>() }
    val animations = remember { MarkerAnimations() }
    val handler = remember { Handler(Looper.getMainLooper()) }
    var clusterGeneration by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner, mapView) {
        val map = mapView.mapWindow.map
        val recluster = Runnable { clusterGeneration += 1 }
        val cameraListener = object : CameraListener {
            override fun onCameraPositionChanged(
                map: Map,
                cameraPosition: CameraPosition,
                cameraUpdateReason: CameraUpdateReason,
                finished: Boolean,
            ) {
                handler.removeCallbacks(recluster)
                if (finished) {
                    clusterGeneration += 1
                    onBoundsChangedState.value(map.visibleRegion.toMapBounds())
                } else {
                    handler.postDelayed(recluster, CLUSTER_MOVE_DEBOUNCE_MS)
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
            handler.removeCallbacksAndMessages(null)
            animations.cancel()
            animations.pulseMark?.let { mark ->
                runCatching { map.mapObjects.remove(mark) }
            }
            animations.pulseMark = null
            lifecycleOwner.lifecycle.removeObserver(observer)
            map.removeCameraListener(cameraListener)
            shopMarks.values.forEach { entry ->
                runCatching { map.mapObjects.remove(entry.placemark) }
            }
            clusterMarks.values.forEach { entry ->
                runCatching { map.mapObjects.remove(entry.placemark) }
            }
            shopMarks.clear()
            clusterMarks.clear()
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

    LaunchedEffect(myLocationRequestKey, mapView) {
        if (myLocationRequestKey == 0) return@LaunchedEffect
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
            return@LaunchedEffect
        }

        context.lastKnownLocation()?.let { location ->
            val map = mapView.mapWindow.map
            map.move(
                CameraPosition(Point(location.latitude, location.longitude), 15f, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 0.45f),
                null,
            )
            onMyLocationFoundState.value(location.latitude, location.longitude)
        }
    }

    LaunchedEffect(shops, selectedShopId, clusterGeneration, mapView) {
        val map = mapView.mapWindow.map
        val mapWindow = mapView.mapWindow
        syncMapMarkers(
            context = appContext,
            map = map,
            shops = shops,
            selectedShopId = selectedShopId,
            zoom = map.cameraPosition.zoom,
            project = { shop ->
                mapWindow.worldToScreen(Point(shop.latitude, shop.longitude))
                    ?.let { ScreenXy(it.x, it.y) }
            },
            shopMarks = shopMarks,
            clusterMarks = clusterMarks,
            animations = animations,
            reduceMotion = appContext.prefersReducedMotion(),
            onShopClick = { shop -> onShopClickState.value(shop) },
            onClusterClick = { clusterShops ->
                zoomToCluster(map, mapView.width, mapView.height, clusterShops)
            },
        )
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                val location = context.lastKnownLocation()
                val initialTarget = cameraTarget?.let { Point(it.first, it.second) }
                    ?: location?.let { Point(it.latitude, it.longitude) }
                    ?: Point(DEFAULT_LAT, DEFAULT_LON)
                val initialZoom = cameraZoom ?: when {
                    cameraTarget != null -> 16f
                    location != null -> 15f
                    else -> DEFAULT_ZOOM
                }
                mapWindow.map.move(
                    CameraPosition(
                        initialTarget,
                        initialZoom,
                        0f,
                        0f,
                    ),
                    Animation(Animation.Type.SMOOTH, 0f),
                    null,
                )
                location?.let {
                    onMyLocationFoundState.value(it.latitude, it.longitude)
                }
            }
        },
    )
}

private fun Context.lastKnownLocation(): Location? {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    return providers
        .mapNotNull { provider ->
            runCatching {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.getLastKnownLocation(provider)
                } else {
                    null
                }
            }.getOrNull()
        }
        .maxByOrNull { it.time }
}

private fun Context.prefersReducedMotion(): Boolean {
    val duration = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    val transition = Settings.Global.getFloat(contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
    return duration == 0f || transition == 0f
}

private fun pinStyle(scale: Float = 1f): IconStyle =
    IconStyle()
        .setAnchor(MapMarkerIcons.anchor())
        .setScale(MapMarkerIcons.DISPLAY_SCALE * scale)

private fun syncMapMarkers(
    context: Context,
    map: Map,
    shops: List<MapShop>,
    selectedShopId: String?,
    zoom: Float,
    project: (MapShop) -> ScreenXy?,
    shopMarks: MutableMap<String, ShopMark>,
    clusterMarks: MutableMap<String, ClusterMark>,
    animations: MarkerAnimations,
    reduceMotion: Boolean,
    onShopClick: (MapShop) -> Unit,
    onClusterClick: (List<MapShop>) -> Unit,
) {
    val selected = shops.firstOrNull { it.id == selectedShopId }
    val clusterable = if (selected == null) shops else shops.filter { it.id != selected.id }
    val clustered = clusterMapShops(clusterable, zoom, project)
    val shopItems = buildList {
        clustered.filterIsInstance<MapMarkerItem.Shop>().forEach { add(it.shop) }
        if (selected != null) add(selected)
    }.distinctBy { it.id }
    val clusterItems = clustered.filterIsInstance<MapMarkerItem.Cluster>()

    val shopIds = shopItems.map { it.id }.toSet()
    shopMarks.keys.filter { it !in shopIds }.toList().forEach { id ->
        shopMarks.remove(id)?.let { entry -> map.mapObjects.remove(entry.placemark) }
    }
    val clusterKeys = clusterItems.map { it.key }.toSet()
    clusterMarks.keys.filter { it !in clusterKeys }.toList().forEach { key ->
        clusterMarks.remove(key)?.let { entry -> map.mapObjects.remove(entry.placemark) }
    }

    shopItems.forEach { shop ->
        val point = Point(shop.latitude, shop.longitude)
        val isSelected = shop.id == selectedShopId
        val visual = if (isSelected) MapPinVisual.Selected else MapPinVisual.Default
        val existing = shopMarks[shop.id]
        if (existing == null) {
            lateinit var entry: ShopMark
            val placemark = map.mapObjects.addPlacemark(point).apply {
                setIcon(MapMarkerIcons.pinProvider(context, shop.type, visual))
                setIconStyle(pinStyle(if (isSelected && reduceMotion) 1.08f else 1f))
                zIndex = if (isSelected) Z_SELECTED else Z_PIN
                isDraggable = false
                addTapListener { _, _ ->
                    onShopClick(entry.shop)
                    true
                }
            }
            entry = ShopMark(
                placemark = placemark,
                isSelected = isSelected,
                latitude = shop.latitude,
                longitude = shop.longitude,
                shop = shop,
                type = shop.type,
            )
            shopMarks[shop.id] = entry
            if (isSelected) {
                playSelectedAnimations(context, map, entry, animations, reduceMotion)
            }
            return@forEach
        }

        existing.shop = shop
        if (existing.latitude != shop.latitude || existing.longitude != shop.longitude) {
            existing.placemark.geometry = point
            existing.latitude = shop.latitude
            existing.longitude = shop.longitude
        }
        if (existing.isSelected != isSelected || existing.type != shop.type) {
            existing.placemark.setIcon(MapMarkerIcons.pinProvider(context, shop.type, visual))
            existing.placemark.zIndex = if (isSelected) Z_SELECTED else Z_PIN
            existing.isSelected = isSelected
            existing.type = shop.type
            if (isSelected) {
                playSelectedAnimations(context, map, existing, animations, reduceMotion)
            } else if (selectedShopId == null || shop.id != selectedShopId) {
                existing.placemark.setIconStyle(pinStyle(1f))
            }
        }
    }

    clusterItems.forEach { cluster ->
        val point = Point(cluster.latitude, cluster.longitude)
        val existing = clusterMarks[cluster.key]
        if (existing == null) {
            lateinit var entry: ClusterMark
            val placemark = map.mapObjects.addPlacemark(point).apply {
                setIcon(MapMarkerIcons.clusterProvider(context, cluster.count))
                setIconStyle(pinStyle(1f))
                zIndex = Z_CLUSTER
                isDraggable = false
                addTapListener { _, _ ->
                    onClusterClick(entry.shops)
                    true
                }
            }
            entry = ClusterMark(
                placemark = placemark,
                latitude = cluster.latitude,
                longitude = cluster.longitude,
                count = cluster.count,
                shops = cluster.shops,
            )
            clusterMarks[cluster.key] = entry
            return@forEach
        }
        existing.shops = cluster.shops
        if (existing.latitude != cluster.latitude || existing.longitude != cluster.longitude) {
            existing.placemark.geometry = point
            existing.latitude = cluster.latitude
            existing.longitude = cluster.longitude
        }
        if (existing.count != cluster.count) {
            existing.placemark.setIcon(MapMarkerIcons.clusterProvider(context, cluster.count))
            existing.count = cluster.count
        }
    }

    if (selected == null) {
        animations.cancel()
        animations.pulseMark?.let { map.mapObjects.remove(it) }
        animations.pulseMark = null
    }
}

private fun playSelectedAnimations(
    context: Context,
    map: Map,
    mark: ShopMark,
    animations: MarkerAnimations,
    reduceMotion: Boolean,
) {
    animations.cancel()
    animations.pulseMark?.let { runCatching { map.mapObjects.remove(it) } }
    animations.pulseMark = null

    if (reduceMotion) {
        mark.placemark.setIconStyle(pinStyle(1.08f))
        return
    }

    val pop = ValueAnimator.ofFloat(1f, 1.18f, 1.10f).apply {
        duration = 350L
        interpolator = PathInterpolator(0.34f, 1.4f, 0.64f, 1f)
        addUpdateListener { animator ->
            val scale = animator.animatedValue as Float
            mark.placemark.setIconStyle(pinStyle(scale))
        }
    }
    animations.pop = pop
    pop.start()

    val pulseMark = map.mapObjects.addPlacemark(Point(mark.latitude, mark.longitude)).apply {
        setIcon(MapMarkerIcons.pulseProvider(context, 0))
        setIconStyle(pinStyle(1f))
        zIndex = Z_PULSE
        isDraggable = false
    }
    animations.pulseMark = pulseMark
    val pulse = ValueAnimator.ofInt(0, MapMarkerIcons.PULSE_FRAMES).apply {
        duration = 1800L
        interpolator = DecelerateInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animator ->
            val frame = animator.animatedValue as Int
            pulseMark.setIcon(MapMarkerIcons.pulseProvider(context, frame))
            pulseMark.geometry = mark.placemark.geometry
        }
    }
    animations.pulse = pulse
    pulse.start()
}

private fun zoomToCluster(
    map: Map,
    viewWidth: Int,
    viewHeight: Int,
    shops: List<MapShop>,
) {
    if (shops.isEmpty()) return
    val minLat = shops.minOf { it.latitude }
    val maxLat = shops.maxOf { it.latitude }
    val minLon = shops.minOf { it.longitude }
    val maxLon = shops.maxOf { it.longitude }
    val currentZoom = map.cameraPosition.zoom
    val animation = Animation(Animation.Type.SMOOTH, CLUSTER_TAP_ANIMATION_MS / 1000f)

    if (minLat == maxLat && minLon == maxLon) {
        map.move(
            CameraPosition(Point(minLat, minLon), clusterFitZoom(currentZoom, currentZoom + 2f), 0f, 0f),
            animation,
            null,
        )
        return
    }

    val box = BoundingBox(Point(minLat, minLon), Point(maxLat, maxLon))
    val geometry = Geometry.fromBoundingBox(box)
    val insetX = (viewWidth.coerceAtLeast(1) * clusterPaddingFraction() / 2f)
    val insetY = (viewHeight.coerceAtLeast(1) * clusterPaddingFraction() / 2f)
    val fitted = runCatching {
        if (viewWidth > 0 && viewHeight > 0) {
            map.cameraPosition(
                geometry,
                0f,
                0f,
                ScreenRect(
                    ScreenPoint(insetX, insetY),
                    ScreenPoint(viewWidth - insetX, viewHeight - insetY),
                ),
            )
        } else {
            map.cameraPosition(geometry)
        }
    }.getOrElse { map.cameraPosition(geometry) }

    map.move(
        CameraPosition(
            fitted.target,
            clusterFitZoom(currentZoom, fitted.zoom),
            0f,
            0f,
        ),
        animation,
        null,
    )
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
