package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.utils.DrawableExt.toPainterResource
import org.jetbrains.compose.resources.DrawableResource

object FabMenu {

    data class Menu(
        val resource: DrawableResource
    )


    @Composable
    operator fun invoke(
        items: List<Menu>,
        buttonSize: Dp = 48.dp,
        padding: Dp = 1.dp,
        onClick: (Menu) -> Unit = {},
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier =  modifier
        ) {
            items.forEachIndexed { i, menu ->
                val isStart = i == 0
                val isEnd = i == items.lastIndex
                FloatingActionButton(
                    onClick = { onClick(menu) },
                    shape = RoundedCornerShape(
                        topStart = if (isStart) 18.dp else CpDimens.radiusSm,
                        bottomStart = if (isStart) 18.dp else CpDimens.radiusSm,
                        topEnd = if (isEnd) 18.dp else CpDimens.radiusSm,
                        bottomEnd = if (isEnd) 18.dp else CpDimens.radiusSm
                    ),
                    containerColor = CpColor.Success,
                    contentColor = CpColor.LightSurface,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                    modifier = Modifier.padding(padding)
                        .size(buttonSize)
                ) {
                    Icon(
                        painter = menu.resource.toPainterResource(),
                        contentDescription = null
                    )
                }
            }
        }
    }

}