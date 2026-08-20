package com.coffeepeek.admin.ui.screen.shop

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeeShopPlaceholderImage
import com.coffeepeek.admin.ui.component.FullScreenImageDialog
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.utils.OpenInBrowser
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.Review
import com.coffeepeek.domain.model.ReviewRating
import com.coffeepeek.domain.model.ShopContact
import com.coffeepeek.domain.model.ShopSchedule
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailScreen(shopId: String) {
    val vm: ShopDetailViewModel = koinViewModel(parameters = { parametersOf(shopId) })
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            vm.clearActionMessage()
        }
    }

    previewImageUrl?.let { url ->
        FullScreenImageDialog(imageUrl = url, onDismiss = { previewImageUrl = null })
    }

    if (state.showCheckInSheet) {
        CheckInBottomSheet(
            isLoading = state.isCheckInLoading,
            onDismiss = vm::dismissCheckInSheet,
            onSubmit = vm::checkIn,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CoffeePeekLoader()
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
                    isFavoriteLoading = state.isFavoriteLoading,
                    isCheckInLoading = state.isCheckInLoading,
                    showCreateReview = state.isLoggedIn && state.details!!.existingReviewId.isNullOrBlank(),
                    onToggleFavorite = vm::toggleFavorite,
                    onCheckIn = vm::openCheckInSheet,
                    onShare = vm::shareShop,
                    onCreateReview = vm::openCreateReview,
                    onEditReview = vm::openEditReview,
                    onOpenOnMap = vm::openOnMap,
                    onBack = Navigator::popBack,
                    onReviewPhotoClick = { previewImageUrl = it },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShopDetailContent(
    details: CoffeeShopDetails,
    modifier: Modifier = Modifier,
    isFavoriteLoading: Boolean = false,
    isCheckInLoading: Boolean = false,
    showCreateReview: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onCheckIn: () -> Unit = {},
    onShare: () -> Unit = {},
    onCreateReview: () -> Unit = {},
    onEditReview: (String) -> Unit = {},
    onOpenOnMap: () -> Unit = {},
    onBack: () -> Unit = {},
    onReviewPhotoClick: (String) -> Unit = {},
) {
    val shop = details.shop
    val photos = details.photos.filter { it.isNotBlank() }.ifEmpty {
        listOfNotNull(shop.photoUrl?.takeIf { it.isNotBlank() })
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                ShopHeroImage(
                    photos = photos,
                    title = shop.title,
                    onBack = onBack,
                    isFavorite = shop.isFavorite,
                    isFavoriteLoading = isFavoriteLoading,
                    isCheckInLoading = isCheckInLoading,
                    isVisited = details.isVisited,
                    onToggleFavorite = onToggleFavorite,
                    onCheckIn = onCheckIn,
                    onShare = onShare,
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing3),
                ) {
                    Text(
                        text = shop.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(CpDimens.spacing2))
                    ShopMetaRow(
                        rating = shop.rating,
                        reviewCount = shop.reviewCount,
                        isNew = details.isNew,
                        isOpen = shop.isOpen,
                    )
                    if (shop.tags.isNotEmpty()) {
                        Spacer(Modifier.height(CpDimens.spacing2))
                        ShopTagRow(tags = shop.tags)
                    }
                    shop.priceRange?.let { price ->
                        Spacer(Modifier.height(CpDimens.spacing2))
                        InfoChip("Ценовой диапазон: $price")
                    }
                    details.location?.address?.let { address ->
                        Spacer(Modifier.height(CpDimens.spacing3))
                        LocationRow(address = address)
                    }

                    val lat = details.location?.latitude
                    val lon = details.location?.longitude
                    if (lat != null && lon != null) {
                        Spacer(Modifier.height(CpDimens.spacing3))
                        OutlinedButton(
                            onClick = onOpenOnMap,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(CpIcons.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(CpDimens.spacing2))
                            Text("Посмотреть на карте")
                        }
                    }
                }
            }

            details.description?.takeIf { it.isNotBlank() }?.let { description ->
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

            if (details.schedules.isNotEmpty()) {
                item {
                    CollapsibleScheduleSection(schedules = details.schedules)
                }
            }

            catalogSection(title = "Способы заваривания", items = details.brewMethods)
            catalogSection(title = "Кофейные зёрна", items = details.coffeeBeans)
            catalogSection(title = "Обжарщики", items = details.roasters)
            catalogSection(title = "Оборудование", items = details.equipment)

            details.contact?.let { contact ->
                if (contact.hasAny()) {
                    item {
                        ContactsSection(contact = contact)
                    }
                }
            }

            item {
                ReviewsSection(
                    reviews = details.reviews,
                    showCreateReview = showCreateReview,
                    existingReviewId = details.existingReviewId,
                    onCreateReview = onCreateReview,
                    onEditReview = onEditReview,
                    onReviewPhotoClick = onReviewPhotoClick,
                )
            }

            item { Spacer(Modifier.height(CpDimens.spacing6)) }
        }
    }
}

