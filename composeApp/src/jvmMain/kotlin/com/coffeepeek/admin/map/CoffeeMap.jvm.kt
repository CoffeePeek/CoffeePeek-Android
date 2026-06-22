package com.coffeepeek.admin.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.map_desktop_hint
import coffeepeek.composeapp.generated.resources.map_desktop_unavailable
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun CoffeeMap(
    shops: List<MapShop>,
    selectedShopId: String?,
    onBoundsChanged: (MapBounds) -> Unit,
    onShopClick: (MapShop) -> Unit,
    modifier: Modifier,
    cameraTarget: Pair<Double, Double>?,
    cameraZoom: Float?,
    onCameraTargetApplied: () -> Unit,
    isDarkTheme: Boolean,
    myLocationRequestKey: Int,
    onMyLocationFound: (Double, Double) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = CpIcons.Map,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(32.dp),
                )
            }
            Text(
                text = stringResource(Res.string.map_desktop_unavailable),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.map_desktop_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
