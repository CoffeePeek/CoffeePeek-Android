package com.coffeepeek.admin.location

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

expect object PlatformLocation {
    suspend fun getLastKnownLocation(): GeoPoint?
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String?
}
