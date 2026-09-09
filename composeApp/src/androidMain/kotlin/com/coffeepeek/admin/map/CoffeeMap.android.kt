@file:Suppress("DEPRECATION")

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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.backgroundColor
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOutlineColor
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloWidth
import org.maplibre.android.style.layers.PropertyFactory.visibility
import org.maplibre.android.style.layers.SymbolLayer

private const val DEFAULT_LAT = 53.9045
private const val DEFAULT_LON = 27.5615
private const val DEFAULT_ZOOM = 12f
private const val LOCATION_ZOOM = 15f
private const val TARGET_ZOOM = 16f
private const val OSM_COPYRIGHT_URL = "https://www.openstreetmap.org/copyright"
private const val OPEN_FREE_MAP_LIGHT_STYLE = "https://tiles.openfreemap.org/styles/positron"
private const val OPEN_FREE_MAP_DARK_STYLE = "https://tiles.openfreemap.org/styles/dark"

private data class ShopMark(
    var marker: Marker,
    var isSelected: Boolean,
    var latitude: Double,
    var longitude: Double,
    var shop: MapShop,
    var type: String,
)

private data class ClusterMark(
    var marker: Marker,
    var latitude: Double,
    var longitude: Double,
    var count: Int,
    var shops: List<MapShop>,
)

private class MarkerAnimations {
    var pulse: ValueAnimator? = null
    var pulseMark: Marker? = null

