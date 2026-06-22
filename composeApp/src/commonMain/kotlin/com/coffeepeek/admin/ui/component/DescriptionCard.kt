package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
object DescriptionCard {


    @Composable
    operator fun invoke(
        title: String,
        description: String,
        modifier: Modifier = Modifier
    ) {
        Column(modifier = modifier) {
            Texts.Body1(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp),
            )
            Texts.Body2(
                text = description,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(start = 32.dp),
            )
        }
    }


}