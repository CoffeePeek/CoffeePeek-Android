package com.coffeepeek.admin.ui.screen.map

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coffeepeek.admin.map.CoffeeMap
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeeShopPlaceholderImage
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.MapShop
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MapScreen(vm: MapViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val pendingFocus by Navigator.pendingMapFocus.collectAsState()
    val pendingFocusShop = pendingFocus?.let { focus ->
        MapShop(
            id = focus.shopId,
            title = focus.title,
            latitude = focus.latitude,
            longitude = focus.longitude,
        )
    }
    val mapShops = pendingFocusShop?.let { shop ->
        (state.shops + shop).distinctBy { it.id }
    } ?: state.shops
    val selectedShopId = state.selectedShop?.id ?: pendingFocus?.shopId
    val cameraTarget = state.cameraTarget
        ?: pendingFocus?.let { it.latitude to it.longitude }
    val cameraZoom = state.cameraZoom ?: if (pendingFocus != null) 16f else null
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    LaunchedEffect(pendingFocus) {
        pendingFocus?.let { focus ->
            vm.focusOnShop(focus)
            Navigator.consumeMapFocus()
        }
    }

    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = vm::clearError,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Ошибка", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    err,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = vm::clearError) {
                    Text("Понятно", style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = RoundedCornerShape(CpDimens.radius2xl),
        )
    }

    Box(Modifier.fillMaxSize()) {
        CoffeeMap(
            shops = mapShops,
            selectedShopId = selectedShopId,
            onBoundsChanged = vm::onBoundsChanged,
            onShopClick = vm::onShopSelected,
            modifier = Modifier.fillMaxSize(),
            cameraTarget = cameraTarget,
            cameraZoom = cameraZoom,
            onCameraTargetApplied = vm::onCameraTargetApplied,
            isDarkTheme = isDarkTheme,
        )

        if (state.isLoading) {
            CoffeePeekLoader(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = CpDimens.spacing4),
                strokeWidth = 2.dp,
            )
        }

        state.selectedShop?.let { shop ->
            MapShopBottomSheet(
                shop = shop,
                details = state.selectedShopDetails,
                isLoadingDetails = state.isLoadingShopDetails,
                onOpen = { Navigator.navigate(Navigator.Screen.ShopDetail(shop.id)) },
                onDismiss = vm::clearSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing4),
            )
        }
    }
}

@Composable
private fun MapShopBottomSheet(
    shop: MapShop,
    details: CoffeeShopDetails?,
    isLoadingDetails: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoUrl = details?.photos?.firstOrNull() ?: details?.shop?.photoUrl
    val rating = details?.shop?.rating
    val reviewCount = details?.shop?.reviewCount ?: 0
    val hours = details?.schedules?.let { formatMapHoursSummary(it) }
    val isOpen = details?.shop?.isOpen == true

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(CpDimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CpDimens.spacing3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(CpDimens.radiusMd))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    !photoUrl.isNullOrBlank() -> {
                        KamelImage(
                            resource = asyncPainterResource(photoUrl),
                            contentDescription = shop.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    isLoadingDetails -> {
                        CoffeePeekLoader(
                            size = CpDimens.loaderButton,
                            strokeWidth = 2.dp,
                        )
                    }
                    else -> {
                        CoffeeShopPlaceholderImage(
                            labelSize = 7.sp,
                            contentDescription = "Фото ${shop.title} отсутствует",
                        )
                    }
                }
            }

            Spacer(Modifier.width(CpDimens.spacing3))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (rating != null && rating > 0) {
                        Icon(
                            CpIcons.StarFilled,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(rating),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (reviewCount > 0) {
                            Text(
                                text = " ($reviewCount)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Text(
                            text = if (reviewCount > 0) "$reviewCount отзывов" else "Нет отзывов",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    text = shop.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )

                when {
                    hours != null -> {
                        Text(
                            text = buildString {
                                append(if (isOpen) "Открыто" else "Закрыто")
                                append(" · ")
                                append(hours)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    isLoadingDetails -> {
                        Text(
                            text = "Загрузка…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = CpIcons.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
