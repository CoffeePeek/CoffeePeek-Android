package com.coffeepeek.admin.ui

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NavigatorViewModel(
    sessionRepository: SessionRepository,
) : BaseViewModel() {

    val isLoggedIn = sessionRepository.observeSession()
        .map { it?.accessToken?.isNotBlank() == true }
        .stateIn(workScope, SharingStarted.Eagerly, false)
}
