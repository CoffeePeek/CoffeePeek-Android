@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.coffeepeek.admin.map

import platform.UIKit.UIView

/**
 * Small Objective-C-compatible seam between the shared Compose map state and
 * an optional native iOS map implementation. The default implementation stays
 * in Kotlin (MapKit), while the iOS app can register a MapLibre provider.
 */
interface IosNativeMapCallbacks {
    fun onShopClick(shopId: String)
    fun onZoneClick(zoneId: String)
    fun onBoundsChanged(
        minLat: Double,
        minLon: Double,
        maxLat: Double,
        maxLon: Double,
        zoom: Float,
    )
}

interface IosNativeMapProvider {
    fun createMapView(): UIView

    fun updateMapView(
        mapView: UIView,
        stateJson: String,
        callbacks: IosNativeMapCallbacks,
    )

    fun moveCamera(
        mapView: UIView,
        latitude: Double,
        longitude: Double,
        zoom: Float,
        animated: Boolean,
    )
}

object IosNativeMapRegistry {
    var provider: IosNativeMapProvider? = null
}
