package com.coffeepeek.admin.ui.screen.contributions

import com.coffeepeek.domain.model.ModerationStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class MyContributionsUiStateTest {

    private fun tab(count: Int) = ContributionTab(items = List(count) { ContributionItem(id = "$it", title = "t") })

    @Test
    fun onlyStatusesWithItemsGetATabInFixedOrder() {
        val state = MyContributionsUiState(
            isLoading = false,
            tabs = mapOf(
                ModerationStatus.Rejected to tab(1),
                ModerationStatus.Pending to tab(0),
                ModerationStatus.Approved to tab(2),
            ),
        )
        assertEquals(listOf(ModerationStatus.Approved, ModerationStatus.Rejected), state.visibleTabs)
    }

    @Test
    fun noItemsMeansNoTabs() {
        assertEquals(emptyList(), MyContributionsUiState(isLoading = false).visibleTabs)
    }
}
