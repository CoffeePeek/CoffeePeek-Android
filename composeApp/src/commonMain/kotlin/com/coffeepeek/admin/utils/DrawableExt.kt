package com.coffeepeek.admin.utils

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

object DrawableExt {

    @Composable
    fun DrawableResource.toPainterResource() = painterResource(this)

}