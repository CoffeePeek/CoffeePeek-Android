package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.coffeepeek.admin.theme.Colors

object BaseAppBar {


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        title: @Composable () -> Unit,
        navigationIcon: @Composable () -> Unit = {},
        actions: @Composable RowScope.() -> Unit = {},
        expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
        scrollBehavior: TopAppBarScrollBehavior? = null,
        modifier: Modifier = Modifier.Companion
    ){
        Column {
            Insets.TopInset()
            TopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                expandedHeight = expandedHeight,
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Colors.cardBackground),
                modifier = modifier
            )
            HorizontalDivider(modifier = Modifier.Companion.fillMaxWidth())
        }
    }

}