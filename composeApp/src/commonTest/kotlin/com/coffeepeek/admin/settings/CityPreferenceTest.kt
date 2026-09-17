package com.coffeepeek.admin.settings

import com.coffeepeek.domain.model.City
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CityPreferenceTest {
    private val cities = listOf(
        City(id = "minsk", name = "Минск"),
        City(id = "grodno", name = "Гродно"),
    )

    @Test
    fun keepsSavedCityWhenItStillExists() {
        assertEquals("grodno", resolveSelectedCityId("grodno", cities))
    }

    @Test
    fun selectsFirstCityWhenSavedCityIsMissing() {
        assertEquals("minsk", resolveSelectedCityId("removed", cities))
    }

    @Test
    fun returnsNullWhenCatalogHasNoCities() {
        assertNull(resolveSelectedCityId("minsk", emptyList()))
    }
}
