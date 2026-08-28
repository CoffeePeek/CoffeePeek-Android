package com.coffeepeek.admin.map

import com.coffeepeek.domain.model.MapShop
import kotlin.math.hypot

internal const val MIN_CLUSTER_ZOOM = 15f
internal const val CLUSTER_RADIUS_PX = 46f
internal const val CLUSTER_TAP_MAX_ZOOM = 17f
internal const val CLUSTER_TAP_ANIMATION_MS = 350L
internal const val CLUSTER_MOVE_DEBOUNCE_MS = 300L

internal data class ScreenXy(val x: Float, val y: Float)

internal sealed class MapMarkerItem {
    data class Shop(val shop: MapShop) : MapMarkerItem()
    data class Cluster(
        val shops: List<MapShop>,
        val latitude: Double,
        val longitude: Double,
    ) : MapMarkerItem() {
        val key: String get() = shops.map { it.id }.sorted().joinToString(",")
        val count: Int get() = shops.size
        val label: String get() = clusterCountLabel(count)
    }
}

internal fun clusterCountLabel(count: Int): String =
    if (count > 99) "99+" else count.toString()

internal fun clusterDiameterDp(count: Int): Float = when {
    count >= 100 -> 60f
    count >= 10 -> 55f
    else -> 49f
}

/**
 * Greedy screen-space clustering. [project] maps a shop to screen pixels;
 * a null projection skips that shop as a neighbour (it can still seed a group).
 */
internal fun clusterMapShops(
    shops: List<MapShop>,
    zoom: Float,
    project: (MapShop) -> ScreenXy?,
): List<MapMarkerItem> {
    val valid = shops.filter { it.latitude.isFinite() && it.longitude.isFinite() }
    if (valid.isEmpty()) return emptyList()
    if (zoom >= MIN_CLUSTER_ZOOM) {
        return valid.map { MapMarkerItem.Shop(it) }
    }

    val remaining = valid.toMutableList()
    val result = ArrayList<MapMarkerItem>(valid.size)
    while (remaining.isNotEmpty()) {
        val seed = remaining.removeAt(remaining.lastIndex)
        val group = mutableListOf(seed)
        val seedPoint = project(seed)
        if (seedPoint != null) {
            val iterator = remaining.iterator()
            while (iterator.hasNext()) {
                val candidate = iterator.next()
                val candidatePoint = project(candidate) ?: continue
                val distance = hypot(
                    (seedPoint.x - candidatePoint.x).toDouble(),
                    (seedPoint.y - candidatePoint.y).toDouble(),
                )
                if (distance <= CLUSTER_RADIUS_PX) {
                    group += candidate
                    iterator.remove()
                }
            }
        }
        if (group.size == 1) {
            result += MapMarkerItem.Shop(group.first())
        } else {
            result += MapMarkerItem.Cluster(
                shops = group.toList(),
                latitude = group.map { it.latitude }.average(),
                longitude = group.map { it.longitude }.average(),
            )
        }
    }
    return result
}

internal fun clusterFitZoom(currentZoom: Float, fittedZoom: Float): Float =
    minOf(fittedZoom, minOf(currentZoom + 2f, CLUSTER_TAP_MAX_ZOOM))

internal fun clusterPaddingFraction(): Float = 0.25f
