package com.coffeepeek.feature.favorites.impl.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.coffeepeek.core.designsystem.component.AppButton
import com.coffeepeek.core.designsystem.component.CoffeePeekLoader
import com.coffeepeek.core.designsystem.component.CpTopBar
import com.coffeepeek.core.designsystem.icons.CpIcons
import com.coffeepeek.core.designsystem.theme.CpColor
import com.coffeepeek.core.designsystem.theme.CpDimens
import com.coffeepeek.feature.favorites.domain.FavoriteShop
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlin.math.round

@Composable
internal fun FavoritesScreen(
    state: FavoritesUiState,
    onRetry: () -> Unit,
    onRemove: (String) -> Unit,
    onOpenShop: (String) -> Unit,
    onBack: () -> Unit,
    distanceForCoordinates: (Double, Double) -> String? = { _, _ -> null },
) {
    Scaffold(topBar = { CpTopBar("Избранное", "Назад", onBack = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            when {
                state.isLoading && state.shops.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CoffeePeekLoader("Загрузка избранного")
                }
                state.loadFailed && state.shops.isEmpty() -> Message("Не удалось загрузить избранное", onRetry)
                state.shops.isEmpty() -> Message("Пока нет избранных кофеен")
                else -> {
                    if (state.actionFailed || state.loadFailed) {
                        Text("Не удалось обновить избранное", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                        AppButton("Повторить", onRetry, Modifier.padding(horizontal = 16.dp))
                    }
                    LazyColumn(
                        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.shops, key = { it.id }) { shop ->
                            val distance = shop.latitude?.let { latitude ->
                                shop.longitude?.let { longitude -> distanceForCoordinates(latitude, longitude) }
                            }
                            FavoriteCard(shop, shop.id in state.removing, distance,
                                { onOpenShop(shop.id) }, { onRemove(shop.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Message(text: String, onRetry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
            onRetry?.let { AppButton("Повторить", it) }
        }
    }
}

/** Feature-owned rendering: never imports feed implementation or legacy shop aggregates. */
@Composable
private fun FavoriteCard(
    shop: FavoriteShop,
    removing: Boolean,
    distance: String?,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpen),
        shape = RoundedCornerShape(CpDimens.radiusXl),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(2f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                FavoritePhoto(shop.photoUrl, shop.title, Modifier.fillMaxSize())

                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(CpDimens.spacing3),
                    horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    shop.rating?.takeIf { it.isFinite() && it > 0 }?.let { rating ->
                        RatingBadge(rating, shop.reviewCount)
                    }
                    Box(
                        modifier = Modifier.size(36.dp)
                            .clip(RoundedCornerShape(CpDimens.radiusLg))
                            .background(Color.Black.copy(alpha = 0.68f))
                            .clickable(enabled = !removing, role = Role.Button, onClick = onRemove),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            CpIcons.FavoriteFilled,
                            contentDescription = "Удалить из избранного: ${shop.title}",
                            tint = CpColor.Error,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                val logos = shop.roasterPhotoUrls.filter(String::isNotBlank).distinct().take(3)
                if (logos.isNotEmpty()) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(CpDimens.spacing3),
                        horizontalArrangement = Arrangement.spacedBy((-14).dp),
                    ) {
                        logos.forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                KamelImage(
                                    resource = { asyncPainterResource(url) },
                                    contentDescription = "Логотип обжарщика ${index + 1}",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    onLoading = { Icon(CpIcons.Coffee, null, Modifier.size(18.dp)) },
                                    onFailure = { Icon(CpIcons.Coffee, null, Modifier.size(18.dp)) },
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(
                    start = CpDimens.spacing4, top = CpDimens.spacing3,
                    end = CpDimens.spacing4, bottom = CpDimens.spacing4,
                ),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        shop.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    OpenStatusBadge(shop.isOpen, Modifier.padding(start = CpDimens.spacing2))
                }

                if (shop.brewMethods.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1)) {
                        shop.brewMethods.take(2).forEach { method -> InfoChip(method) }
                        if (shop.brewMethods.size > 2) InfoChip("+${shop.brewMethods.size - 2}")
                    }
                }

                val location = listOfNotNull(
                    shop.address?.takeIf(String::isNotBlank)
                        ?: shop.cityName?.takeIf(String::isNotBlank),
                    distance?.let { "$it от вас" },
                ).joinToString(" • ")
                if (location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(CpIcons.Location, null, tint = CpColor.Primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            location,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        shop.priceRange?.takeIf(String::isNotBlank)?.let { price ->
                            Text(price, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        shop.tags.firstOrNull { it.isNotBlank() && it !in shop.brewMethods }?.let { tag ->
                            if (!shop.priceRange.isNullOrBlank()) Text("·", style = MaterialTheme.typography.labelSmall)
                            Text(tag, modifier = Modifier.weight(1f, fill = false),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Icon(CpIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun FavoritePhoto(url: String?, title: String, modifier: Modifier) {
    if (url.isNullOrBlank()) {
        Box(modifier.semantics { contentDescription = "Фото $title отсутствует" },
            contentAlignment = Alignment.Center) {
            Text("COFFEEPEEK", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        KamelImage(
            resource = { asyncPainterResource(url) },
            contentDescription = title,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            onLoading = { FavoritePhoto(null, title, Modifier.fillMaxSize()) },
            onFailure = { FavoritePhoto(null, title, Modifier.fillMaxSize()) },
        )
    }
}

@Composable
private fun RatingBadge(rating: Double, reviewCount: Int) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(CpDimens.radiusLg))
            .background(Color.Black.copy(alpha = 0.68f))
            .padding(horizontal = CpDimens.spacing2, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(CpIcons.StarFilled, null, tint = CpColor.Primary, modifier = Modifier.size(16.dp))
        Text((round(rating * 10) / 10).toString(), color = Color.White,
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        if (reviewCount > 0) {
            Text("($reviewCount)", color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun OpenStatusBadge(isOpen: Boolean, modifier: Modifier = Modifier) {
    val color = if (isOpen) CpColor.Success else MaterialTheme.colorScheme.error
    Row(
        modifier = modifier.clip(RoundedCornerShape(CpDimens.radiusSm))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = CpDimens.spacing2, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(if (isOpen) "ОТКРЫТО" else "ЗАКРЫТО", style = MaterialTheme.typography.labelSmall,
            color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier.widthIn(max = 112.dp)
            .clip(RoundedCornerShape(CpDimens.radiusLg))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(CpDimens.radiusLg))
            .padding(horizontal = CpDimens.spacing2, vertical = 5.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
