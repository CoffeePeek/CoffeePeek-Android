package com.coffeepeek.admin.ui.screen.shop

import kotlin.test.Test
import kotlin.test.assertEquals

class ShopRouteUrlTest {

    @Test
    fun buildsYandexMapsRouteFromCurrentLocation() {
        assertEquals(
            "https://yandex.ru/maps/?mode=routes&rtext=~53.902284,27.561831&rtt=auto",
            buildYandexMapsRouteUrl(latitude = 53.902284, longitude = 27.561831),
        )
    }
}