@Composable
private fun ShopHeroImage(
    photos: List<String>,
    title: String,
    onBack: () -> Unit,
    isFavorite: Boolean,
    isFavoriteLoading: Boolean,
    isCheckInLoading: Boolean,
    isVisited: Boolean,
    onToggleFavorite: () -> Unit,
    onCheckIn: () -> Unit,
    onShare: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (photos.size <= 1) {
            val coverUrl = photos.firstOrNull()
            if (!coverUrl.isNullOrBlank()) {
                KamelImage(
                    resource = asyncPainterResource(coverUrl),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                CoffeeShopPlaceholderImage(
                    labelSize = 24.sp,
                    contentDescription = "Фото $title отсутствует",
                )
            }
        } else {
            PhotoGallery(photos = photos, title = title)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CpDimens.spacing3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            HeroIconButton(
                onClick = onBack,
                enabled = true,
                isLoading = false,
                contentDescription = "Назад",
            ) {
                Icon(
                    imageVector = CpIcons.Back,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            HeaderActionButtons(
                isFavorite = isFavorite,
                isFavoriteLoading = isFavoriteLoading,
                isCheckInLoading = isCheckInLoading,
                isVisited = isVisited,
                onToggleFavorite = onToggleFavorite,
                onCheckIn = onCheckIn,
                onShare = onShare,
            )
        }
    }
}

@Composable
private fun HeaderActionButtons(
    isFavorite: Boolean,
    isFavoriteLoading: Boolean,
    isCheckInLoading: Boolean,
    isVisited: Boolean,
    onToggleFavorite: () -> Unit,
    onCheckIn: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onCheckIn,
            enabled = !isCheckInLoading && !isVisited,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(CpDimens.radiusMd),
            contentPadding = PaddingValues(horizontal = CpDimens.spacing3, vertical = CpDimens.spacing2),
        ) {
            if (isCheckInLoading) {
                CoffeePeekLoader(
                    size = 16.dp,
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(
                    imageVector = CpIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(CpDimens.spacing1))
                Text(
                    text = "Чекиниться",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        HeroIconButton(
            onClick = onToggleFavorite,
            enabled = !isFavoriteLoading,
            isLoading = isFavoriteLoading,
            contentDescription = if (isFavorite) "Убрать из избранного" else "Добавить в избранное",
        ) {
            Icon(
                imageVector = if (isFavorite) CpIcons.FavoriteFilled else CpIcons.Favorite,
                contentDescription = null,
                tint = if (isFavorite) CpColor.Error else MaterialTheme.colorScheme.onSurface,
            )
        }
        HeroIconButton(
            onClick = onShare,
            enabled = true,
            isLoading = false,
            contentDescription = "Поделиться",
        ) {
            Icon(
                imageVector = CpIcons.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ShopMetaRow(
    rating: Double?,
    reviewCount: Int,
    isNew: Boolean,
    isOpen: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (rating != null && rating > 0) {
            InfoChip(
                text = "★ %.1f".format(rating),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            text = "$reviewCount отзывов",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = TextDecoration.Underline,
        )
        MetaDot()
        if (isNew) {
            InfoChip(
                "Новая",
                containerColor = CpColor.Success.copy(alpha = 0.2f),
                textColor = CpColor.Success,
            )
            MetaDot()
        }
        if (isOpen) {
            InfoChip("Открыто", containerColor = CpColor.Success.copy(alpha = 0.2f), textColor = CpColor.Success)
        } else {
            InfoChip("Закрыто", containerColor = CpColor.Error.copy(alpha = 0.2f), textColor = CpColor.Error)
        }
    }
}

@Composable
private fun ShopTagRow(tags: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        tags.take(6).forEach { tag ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(999.dp))
                    .padding(horizontal = CpDimens.spacing2, vertical = 4.dp),
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetaDot() {
    Text(
        text = "•",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun HeroIconButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(CpDimens.radiusMd),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        modifier = Modifier.size(42.dp),
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
        ) {
            if (isLoading) {
                CoffeePeekLoader(
                    size = 18.dp,
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                content()
            }
        }
    }
}

@Composable
private fun CollapsibleScheduleSection(schedules: List<ShopSchedule>) {
    var expanded by remember { mutableStateOf(false) }
    val preview = schedules.firstOrNull()?.let { schedule ->
        "${dayOfWeekLabel(schedule.dayOfWeek)}: ${scheduleSummary(schedule)}"
    } ?: "Раскрыть расписание"

    SectionCard(title = "Режим работы") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (!expanded) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = if (expanded) CpIcons.ChevronUp else CpIcons.ChevronDown,
                contentDescription = if (expanded) "Скрыть" else "Показать",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200)),
        ) {
            Column(
                modifier = Modifier.padding(top = CpDimens.spacing2),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            ) {
                schedules.forEach { schedule ->
                    ScheduleRow(schedule)
                }
            }
        }
    }
}

@Composable
private fun ContactsSection(contact: ShopContact) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4, vertical = 6.dp),
        shape = RoundedCornerShape(CpDimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(CpDimens.spacing4),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
        ) {
            SectionTitle("Контакты")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                contact.phone?.let {
                    ContactPill(
                        icon = CpIcons.Phone,
                        text = it,
                        onClick = { OpenInBrowser.openInBrowser("tel:$it") },
                    )
                }
                contact.website?.let {
                    formatWebsiteLink(it)?.let { website ->
                        ContactPill(
                            icon = CpIcons.Globe,
                            text = null,
                            onClick = { OpenInBrowser.openInBrowser(website.targetUrl) },
                        )
                    }
                }
                contact.instagram?.let {
                    formatInstagramLink(it)?.let { instagram ->
                        ContactPill(
                            icon = CpIcons.Profile,
                            text = instagramLabel(instagram),
                            onClick = { OpenInBrowser.openInBrowser(instagram.targetUrl) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewsSection(
    reviews: List<Review>,
    showCreateReview: Boolean,
    existingReviewId: String?,
    onCreateReview: () -> Unit,
    onEditReview: (String) -> Unit,
    onReviewPhotoClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing3),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle("Отзывы клиентов")
            when {
                showCreateReview -> {
                    Button(
                        onClick = onCreateReview,
                        shape = RoundedCornerShape(CpDimens.radiusMd),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text("Написать отзыв", style = MaterialTheme.typography.labelLarge)
                    }
                }
                !existingReviewId.isNullOrBlank() -> {
                    OutlinedButton(
                        onClick = { onEditReview(existingReviewId) },
                        shape = RoundedCornerShape(CpDimens.radiusMd),
                    ) {
                        Text("Редактировать отзыв", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        if (reviews.isEmpty()) {
            Text(
                text = "Пока нет отзывов",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
            ) {
                reviews.forEachIndexed { index, review ->
                    ReviewCard(review, onReviewPhotoClick)
                    if (index < reviews.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

private fun scheduleSummary(schedule: ShopSchedule): String = when {
    schedule.isClosed -> "Выходной"
    schedule.intervals.isEmpty() -> "—"
    else -> schedule.intervals.joinToString(", ") { interval ->
        "${formatTime(interval.openTime)}–${formatTime(interval.closeTime)}"
    }
}

@Composable
private fun PhotoGallery(photos: List<String>, title: String) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(photos) { url ->
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .height(240.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                KamelImage(
                    resource = asyncPainterResource(url),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusBadges(details: CoffeeShopDetails) {
    val shop = details.shop
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        ShopBadge(
            text = if (shop.isOpen) "Открыто" else "Закрыто",
            color = if (shop.isOpen) CpColor.Success else MaterialTheme.colorScheme.error,
        )
        if (details.isNew) ShopBadge("Новое", MaterialTheme.colorScheme.primary)
        if (details.isVisited) ShopBadge("Посещено", MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun RatingBlock(rating: Double?, reviewCount: Int) {
    Column(horizontalAlignment = Alignment.End) {
        if (rating != null && rating > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    CpIcons.StarFilled,
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
        if (reviewCount > 0) {
            Text(
                text = "$reviewCount отзывов",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LocationRow(address: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            CpIcons.Location,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = address,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ScheduleRow(schedule: ShopSchedule) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                CpIcons.Time,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(CpDimens.spacing1))
            Text(
                text = dayOfWeekLabel(schedule.dayOfWeek),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(120.dp),
            )
        }
        Text(
            text = when {
                schedule.isClosed -> "Выходной"
                schedule.intervals.isEmpty() -> "—"
                else -> schedule.intervals.joinToString(", ") { interval ->
                    "${formatTime(interval.openTime)}–${formatTime(interval.closeTime)}"
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun LazyListScope.catalogSection(title: String, items: List<String>) {
    if (items.isEmpty()) return
    item {
        SectionCard(title = title) {
            TagFlow(items = items)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagFlow(items: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        items.forEach { item ->
            InfoChip(item)
        }
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
private fun InfoChip(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CpDimens.radiusSm))
            .background(containerColor)
            .padding(horizontal = CpDimens.spacing2, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
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
private fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ContactPill(
    icon: ImageVector,
    text: String?,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp)),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CpDimens.spacing2, vertical = CpDimens.spacing1),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun instagramLabel(link: ExternalLink): String {
    val handle = link.targetUrl
        .substringAfter("instagram.com/", "")
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
        .trim()
    if (handle.isBlank()) return link.displayText
    return "@$handle"
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    text: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
            color = if (onClick != null) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
private fun ReviewCard(review: Review, onPhotoClick: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.username.ifBlank { "Пользователь" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (review.createdAt.isNotBlank()) {
                    Text(
                        text = formatReviewDate(review.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    CpIcons.StarFilled,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = "%.1f".format(review.rating.average),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        ReviewRatingBreakdown(review.rating)

        if (review.header.isNotBlank()) {
            Text(
                text = review.header,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = CpDimens.spacing1),
            )
        }
        if (review.comment.isNotBlank()) {
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        if (review.photoUrls.isNotEmpty()) {
            Spacer(Modifier.height(CpDimens.spacing2))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
                items(review.photoUrls) { url ->
                    KamelImage(
                        resource = asyncPainterResource(url),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(CpDimens.radiusSm))
                            .clickable { onPhotoClick(url) },
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = CpDimens.spacing2),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun ReviewRatingBreakdown(rating: ReviewRating) {
    if (rating.place == 0 && rating.service == 0 && rating.coffee == 0) return
    Spacer(Modifier.height(CpDimens.spacing1))
    Row(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing3)) {
        RatingPill("Место", rating.place)
        RatingPill("Сервис", rating.service)
        RatingPill("Кофе", rating.coffee)
    }
}

@Composable
private fun RatingPill(label: String, value: Int) {
    if (value <= 0) return
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(CpDimens.radiusSm))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = CpDimens.spacing2, vertical = 2.dp),
    )
}

private fun ShopContact.hasAny(): Boolean =
    listOf(phone, email, website, instagram).any { !it.isNullOrBlank() }

private data class ExternalLink(
    val displayText: String,
    val targetUrl: String,
)

private fun formatWebsiteLink(raw: String): ExternalLink? {
    val value = raw.trim().takeIf { it.isNotBlank() } ?: return null
    val targetUrl = ensureHttpScheme(value)
    return ExternalLink(
        displayText = prettyLinkText(targetUrl),
        targetUrl = targetUrl,
    )
}

private fun formatInstagramLink(raw: String): ExternalLink? {
    val value = raw.trim().takeIf { it.isNotBlank() } ?: return null
    val handleFromUrl = extractInstagramHandleFromUrl(value)
    val handle = (handleFromUrl ?: value.removePrefix("@"))
        .trim()
        .trim('/')
        .substringBefore('?')
        .substringBefore('#')

    if (handle.isNotBlank() && handle.matches("^[A-Za-z0-9._]{1,30}$".toRegex())) {
        val targetUrl = "https://instagram.com/$handle"
        return ExternalLink(
            displayText = "instagram.com/$handle",
            targetUrl = targetUrl,
        )
    }

    val targetUrl = ensureHttpScheme(value)
    return ExternalLink(
        displayText = prettyLinkText(targetUrl),
        targetUrl = targetUrl,
    )
}

private fun extractInstagramHandleFromUrl(raw: String): String? {
    val value = raw.trim()
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
    if (!value.startsWith("instagram.com/", ignoreCase = true)) return null
    return value
        .substringAfter("instagram.com/", "")
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
        .ifBlank { null }
}

private fun ensureHttpScheme(value: String): String {
    return if (value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true)) {
        value
    } else {
        "https://$value"
    }
}

private fun prettyLinkText(value: String): String {
    return value
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .substringBefore('?')
        .substringBefore('#')
        .trimEnd('/')
        .ifBlank { value }
}

private fun dayOfWeekLabel(day: Int): String = when (day) {
    0 -> "Воскресенье"
    1 -> "Понедельник"
    2 -> "Вторник"
    3 -> "Среда"
    4 -> "Четверг"
    5 -> "Пятница"
    6 -> "Суббота"
    else -> "День $day"
}

private fun formatTime(raw: String): String {
    if (raw.isBlank()) return raw
    return raw.split(":").take(2).joinToString(":")
}

private fun formatReviewDate(raw: String): String {
    val datePart = raw.substringBefore('T').ifBlank { raw }
    val parts = datePart.split('-')
    if (parts.size != 3) return datePart
    return "${parts[2]}.${parts[1]}.${parts[0]}"
}
