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
import com.coffeepeek.domain.model.MapShop
import kotlinx.cinterop.useContents
import kotlinx.coroutines.launch
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIUserInterfaceStyle
import platform.darwin.NSObject

private const val DEFAULT_LAT = 53.9045
private const val DEFAULT_LON = 27.5615
private const val DEFAULT_ZOOM = 12f
private const val LOCATION_ZOOM = 15f

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
    val boundsCallback = rememberUpdatedState(onBoundsChanged)
    val shopCallback = rememberUpdatedState(onShopClick)
    val targetAppliedCallback = rememberUpdatedState(onCameraTargetApplied)
    val locationFoundCallback = rememberUpdatedState(onMyLocationFound)
    val locationDeniedCallback = rememberUpdatedState(onLocationPermissionDenied)
    val scope = rememberCoroutineScope()

    val coordinator = remember {
        AppleMapCoordinator(
            onBoundsChanged = { bounds -> boundsCallback.value(bounds) },
            onShopClick = { shop -> shopCallback.value(shop) },
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
                    selectedShopId = selectedShopId,
                )
            },
        )
    }
}

private class AppleMapCoordinator(
    private val onBoundsChanged: (MapBounds) -> Unit,
    private val onShopClick: (MapShop) -> Unit,
) : NSObject(), MKMapViewDelegateProtocol {
    private var mapView: MKMapView? = null
    private val shopsByAnnotation = mutableMapOf<MKPointAnnotation, MapShop>()

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
    }

    fun updateContent(
        shops: List<MapShop>,
        selectedShopId: String?,
    ) {
        val map = mapView ?: return
        map.removeAnnotations(map.annotations)
        shopsByAnnotation.clear()

        shops.forEach { shop ->
            val annotation = pointAnnotation(
                latitude = shop.latitude,
                longitude = shop.longitude,
                title = if (shop.id == selectedShopId) "● ${shop.title}" else shop.title,
            )
            shopsByAnnotation[annotation] = shop
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
    }

    override fun mapView(mapView: MKMapView, regionDidChangeAnimated: Boolean) {
        val region = mapView.region
        region.useContents {
            val minLat = center.latitude - span.latitudeDelta / 2.0
            val maxLat = center.latitude + span.latitudeDelta / 2.0
            val minLon = center.longitude - span.longitudeDelta / 2.0
            val maxLon = center.longitude + span.longitudeDelta / 2.0
            onBoundsChanged(MapBounds(minLat, minLon, maxLat, maxLon))
        }
    }
}

private fun pointAnnotation(
    latitude: Double,
    longitude: Double,
    title: String,
): MKPointAnnotation = MKPointAnnotation().apply {
    setCoordinate(CLLocationCoordinate2DMake(latitude, longitude))
    setTitle(title)
}
