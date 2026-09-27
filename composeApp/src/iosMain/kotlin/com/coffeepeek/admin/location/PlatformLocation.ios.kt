@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.coffeepeek.admin.location

import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.CLPlacemark
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual object PlatformLocation {
    actual fun hasPermission(): Boolean = when (CLLocationManager().authorizationStatus) {
        kCLAuthorizationStatusAuthorizedAlways,
        kCLAuthorizationStatusAuthorizedWhenInUse -> true
        else -> false
    }

    actual suspend fun getLastKnownLocation(): GeoPoint? {
        if (!hasPermission()) return null
        val manager = CLLocationManager()
        manager.location?.let { location ->
            return location.coordinate.useContents { GeoPoint(latitude, longitude) }
        }
        return suspendCancellableCoroutine { continuation ->
            val delegate = OneShotLocationDelegate(
                manager = manager,
                onResult = { point ->
                    if (continuation.isActive) continuation.resume(point)
                },
            )
            manager.delegate = delegate
            continuation.invokeOnCancellation { delegate.dispose() }
            manager.requestLocation()
        }
    }

    actual suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        suspendCancellableCoroutine { continuation ->
            val geocoder = CLGeocoder()
            continuation.invokeOnCancellation { geocoder.cancelGeocode() }
            val location = CLLocation(latitude = latitude, longitude = longitude)
            geocoder.reverseGeocodeLocation(location) { placemarks, error ->
                if (!continuation.isActive) return@reverseGeocodeLocation
                val placemark = placemarks?.firstOrNull() as? CLPlacemark
                val street = placemark?.thoroughfare?.trim()?.takeIf(String::isNotEmpty)
                val house = placemark?.subThoroughfare?.trim()?.takeIf(String::isNotEmpty)
                val title = placemark?.name?.trim()?.takeIf(String::isNotEmpty)
                val result = listOfNotNull(street, house).joinToString(", ").ifBlank { title }
                continuation.resume(if (error == null) result else null)
            }
        }
}

private class OneShotLocationDelegate(
    private val manager: CLLocationManager,
    private val onResult: (GeoPoint?) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {
    private var completed = false

    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>,
    ) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        finish(location?.coordinate?.useContents { GeoPoint(latitude, longitude) })
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        finish(null)
    }

    fun dispose() {
        completed = true
        manager.delegate = null
        manager.stopUpdatingLocation()
    }

    private fun finish(point: GeoPoint?) {
        if (completed) return
        completed = true
        manager.delegate = null
        manager.stopUpdatingLocation()
        onResult(point)
    }
}
