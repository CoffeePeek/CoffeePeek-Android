package com.coffeepeek.admin.ui.screen.feed

import kotlin.test.Test
import kotlin.test.assertEquals

class FeedFiltersUiTest {

    @Test
    fun activeFilterCountIncludesQuickAndAdvancedFilters() {
        val filters = FeedFiltersUi(
            cityId = "minsk",
            coffeeFocus = "specialty",
            openOnly = true,
            roasterIds = setOf("roaster-1"),
        )

        assertEquals(3, filters.activeFilterCount)
    }

    @Test
    fun clearSelectionsRemovesQuickAndAdvancedFiltersButKeepsCity() {
        val filters = FeedFiltersUi(
            cityId = "minsk",
            coffeeFocus = "specialty",
            openOnly = true,
            newOnly = true,
            nearbyOnly = true,
            priceRange = 2,
            roasterIds = setOf("roaster-1"),
            beanIds = setOf("bean-1"),
            tagIds = setOf("wifi"),
        )

        assertEquals(FeedFiltersUi(cityId = "minsk"), filters.clearSelections())
        assertEquals(0, filters.clearSelections().activeFilterCount)
    }
}
