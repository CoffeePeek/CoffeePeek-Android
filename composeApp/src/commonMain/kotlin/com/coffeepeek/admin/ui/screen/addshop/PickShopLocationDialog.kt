package com.coffeepeek.admin.ui.screen.addshop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.coffeepeek.admin.location.GeoPoint
import com.coffeepeek.admin.location.PlatformLocation
import com.coffeepeek.admin.map.CoffeeMap
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.domain.model.MapBounds
import kotlinx.coroutines.delay

@Composable
fun PickShopLocationDialog(
    initialPoint: GeoPoint?,
    onConfirm: (latitude: Double, longitude: Double, address: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var cameraCenter by remember(initialPoint) {
        mutableStateOf(initialPoint)
    }
    var cameraTarget by remember(initialPoint) {
        mutableStateOf(initialPoint?.let { it.latitude to it.longitude })
    }
    var myLocationRequestKey by remember { mutableIntStateOf(0) }
    var previewAddress by remember { mutableStateOf<String?>(null) }
    var isResolvingAddress by remember { mutableStateOf(false) }
    var resolveError by remember { mutableStateOf<String?>(null) }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val onConfirmState = rememberUpdatedState(onConfirm)

    LaunchedEffect(cameraCenter) {
        val point = cameraCenter ?: return@LaunchedEffect
        isResolvingAddress = true
        resolveError = null
        delay(350)
        val address = PlatformLocation.reverseGeocode(point.latitude, point.longitude)
        previewAddress = address
        isResolvingAddress = false
        if (address.isNullOrBlank()) {
            resolveError = "Не удалось определить адрес. Подвиньте карту."
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            CoffeeMap(
                shops = emptyList(),
                selectedShopId = null,
                onBoundsChanged = { bounds ->
                    cameraCenter = bounds.center()
                },
                onShopClick = {},
                modifier = Modifier.fillMaxSize(),
                cameraTarget = cameraTarget,
                cameraZoom = 16f,
                onCameraTargetApplied = { cameraTarget = null },
                isDarkTheme = isDarkTheme,
                myLocationRequestKey = myLocationRequestKey,
                onMyLocationFound = { lat, lon ->
                    cameraCenter = GeoPoint(lat, lon)
                },
            )

            Icon(
                imageVector = CpIcons.Location,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .padding(bottom = 20.dp),
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(CpDimens.spacing4)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(CpIcons.Close, contentDescription = "Закрыть")
            }

            IconButton(
                onClick = { myLocationRequestKey += 1 },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(CpDimens.spacing4)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(CpIcons.MyLocation, contentDescription = "Моё местоположение")
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(CpDimens.spacing4),
                shape = RoundedCornerShape(CpDimens.radius2xl),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
            ) {
                Column(modifier = Modifier.padding(CpDimens.spacing4)) {
                    Text(
                        text = "Выберите точку на карте",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(CpDimens.spacing1))
                    Text(
                        text = "Перемещайте карту так, чтобы метка указывала на кофейню",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(CpDimens.spacing3))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                    ) {
                        Icon(
                            CpIcons.Location,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        when {
                            isResolvingAddress -> {
                                CoffeePeekLoader(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(CpDimens.spacing2))
                                Text(
                                    text = "Определяем адрес…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            !previewAddress.isNullOrBlank() -> {
                                Text(
                                    text = previewAddress.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            else -> {
                                Text(
                                    text = resolveError ?: "Адрес появится после перемещения карты",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(CpDimens.spacing4))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Отмена")
                        }
                        Button(
                            onClick = {
                                val point = cameraCenter ?: return@Button
                                val address = previewAddress?.trim().orEmpty()
                                if (address.isBlank()) return@Button
                                onConfirmState.value(point.latitude, point.longitude, address)
                            },
                            enabled = !isResolvingAddress && !previewAddress.isNullOrBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(CpDimens.buttonRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text("Выбрать")
                        }
                    }
                }
            }
        }
    }
}

private fun MapBounds.center(): GeoPoint = GeoPoint(
    latitude = (minLat + maxLat) / 2.0,
    longitude = (minLon + maxLon) / 2.0,
)
