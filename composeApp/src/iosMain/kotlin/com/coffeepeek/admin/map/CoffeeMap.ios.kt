@file:OptIn(
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class,
)

package com.coffeepeek.admin.map

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.coffeepeek.admin.location.LocationPermissionEffect
import com.coffeepeek.admin.location.PlatformLocation
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapCluster
import com.coffeepeek.domain.model.MapCoffeeZone
import com.coffeepeek.domain.model.MapShop
import com.coffeepeek.domain.model.CoffeeShopType
import kotlinx.cinterop.useContents
import kotlinx.coroutines.launch
import platform.Foundation.NSBundle
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIUserInterfaceStyle
import platform.darwin.NSObject

private const val DEFAULT_LAT = 53.9045
private const val DEFAULT_LON = 27.5615
private const val DEFAULT_ZOOM = 12f
private const val LOCATION_ZOOM = 15f

@Composable
actual fun CoffeeMap(
    shops: List<MapShop>,
    clusters: List<MapCluster>,
    zones: List<MapCoffeeZone>,
    selectedShopId: String?,
    onBoundsChanged: (MapBounds, Float) -> Unit,
    onShopClick: (MapShop) -> Unit,
    onZoneClick: (MapCoffeeZone) -> Unit,
    modifier: Modifier,
    cameraTarget: Pair<Double, Double>?,
    cameraZoom: Float?,
    onCameraTargetApplied: () -> Unit,
    isDarkTheme: Boolean,
    myLocationRequestKey: Int,
    onMyLocationFound: (Double, Double) -> Unit,
    onLocationPermissionDenied: () -> Unit,
) {
    val boundsCallback = rememberUpdatedState(onBoundsChanged)
    val shopCallback = rememberUpdatedState(onShopClick)
    val targetAppliedCallback = rememberUpdatedState(onCameraTargetApplied)
    val locationFoundCallback = rememberUpdatedState(onMyLocationFound)
    val locationDeniedCallback = rememberUpdatedState(onLocationPermissionDenied)
    val zoneCallback = rememberUpdatedState(onZoneClick)
    val scope = rememberCoroutineScope()

    val nativeProvider = remember { IosNativeMapRegistry.provider }
    if (nativeProvider != null) {
        NativeMapContent(
            provider = nativeProvider,
            shops = shops,
            clusters = clusters,
            zones = zones,
            selectedShopId = selectedShopId,
            onBoundsChanged = { boundsCallback.value(it.first, it.second) },
            onShopClick = { shopId -> shops.firstOrNull { it.id == shopId }?.let(shopCallback.value) },
            onZoneClick = { zoneId -> zones.firstOrNull { it.id == zoneId }?.let(zoneCallback.value) },
            modifier = modifier,
            cameraTarget = cameraTarget,
            cameraZoom = cameraZoom,
            onCameraTargetApplied = { targetAppliedCallback.value() },
            isDarkTheme = isDarkTheme,
            myLocationRequestKey = myLocationRequestKey,
            onMyLocationFound = onMyLocationFound,
            onLocationPermissionDenied = onLocationPermissionDenied,
        )
        return
    }

    val coordinator = remember {
        AppleMapCoordinator(
            onBoundsChanged = { bounds, zoom -> boundsCallback.value(bounds, zoom) },
            onShopClick = { shop -> shopCallback.value(shop) },
            onZoneClick = { zone -> zoneCallback.value(zone) },
        )
    }

    DisposableEffect(coordinator) {
        onDispose { coordinator.detach() }
    }

    LaunchedEffect(cameraTarget, cameraZoom) {
        if (cameraTarget != null) {
            coordinator.moveCamera(
                latitude = cameraTarget.first,
                longitude = cameraTarget.second,
                zoom = cameraZoom ?: DEFAULT_ZOOM,
                animated = true,
            )
            targetAppliedCallback.value()
        }
    }

    fun findAndShowLocation() {
        scope.launch {
            val point = PlatformLocation.getLastKnownLocation()
            if (point == null) {
                locationDeniedCallback.value()
            } else {
                coordinator.moveCamera(point.latitude, point.longitude, LOCATION_ZOOM, animated = true)
                locationFoundCallback.value(point.latitude, point.longitude)
            }
        }
    }

    Box(modifier = modifier) {
        LocationPermissionEffect(
            requestKey = myLocationRequestKey,
            onGranted = ::findAndShowLocation,
            onDenied = { locationDeniedCallback.value() },
        )

        UIKitView(
            factory = {
                MKMapView().also { map ->
                    coordinator.attach(map)
                    coordinator.moveCamera(DEFAULT_LAT, DEFAULT_LON, DEFAULT_ZOOM, animated = false)
                }
            },
            modifier = Modifier.matchParentSize(),
            update = { map ->
                map.overrideUserInterfaceStyle =
                    if (isDarkTheme) {
                        UIUserInterfaceStyle.UIUserInterfaceStyleDark
                    } else {
                        UIUserInterfaceStyle.UIUserInterfaceStyleLight
                    }
                coordinator.updateContent(
                    shops = shops,
                    clusters = clusters,
                    zones = zones,
                    selectedShopId = selectedShopId,
                    isDarkTheme = isDarkTheme,
                )
            },
        )
    }
}

