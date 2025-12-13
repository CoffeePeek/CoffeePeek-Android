package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.image_not_available
import com.coffeepeek.api.model.response.ItemResp
import com.coffeepeek.admin.theme.Colors
import com.coffeepeek.admin.theme.Theme
import com.coffeepeek.admin.utils.KamelExt.FlowerImage

object ItemCard {


    @Composable
    operator fun invoke(
        item: ItemResp,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .padding(4.dp)
                .fillMaxSize()
                .clip(Theme.shape)
                .background(Colors.cardBackground)
                .border(Theme.border, Theme.shape)
                .clickable(onClick = onClick)

        ){
            Row(
                modifier = Modifier.fillMaxSize()

            ) {
                val image = item.images.firstOrNull()
                if (image != null) FlowerImage(
                    data = image,
                    error = Res.drawable.image_not_available,
                    modifier = Modifier.size(108.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .heightIn(100.dp)
                ) {
                    Texts.Headline6(text = item.name)
                    Texts.Body1(text = item.description)
                    Spacer(Modifier.weight(1f))
                }
            }

        }
    }


}