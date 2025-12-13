package com.coffeepeek.admin.ui.screen.home

import androidx.compose.ui.Alignment
import com.coffeepeek.admin.base.BasePager
import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.base.flatMapPager
import com.coffeepeek.admin.locator.Locator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class HomeViewModel : BaseViewModel() {

    private val repo = Locator.flowerRepo.itemService

    val search = MutableStateFlow("")

    private val pager = search.flatMapPager(workScope){
        BasePager(
            load = { repo.getItems("", it).map { it.list } },
            scope = workScope
        )
    }

    val data = pager.flatMapLatest { it.data }.stateHere(emptyList())
    val isRefreshing = pager.flatMapLatest { it.isRefreshing }

    val userFlow = Locator.setting.userFlow

    val menuPosition = MutableStateFlow(Alignment.BottomEnd)

    val position = MutableStateFlow(0)

    fun setPosition(position: Int){
        this.position.value = position
        pager.value.position(position)
    }

    fun refresh(){
        pager.value.refresh()
    }

}