@Composable
private fun NativeMapContent(
    provider: IosNativeMapProvider,
    shops: List<MapShop>,
    clusters: List<MapCluster>,
    zones: List<MapCoffeeZone>,
    selectedShopId: String?,
    onBoundsChanged: (Pair<MapBounds, Float>) -> Unit,
    onShopClick: (String) -> Unit,
    onZoneClick: (String) -> Unit,
    modifier: Modifier,
    cameraTarget: Pair<Double, Double>?,
    cameraZoom: Float?,
    onCameraTargetApplied: () -> Unit,
    isDarkTheme: Boolean,
    myLocationRequestKey: Int,
    onMyLocationFound: (Double, Double) -> Unit,
    onLocationPermissionDenied: () -> Unit,
) {
    val boundsCallback = rememberUpdatedState(onBoundsChanged)
    val shopCallback = rememberUpdatedState(onShopClick)
    val zoneCallback = rememberUpdatedState(onZoneClick)
    val locationFoundCallback = rememberUpdatedState(onMyLocationFound)
    val locationDeniedCallback = rememberUpdatedState(onLocationPermissionDenied)
    val scope = rememberCoroutineScope()
    val session = remember {
        NativeMapSession(
            provider = provider,
            onBoundsChanged = { bounds, zoom -> boundsCallback.value(bounds to zoom) },
            onShopClick = { shopCallback.value(it) },
            onZoneClick = { zoneCallback.value(it) },
        )
    }

    DisposableEffect(session) {
        onDispose { session.detach() }
    }

    LaunchedEffect(cameraTarget, cameraZoom) {
        cameraTarget?.let { target ->
            session.moveCamera(target.first, target.second, cameraZoom ?: DEFAULT_ZOOM, animated = true)
            onCameraTargetApplied()
        }
    }

    fun findAndShowLocation() {
        scope.launch {
            val point = PlatformLocation.getLastKnownLocation()
            if (point == null) {
                locationDeniedCallback.value()
            } else {
                session.moveCamera(point.latitude, point.longitude, LOCATION_ZOOM, animated = true)
                locationFoundCallback.value(point.latitude, point.longitude)
            }
        }
    }

    Box(modifier = modifier) {
        LocationPermissionEffect(
            requestKey = myLocationRequestKey,
            onGranted = ::findAndShowLocation,
            onDenied = { locationDeniedCallback.value() },
        )
        UIKitView(
            factory = { provider.createMapView().also(session::attach) },
            modifier = Modifier.matchParentSize(),
            update = { mapView ->
                session.attach(mapView)
                session.updateContent(shops, clusters, zones, selectedShopId, isDarkTheme)
            },
        )
    }
}

private class NativeMapSession(
    private val provider: IosNativeMapProvider,
    private val onBoundsChanged: (MapBounds, Float) -> Unit,
    private val onShopClick: (String) -> Unit,
    private val onZoneClick: (String) -> Unit,
) : IosNativeMapCallbacks {
    private var mapView: platform.UIKit.UIView? = null

    fun attach(view: platform.UIKit.UIView) {
        mapView = view
    }

    fun detach() {
        mapView = null
    }

    fun updateContent(
        shops: List<MapShop>,
        clusters: List<MapCluster>,
        zones: List<MapCoffeeZone>,
        selectedShopId: String?,
        isDarkTheme: Boolean,
    ) {
        mapView?.let { view ->
            provider.updateMapView(
                mapView = view,
                stateJson = nativeMapStateJson(shops, clusters, zones, selectedShopId, isDarkTheme),
                callbacks = this,
            )
        }
    }

    fun moveCamera(latitude: Double, longitude: Double, zoom: Float, animated: Boolean) {
        mapView?.let { view -> provider.moveCamera(view, latitude, longitude, zoom, animated) }
    }

    override fun onShopClick(shopId: String) = onShopClick.invoke(shopId)

    override fun onZoneClick(zoneId: String) = onZoneClick.invoke(zoneId)

    override fun onBoundsChanged(
        minLat: Double,
        minLon: Double,
        maxLat: Double,
        maxLon: Double,
        zoom: Float,
    ) = onBoundsChanged(MapBounds(minLat, minLon, maxLat, maxLon), zoom)
}