    fun cancel() {
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
    val uriHandler = LocalUriHandler.current
    val onBoundsChangedState = rememberUpdatedState(onBoundsChanged)
    val onShopClickState = rememberUpdatedState(onShopClick)
    val onCameraTargetAppliedState = rememberUpdatedState(onCameraTargetApplied)
    val onMyLocationFoundState = rememberUpdatedState(onMyLocationFound)
    val onLocationPermissionDeniedState = rememberUpdatedState(onLocationPermissionDenied)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.values.none { it }) {
            onLocationPermissionDeniedState.value()
        }
    }

    LaunchedEffect(Unit) {
        if (!context.hasLocationPermission()) {
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    val mapView = remember {
        MapLibre.getInstance(appContext)
        MapView(appContext).apply { onCreate(null) }
    }
    val shopMarks = remember { mutableMapOf<String, ShopMark>() }
    val clusterMarks = remember { mutableMapOf<String, ClusterMark>() }
    val animations = remember { MarkerAnimations() }
    val handler = remember { Handler(Looper.getMainLooper()) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleGeneration by remember { mutableIntStateOf(0) }
    var clusterGeneration by remember { mutableIntStateOf(0) }
    var initialCameraApplied by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var currentLocationMarker by remember { mutableStateOf<Marker?>(null) }

    DisposableEffect(lifecycleOwner, mapView) {
        var started = false
        var resumed = false

        fun start() {
            if (!started) {
                mapView.onStart()
                started = true
            }
        }

        fun resume() {
            start()
            if (!resumed) {
                mapView.onResume()
                resumed = true
            }
        }

        fun pause() {
            if (resumed) {
                mapView.onPause()
                resumed = false
            }
        }

        fun stop() {
            pause()
            if (started) {
                mapView.onStop()
                started = false
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> start()
                Lifecycle.Event.ON_RESUME -> resume()
                Lifecycle.Event.ON_PAUSE -> pause()
                Lifecycle.Event.ON_STOP -> stop()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        when {
            lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) -> resume()
            lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) -> start()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            handler.removeCallbacksAndMessages(null)
            animations.cancel()
            stop()
            mapView.onDestroy()
        }
    }

    DisposableEffect(map) {
        val activeMap = map ?: return@DisposableEffect onDispose { }
        val recluster = Runnable { clusterGeneration += 1 }
        val moveListener = MapLibreMap.OnCameraMoveListener {
            handler.removeCallbacks(recluster)
            handler.postDelayed(recluster, CLUSTER_MOVE_DEBOUNCE_MS)
        }
        val idleListener = MapLibreMap.OnCameraIdleListener {
            handler.removeCallbacks(recluster)
            clusterGeneration += 1
            onBoundsChangedState.value(activeMap.projection.visibleRegion.latLngBounds.toMapBounds())
        }

        activeMap.addOnCameraMoveListener(moveListener)
        activeMap.addOnCameraIdleListener(idleListener)
        activeMap.setOnMarkerClickListener { marker ->
            val shop = shopMarks.values.firstOrNull { it.marker.id == marker.id }
            if (shop != null) {
                onShopClickState.value(shop.shop)
                true
            } else {
                val cluster = clusterMarks.values.firstOrNull { it.marker.id == marker.id }
                if (cluster != null) {
                    zoomToCluster(activeMap, mapView.width, mapView.height, cluster.shops)
                    true
                } else {
                    false
                }
            }
        }

        onDispose {
            handler.removeCallbacks(recluster)
            activeMap.removeOnCameraMoveListener(moveListener)
            activeMap.removeOnCameraIdleListener(idleListener)
            activeMap.setOnMarkerClickListener(null)
        }
    }

    LaunchedEffect(map, isDarkTheme) {
        val activeMap = map ?: return@LaunchedEffect
        animations.cancel()
        animations.pulseMark = null
        activeMap.removeAnnotations()
        shopMarks.clear()
        clusterMarks.clear()
        currentLocationMarker = null
        activeMap.setStyle(Style.Builder().fromUri(coffeeMapStyleUri(isDarkTheme))) { style ->
            applyCoffeePeekMapStyle(style, isDarkTheme)
            styleGeneration += 1
            if (!initialCameraApplied) {
                val location = context.lastKnownLocation()
                currentLocation = location?.let { LatLng(it.latitude, it.longitude) }
                val initialTarget = cameraTarget?.let { LatLng(it.first, it.second) }
                    ?: currentLocation
                    ?: LatLng(DEFAULT_LAT, DEFAULT_LON)
                val initialZoom = cameraZoom ?: when {
                    cameraTarget != null -> TARGET_ZOOM
                    location != null -> LOCATION_ZOOM
                    else -> DEFAULT_ZOOM
                }
                activeMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialTarget, initialZoom.toDouble()))
                initialCameraApplied = true
                location?.takeIf { cameraTarget == null }?.let {
                    onMyLocationFoundState.value(it.latitude, it.longitude)
                }
            }
        }
    }

    LaunchedEffect(cameraTarget, cameraZoom, map, styleGeneration) {
        val target = cameraTarget ?: return@LaunchedEffect
        val activeMap = map ?: return@LaunchedEffect
        if (styleGeneration == 0) return@LaunchedEffect
        activeMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(target.first, target.second),
                (cameraZoom ?: TARGET_ZOOM).toDouble(),
            ),
            450,
        )
        onCameraTargetAppliedState.value()
    }

    LaunchedEffect(myLocationRequestKey, map, styleGeneration) {
        if (myLocationRequestKey == 0 || styleGeneration == 0) return@LaunchedEffect
        val activeMap = map ?: return@LaunchedEffect
        if (!context.hasLocationPermission()) {
            locationPermissionLauncher.launch(locationPermissions)
            return@LaunchedEffect
        }

        context.lastKnownLocation()?.let { location ->
            currentLocation = LatLng(location.latitude, location.longitude)
            activeMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    LOCATION_ZOOM.toDouble(),
                ),
                450,
            )
            onMyLocationFoundState.value(location.latitude, location.longitude)
        }
    }

    LaunchedEffect(currentLocation, map, styleGeneration) {
        val activeMap = map ?: return@LaunchedEffect
        val position = currentLocation ?: return@LaunchedEffect
        if (styleGeneration == 0) return@LaunchedEffect

        val marker = currentLocationMarker
        if (marker == null) {
            currentLocationMarker = activeMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .icon(
                        IconFactory.getInstance(appContext)
                            .fromBitmap(MapMarkerIcons.myLocationBitmap(appContext)),
                    ),
            )
        } else {
            marker.position = position
            activeMap.updateMarker(marker)
        }
    }

    LaunchedEffect(shops, selectedShopId, clusterGeneration, styleGeneration, map) {
        val activeMap = map ?: return@LaunchedEffect
        if (styleGeneration == 0) return@LaunchedEffect
        syncMapMarkers(
            context = appContext,
            map = activeMap,
            shops = shops,
            selectedShopId = selectedShopId,
            zoom = activeMap.cameraPosition.zoom.toFloat(),
            project = { shop ->
                activeMap.projection.toScreenLocation(LatLng(shop.latitude, shop.longitude))
                    .let { ScreenXy(it.x, it.y) }
            },
            shopMarks = shopMarks,
            clusterMarks = clusterMarks,
            animations = animations,
            reduceMotion = appContext.prefersReducedMotion(),
        )
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = {
                mapView.apply {
                    getMapAsync { readyMap ->
                        readyMap.uiSettings.apply {
                            isLogoEnabled = false
                            isAttributionEnabled = false
                            isCompassEnabled = false
                        }
                        readyMap.setPrefetchesTiles(true)
                        map = readyMap
                    }
                }
            },
        )
        Text(
            text = "OpenFreeMap · OpenMapTiles · © OSM",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(4.dp),
                )
                .clickable { uriHandler.openUri(OSM_COPYRIGHT_URL) }
                .padding(horizontal = 6.dp, vertical = 3.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 10.sp,
        )
    }
}

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun Context.lastKnownLocation(): Location? {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    if (!hasLocationPermission()) return null
    return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }
        .maxByOrNull { it.time }
}

