package com.coffeepeek.admin.ui.screen.map

import com.coffeepeek.domain.model.MapBounds
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapPrefetchTest {

    // ~1.1 km × ~0.7 km viewport in central Minsk (zoomed-in street view).
    private val street = MapBounds(minLat = 53.900, minLon = 27.555, maxLat = 53.910, maxLon = 27.566)

    @Test
    fun smallViewportGetsAtLeastThreeKmOnEverySide() {
        val area = street.expandedForPrefetch()
        val kmPerDegLat = 111.32
        assertTrue((street.minLat - area.minLat) * kmPerDegLat >= 2.99)
        assertTrue((area.maxLat - street.maxLat) * kmPerDegLat >= 2.99)
    }

    @Test
    fun panningTwoKmStaysInsideLoadedArea() {
        val area = street.expandedForPrefetch()
        val twoKmNorth = 2.0 / 111.32
        val panned = street.copy(minLat = street.minLat + twoKmNorth, maxLat = street.maxLat + twoKmNorth)
        assertTrue(area.contains(panned))
    }

    @Test
    fun wideViewportIsPaddedByHalfItsSize() {
        val city = MapBounds(minLat = 53.80, minLon = 27.40, maxLat = 54.00, maxLon = 27.70)
        val area = city.expandedForPrefetch()
        assertTrue(area.minLat <= 53.70 + 1e-9 && area.maxLat >= 54.10 - 1e-9)
        assertFalse(city.contains(area))
    }
}