private fun nativeMapStateJson(
    shops: List<MapShop>,
    clusters: List<MapCluster>,
    zones: List<MapCoffeeZone>,
    selectedShopId: String?,
    isDarkTheme: Boolean,
): String = buildString {
    append('{')
    append("\"selectedShopId\":").append(selectedShopId.jsonValue()).append(',')
    append("\"dark\":").append(isDarkTheme).append(',')
    append("\"shops\":[")
    shops.joinTo(this, separator = ",") { shop ->
        "{\"id\":${shop.id.jsonValue()},\"title\":${shop.title.jsonValue()}," +
            "\"lat\":${shop.latitude},\"lon\":${shop.longitude},\"type\":${shop.type.jsonValue()}," +
            "\"selected\":${shop.id == selectedShopId}}"
    }
    append("],\"clusters\":[")
    clusters.joinTo(this, separator = ",") { cluster ->
        "{\"id\":${cluster.id.jsonValue()},\"lat\":${cluster.latitude}," +
            "\"lon\":${cluster.longitude},\"count\":${cluster.count}}"
    }
    append("],\"zones\":[")
    zones.joinTo(this, separator = ",") { zone ->
        "{\"id\":${zone.id.jsonValue()},\"name\":${zone.name.jsonValue()}," +
            "\"lat\":${zone.latitude},\"lon\":${zone.longitude}," +
            "\"radius\":${zone.radiusMeters},\"polygon\":[" +
            zone.polygon.joinToString(",") { (lat, lon) -> "[$lat,$lon]" } + "]}"
    }
    append("]}")
}

private fun String?.jsonValue(): String = if (this == null) {
    "null"
} else {
    buildString {
        append('"')
        for (character in this@jsonValue) {
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(character)
            }
        }
        append('"')
    }
}