private fun Context.prefersReducedMotion(): Boolean {
    val duration = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    val transition = Settings.Global.getFloat(contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
    return duration == 0f || transition == 0f
}

private fun syncMapMarkers(
    context: Context,
    map: MapLibreMap,
    shops: List<MapShop>,
    selectedShopId: String?,
    zoom: Float,
    project: (MapShop) -> ScreenXy?,
    shopMarks: MutableMap<String, ShopMark>,
    clusterMarks: MutableMap<String, ClusterMark>,
    animations: MarkerAnimations,
    reduceMotion: Boolean,
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
        shopMarks.remove(id)?.let { map.removeMarker(it.marker) }
    }
    val clusterKeys = clusterItems.map { it.key }.toSet()
    clusterMarks.keys.filter { it !in clusterKeys }.toList().forEach { key ->
        clusterMarks.remove(key)?.let { map.removeMarker(it.marker) }
    }

    clusterItems.forEach { cluster ->
        val position = LatLng(cluster.latitude, cluster.longitude)
        val existing = clusterMarks[cluster.key]
        if (existing == null) {
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .icon(IconFactory.getInstance(context).fromBitmap(MapMarkerIcons.clusterBitmap(context, cluster.count))),
            )
            clusterMarks[cluster.key] = ClusterMark(
                marker = marker,
                latitude = cluster.latitude,
                longitude = cluster.longitude,
                count = cluster.count,
                shops = cluster.shops,
            )
        } else {
            existing.shops = cluster.shops
            if (existing.latitude != cluster.latitude || existing.longitude != cluster.longitude) {
                existing.marker.position = position
                existing.latitude = cluster.latitude
                existing.longitude = cluster.longitude
            }
            if (existing.count != cluster.count) {
                existing.marker.setIcon(
                    IconFactory.getInstance(context).fromBitmap(MapMarkerIcons.clusterBitmap(context, cluster.count)),
                )
                existing.count = cluster.count
            }
            map.updateMarker(existing.marker)
        }
    }

    shopItems.forEach { shop ->
        val position = LatLng(shop.latitude, shop.longitude)
        val isSelected = shop.id == selectedShopId
        val visual = if (isSelected) MapPinVisual.Selected else MapPinVisual.Default
        val existing = shopMarks[shop.id]
        if (existing == null) {
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .icon(IconFactory.getInstance(context).fromBitmap(MapMarkerIcons.pinBitmap(context, shop.type, visual))),
            )
            val entry = ShopMark(
                marker = marker,
                isSelected = isSelected,
                latitude = shop.latitude,
                longitude = shop.longitude,
                shop = shop,
                type = shop.type,
            )
            shopMarks[shop.id] = entry
            if (isSelected) {
                playSelectedAnimation(context, map, entry, animations, reduceMotion)
            }
        } else {
            existing.shop = shop
            if (existing.latitude != shop.latitude || existing.longitude != shop.longitude) {
                existing.marker.position = position
                existing.latitude = shop.latitude
                existing.longitude = shop.longitude
            }
            if (existing.isSelected != isSelected || existing.type != shop.type) {
                existing.marker.setIcon(
                    IconFactory.getInstance(context).fromBitmap(MapMarkerIcons.pinBitmap(context, shop.type, visual)),
                )
                existing.isSelected = isSelected
                existing.type = shop.type
                if (isSelected) {
                    playSelectedAnimation(context, map, existing, animations, reduceMotion)
                }
            }
            map.updateMarker(existing.marker)
        }
    }

    if (selected == null) {
        animations.cancel()
        animations.pulseMark?.let { runCatching { map.removeMarker(it) } }
        animations.pulseMark = null
    }
}

