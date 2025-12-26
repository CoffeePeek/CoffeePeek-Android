package com.coffeepeek.admin.ui

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.locator.Locator

class NavigatorViewModel: BaseViewModel() {

    val user = Locator.setting.userFlow.value


}