private class AppleMapCoordinator(
    private val onBoundsChanged: (MapBounds, Float) -> Unit,
    private val onShopClick: (MapShop) -> Unit,
    private val onZoneClick: (MapCoffeeZone) -> Unit,
) : NSObject(), MKMapViewDelegateProtocol {
    private var mapView: MKMapView? = null
    private val shopsByAnnotation = mutableMapOf<MKPointAnnotation, MapShop>()
    private val clustersByAnnotation = mutableMapOf<MKPointAnnotation, MapCluster>()
    private val zonesByAnnotation = mutableMapOf<MKPointAnnotation, MapCoffeeZone>()

    fun attach(map: MKMapView) {
        mapView = map
        map.delegate = this
        map.showsCompass = true
        map.showsScale = true
    }

    fun detach() {
        mapView?.delegate = null
        mapView = null
        shopsByAnnotation.clear()
        clustersByAnnotation.clear()
        zonesByAnnotation.clear()
    }

    fun updateContent(
        shops: List<MapShop>,
        clusters: List<MapCluster>,
        zones: List<MapCoffeeZone>,
        selectedShopId: String?,
        isDarkTheme: Boolean,
    ) {
        val map = mapView ?: return
        map.removeAnnotations(map.annotations)
        shopsByAnnotation.clear()
        clustersByAnnotation.clear()
        zonesByAnnotation.clear()

        shops.forEach { shop ->
            val annotation = pointAnnotation(
                latitude = shop.latitude,
                longitude = shop.longitude,
                title = if (shop.id == selectedShopId) "● ${shop.title}" else shop.title,
            )
            shopsByAnnotation[annotation] = shop
            map.addAnnotation(annotation)
        }

        clusters.forEach { cluster ->
            val annotation = pointAnnotation(
                latitude = cluster.latitude,
                longitude = cluster.longitude,
                title = cluster.count.toString(),
            )
            clustersByAnnotation[annotation] = cluster
            map.addAnnotation(annotation)
        }

        // Keep the shared zone center tappable on the fallback MapKit map.
        zones.forEach { zone ->
            val annotation = pointAnnotation(
                latitude = zone.latitude,
                longitude = zone.longitude,
                title = zone.name,
            )
            zonesByAnnotation[annotation] = zone
            map.addAnnotation(annotation)
        }
    }

    fun moveCamera(
        latitude: Double,
        longitude: Double,
        zoom: Float,
        animated: Boolean,
    ) {
        val map = mapView ?: return
        val longitudeDelta = (360.0 / (1 shl zoom.toInt().coerceIn(1, 20))).coerceAtLeast(0.001)
        val latitudeDelta = longitudeDelta * 0.65
        val region = MKCoordinateRegionMake(
            CLLocationCoordinate2DMake(latitude, longitude),
            MKCoordinateSpanMake(latitudeDelta, longitudeDelta),
        )
        map.setRegion(region, animated = animated)
    }

    override fun mapView(
        mapView: MKMapView,
        didSelectAnnotationView: MKAnnotationView,
    ) {
        val annotation = didSelectAnnotationView.annotation as? MKPointAnnotation ?: return
        shopsByAnnotation[annotation]?.let(onShopClick)
        zonesByAnnotation[annotation]?.let(onZoneClick)
    }

    override fun mapView(
        mapView: MKMapView,
        viewForAnnotation: MKAnnotationProtocol,
    ): MKAnnotationView? {
        val annotation = viewForAnnotation as? MKPointAnnotation ?: return null
        shopsByAnnotation[annotation]?.let { shop ->
            return MKAnnotationView(
                annotation = annotation,
                reuseIdentifier = "coffee-shop-${shop.type}",
            ).apply {
                image = mascotImage(shop.type)
                canShowCallout = false
                centerOffset = platform.CoreGraphics.CGPointMake(0.0, -14.0)
            }
        }
        clustersByAnnotation[annotation]?.let { cluster ->
            return MKMarkerAnnotationView(
                annotation = annotation,
                reuseIdentifier = "coffee-cluster",
            ).apply {
                glyphText = cluster.count.toString()
                markerTintColor = UIColor.yellowColor
                canShowCallout = false
            }
        }
        zonesByAnnotation[annotation]?.let {
            return MKMarkerAnnotationView(
                annotation = annotation,
                reuseIdentifier = "coffee-zone",
            ).apply {
                glyphText = "⌖"
                markerTintColor = UIColor.blueColor
                canShowCallout = false
            }
        }
        return null
    }

    override fun mapView(mapView: MKMapView, regionDidChangeAnimated: Boolean) {
        val region = mapView.region
        region.useContents {
            val minLat = center.latitude - span.latitudeDelta / 2.0
            val maxLat = center.latitude + span.latitudeDelta / 2.0
            val minLon = center.longitude - span.longitudeDelta / 2.0
            val maxLon = center.longitude + span.longitudeDelta / 2.0
            val zoom = kotlin.math.log2(360.0 / span.longitudeDelta)
                .toFloat()
                .coerceIn(0f, 20f)
            onBoundsChanged(MapBounds(minLat, minLon, maxLat, maxLon), zoom)
        }
    }
}

/**
 * Compose resources are copied next to the xtool app bundle. Keep the lookup
 * tolerant of both the current xtool layout and the framework resource layout
 * used by a regular Xcode build.
 */
private fun mascotImage(type: String): UIImage? {
    val fileName = when (type) {
        CoffeeShopType.SPECIALTY -> "maskot_with_bean.png"
        CoffeeShopType.CAFE -> "maskot_with_dessert.png"
        else -> "maskot_with_cup.png"
    }
    val resourceRoot = NSBundle.mainBundle.resourcePath ?: return null
    val candidates = listOf(
        NSBundle.mainBundle.pathForResource(fileName.removeSuffix(".png"), ofType = "png"),
        "$resourceRoot/composeResources/coffeepeek.composeapp.generated.resources/drawable/$fileName",
        "$resourceRoot/compose-resources/composeResources/coffeepeek.composeapp.generated.resources/drawable/$fileName",
        "$resourceRoot/Frameworks/ComposeApp.framework/composeResources/coffeepeek.composeapp.generated.resources/drawable/$fileName",
    ).filterNotNull()
    return candidates.firstNotNullOfOrNull { path -> UIImage(contentsOfFile = path) }
}

private fun pointAnnotation(
    latitude: Double,
    longitude: Double,
    title: String,
): MKPointAnnotation = MKPointAnnotation().apply {
    setCoordinate(CLLocationCoordinate2DMake(latitude, longitude))
    setTitle(title)
}
