package com.coffeepeek.admin.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.ic_arrow_back_24px
import com.coffeepeek.admin.theme.Colors
import com.coffeepeek.admin.theme.Theme
import com.coffeepeek.admin.utils.DrawableExt.toPainterResource
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.IconButton as MaterialIconButton

object Buttons {


    @Composable
    fun Common(
        text: String,
        onclick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Button(
            onClick = onclick,
            modifier = modifier,
            shape = Theme.shape,
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Colors.greenDark
            )
        ) { Text(text) }
    }

    @Composable
    fun IconButton(
        painter: Painter,
        onClick: () -> Unit,
        backgroundColor: Color = Color.Transparent,
        enabled: Boolean = true,
        modifier: Modifier = Modifier
    ) {
        MaterialIconButton(
            onClick = onClick,
            enabled = enabled,
            shape = Theme.shape,
            colors = IconButtonDefaults.iconButtonColors(containerColor = backgroundColor),
            modifier = modifier,
        ) {
            MaterialIcon(
                painter = painter,
                contentDescription = null
            )
        }
    }

    @Composable
    fun BackButton(
        onClick: () -> Unit
    ){
        IconButton(
            painter = Res.drawable.ic_arrow_back_24px.toPainterResource(),
            onClick = onClick
        )
    }


}