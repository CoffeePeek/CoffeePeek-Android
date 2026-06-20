package com.coffeepeek.admin.ui.screen.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.Review
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailScreen(shopId: String) {
    val vm: ShopDetailViewModel = koinViewModel(parameters = { parametersOf(shopId) })
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.details?.shop?.title ?: "Кофейня",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { Navigator.popBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error ?: "Ошибка",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(CpDimens.spacing3))
                        Button(
                            onClick = vm::load,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) { Text("Повторить") }
                    }
                }
            }
            state.details != null -> {
                ShopDetailContent(
                    details = state.details!!,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun ShopDetailContent(details: CoffeeShopDetails, modifier: Modifier = Modifier) {
    val shop = details.shop

    LazyColumn(modifier = modifier.fillMaxSize()) {

        // ── Обложка ───────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                val coverUrl = details.photos.firstOrNull() ?: shop.photoUrl
                if (!coverUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(coverUrl),
                        contentDescription = shop.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                startY = 100f,
                            )
                        )
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomStart).padding(CpDimens.spacing3),
                    horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
                ) {
                    if (shop.isOpen) ShopBadge("Открыто", CpColor.Success)
                    if (details.isVisited) ShopBadge("Посещено", MaterialTheme.colorScheme.primary)
                }
            }
        }

        // ── Карточка с основной информацией ───────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CpDimens.spacing4)
                    .offset(y = (-16).dp),
                shape = RoundedCornerShape(CpDimens.cardRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(CpDimens.spacing4)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = shop.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            val rating = shop.rating
                            if (rating != null && rating > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "%.1f".format(rating),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                            if (shop.reviewCount > 0) {
                                Text(
                                    text = "${shop.reviewCount} отзывов",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    val priceRange = shop.priceRange
                    if (!priceRange.isNullOrBlank()) {
                        Spacer(Modifier.height(CpDimens.spacing2))
                        InfoChip(priceRange)
                    }

                    val detailAddress = details.address
                    if (!detailAddress.isNullOrBlank()) {
                        Spacer(Modifier.height(CpDimens.spacing2))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = detailAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // ── Описание ──────────────────────────────────────────────────────────
        val description = details.description
        if (!description.isNullOrBlank()) {
            item {
                SectionCard(title = "О заведении") {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // ── Особенности (теги) ────────────────────────────────────────────────
        val allTags = buildList {
            details.brewMethods.forEach { add("☕ $it") }
            details.coffeeBeans.forEach { add("🫘 $it") }
            details.roasters.forEach { add("🔥 $it") }
            details.equipment.forEach { add("⚙️ $it") }
        }
        if (allTags.isNotEmpty()) {
            item {
                SectionCard(title = "Особенности") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
                        items(allTags) { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(CpDimens.radiusSm))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = CpDimens.spacing2 + 2.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Контакты ──────────────────────────────────────────────────────────
        val contact = details.contact
        if (contact != null && listOf(contact.phone, contact.email, contact.website, contact.instagram).any { !it.isNullOrBlank() }) {
            item {
                SectionCard(title = "Контакты") {
                    Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
                        contact.phone?.let { ContactRow(Icons.Outlined.Phone, it) }
                        contact.email?.let { ContactRow(Icons.Outlined.Email, it) }
                        contact.website?.let { ContactRow(Icons.Outlined.Language, it) }
                        contact.instagram?.let { ContactRow(Icons.Outlined.Favorite, "Instagram: $it") }
                    }
                }
            }
        }

        // ── Отзывы ────────────────────────────────────────────────────────────
        if (details.reviews.isNotEmpty()) {
            item {
                SectionCard(title = "Отзывы (${details.reviews.size})") {
                    Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3)) {
                        details.reviews.take(5).forEach { review -> ReviewCard(review) }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(CpDimens.spacing6)) }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4, vertical = 6.dp),
        shape = RoundedCornerShape(CpDimens.radiusMd),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(CpDimens.spacing4)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = CpDimens.spacing2),
            )
            content()
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CpDimens.radiusSm))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = CpDimens.spacing2, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ShopBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CpDimens.radiusSm))
            .background(color.copy(alpha = 0.9f))
            .padding(horizontal = CpDimens.spacing2, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ContactRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(CpDimens.spacing2))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = review.username,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = "%.1f".format(review.rating),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (review.header.isNotBlank()) {
            Text(
                text = review.header,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (review.comment.isNotBlank()) {
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 4,
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = CpDimens.spacing2),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