private fun playSelectedAnimation(
    context: Context,
    map: MapLibreMap,
    mark: ShopMark,
    animations: MarkerAnimations,
    reduceMotion: Boolean,
) {
    animations.cancel()
    animations.pulseMark?.let { runCatching { map.removeMarker(it) } }
    animations.pulseMark = null
    if (reduceMotion) return

    val pulseMark = map.addMarker(
        MarkerOptions()
            .position(LatLng(mark.latitude, mark.longitude))
            .icon(IconFactory.getInstance(context).fromBitmap(MapMarkerIcons.pulseBitmap(context, 0))),
    )
    animations.pulseMark = pulseMark
    val pulse = ValueAnimator.ofInt(0, MapMarkerIcons.PULSE_FRAMES).apply {
        duration = 1800L
        interpolator = DecelerateInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animator ->
            val frame = animator.animatedValue as Int
            pulseMark.position = mark.marker.position
            pulseMark.setIcon(
                IconFactory.getInstance(context).fromBitmap(MapMarkerIcons.pulseBitmap(context, frame)),
            )
            map.updateMarker(pulseMark)
        }
    }
    animations.pulse = pulse
    pulse.start()
}

private fun zoomToCluster(
    map: MapLibreMap,
    viewWidth: Int,
    viewHeight: Int,
    shops: List<MapShop>,
) {
    if (shops.isEmpty()) return
    val minLat = shops.minOf { it.latitude }
    val maxLat = shops.maxOf { it.latitude }
    val minLon = shops.minOf { it.longitude }
    val maxLon = shops.maxOf { it.longitude }
    val currentZoom = map.cameraPosition.zoom.toFloat()

    if (minLat == maxLat && minLon == maxLon) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(minLat, minLon),
                clusterFitZoom(currentZoom, currentZoom + 2f).toDouble(),
            ),
            CLUSTER_TAP_ANIMATION_MS.toInt(),
        )
        return
    }

    val bounds = LatLngBounds.from(maxLat, maxLon, minLat, minLon)
    val insetX = (viewWidth.coerceAtLeast(1) * clusterPaddingFraction() / 2f).toInt()
    val insetY = (viewHeight.coerceAtLeast(1) * clusterPaddingFraction() / 2f).toInt()
    val fitted = map.getCameraForLatLngBounds(
        bounds,
        intArrayOf(insetX, insetY, insetX, insetY),
    ) ?: return
    val target = CameraPosition.Builder(fitted)
        .zoom(clusterFitZoom(currentZoom, fitted.zoom.toFloat()).toDouble())
        .build()
    map.animateCamera(
        CameraUpdateFactory.newCameraPosition(target),
        CLUSTER_TAP_ANIMATION_MS.toInt(),
    )
}

