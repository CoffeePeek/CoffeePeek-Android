package com.coffeepeek.admin.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coffeepeek.admin.theme.Theme
import com.coffeepeek.admin.ui.component.Buttons
import com.coffeepeek.admin.ui.component.Insets
import com.coffeepeek.admin.ui.component.PhotoViewer

object AuthScreen {


    @Composable
    operator fun invoke(
        vm: AuthViewModel = viewModel { AuthViewModel() }
    ) {

    }

}