package com.coffeepeek.admin.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

object Texts {


    @Composable
    fun Headline6(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            modifier = modifier
        )
    }

    @Composable
    fun Body1(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            modifier = modifier
        )
    }

    @Composable
    fun Body2(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            modifier = modifier
        )
    }

}