private fun LatLngBounds.toMapBounds(): MapBounds = MapBounds(
    minLat = latitudeSouth,
    minLon = longitudeWest,
    maxLat = latitudeNorth,
    maxLon = longitudeEast,
)

private fun coffeeMapStyleUri(isDarkTheme: Boolean): String =
    if (isDarkTheme) OPEN_FREE_MAP_DARK_STYLE else OPEN_FREE_MAP_LIGHT_STYLE

private data class CoffeeMapPalette(
    val background: String,
    val residential: String,
    val park: String,
    val building: String,
    val water: String,
    val waterLine: String,
    val roadCasing: String,
    val road: String,
    val minorRoad: String,
    val boundary: String,
    val text: String,
    val waterText: String,
    val textHalo: String,
)

private fun applyCoffeePeekMapStyle(style: Style, isDarkTheme: Boolean) {
    val palette = if (isDarkTheme) {
        CoffeeMapPalette(
            background = "#1A1412",
            residential = "#211B18",
            park = "#25231D",
            building = "#302722",
            water = "#26343A",
            waterLine = "#42545B",
            roadCasing = "#332A26",
            road = "#4B403A",
            minorRoad = "#3D342F",
            boundary = "#66564D",
            text = "#C8BEB7",
            waterText = "#9AAFB5",
            textHalo = "#1A1412",
        )
    } else {
        CoffeeMapPalette(
            background = "#F8F6F3",
            residential = "#EFEAE5",
            park = "#E4E8DF",
            building = "#E3DDD7",
            water = "#D3DEE1",
            waterLine = "#B2C5CA",
            roadCasing = "#D6CEC7",
            road = "#FFFFFF",
            minorRoad = "#E8E2DC",
            boundary = "#AA9D94",
            text = "#625A55",
            waterText = "#687E84",
            textHalo = "#FAF8F5",
        )
    }

    style.layers.forEach { layer ->
        val id = layer.id.lowercase()
        if (mapNoiseLayerTokens.any(id::contains)) {
            layer.setProperties(visibility(Property.NONE))
            return@forEach
        }

        if (id.contains("highway_path")) {
            layer.minZoom = maxOf(layer.minZoom, 15f)
        }

        when (layer) {
            is BackgroundLayer -> layer.setProperties(backgroundColor(palette.background))
            is FillLayer -> when {
                id == "water" || id.startsWith("water_") -> layer.setProperties(
                    fillColor(palette.water),
                    fillOutlineColor(palette.waterLine),
                )
                id == "park" || id.contains("landcover_wood") || id.contains("landuse_park") -> {
                    layer.setProperties(fillColor(palette.park))
                }
                id.contains("building") -> layer.setProperties(
                    fillColor(palette.building),
                    fillOutlineColor(palette.roadCasing),
                )
                id.contains("residential") -> layer.setProperties(fillColor(palette.residential))
            }
            is LineLayer -> when {
                id.contains("waterway") -> layer.setProperties(lineColor(palette.waterLine))
                id.contains("boundary") -> layer.setProperties(lineColor(palette.boundary))
                id.contains("highway") || id.contains("road") || id.contains("bridge") || id.contains("tunnel") -> {
                    val color = when {
                        id.contains("casing") -> palette.roadCasing
                        id.contains("inner") || id.contains("motorway") || id.contains("major") -> palette.road
                        else -> palette.minorRoad
                    }
                    layer.setProperties(lineColor(color))
                }
            }
            is SymbolLayer -> layer.setProperties(
                textColor(if (id.contains("water")) palette.waterText else palette.text),
                textHaloColor(palette.textHalo),
                textHaloWidth(1.2f),
            )
        }
    }
}

private val mapNoiseLayerTokens = listOf(
    "poi",
    "housenumber",
    "house-number",
    "house_number",
    "road_oneway",
    "transit_stop",
    "transit-stop",
    "bus_stop",
    "bus-stop",
    "ferry_terminal",
    "ferry-terminal",
    "aerodrome_label",
    "aerodrome-label",
    "airport_label",
    "airport-label",
)
