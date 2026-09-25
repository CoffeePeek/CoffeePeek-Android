package com.coffeepeek.admin.ui.screen.map

import com.coffeepeek.domain.model.CoffeeShop
import com.coffeepeek.domain.model.ShopLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MapSearchTest {

    @Test
    fun searchResultRequiresCoordinatesToBecomeMapMarker() {
        val shop = CoffeeShop(
            id = "shop-1",
            title = "Coffee Place",
            rating = null,
            cityName = null,
            priceRange = null,
            photoUrl = null,
            location = ShopLocation(address = "Main street", latitude = 53.9, longitude = 27.56),
        )

        val marker = shop.toMapShopOrNull()

        assertEquals("shop-1", marker?.id)
        assertEquals(53.9, marker?.latitude)
        assertNull(shop.copy(location = ShopLocation(address = "Main street")).toMapShopOrNull())
    }
}
