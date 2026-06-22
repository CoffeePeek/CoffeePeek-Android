package com.coffeepeek.admin.ui.screen.shop

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.details?.shop?.title ?: "Кофейня",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { Navigator.popBack() }) {
                        Icon(CpIcons.Back, contentDescription = "Назад")
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
                    onToggleFavorite = vm::toggleFavorite,
                    onCheckIn = vm::openCheckInSheet,
                    onCreateReview = vm::openCreateReview,
                    onOpenOnMap = vm::openOnMap,
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
    onToggleFavorite: () -> Unit = {},
    onCheckIn: () -> Unit = {},
    onCreateReview: () -> Unit = {},
    onOpenOnMap: () -> Unit = {},
    onReviewPhotoClick: (String) -> Unit = {},
) {
    val shop = details.shop
    val photos = details.photos.filter { it.isNotBlank() }.ifEmpty {
        listOfNotNull(shop.photoUrl?.takeIf { it.isNotBlank() })
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            ShopHeroImage(
                photos = photos,
                title = shop.title,
                details = details,
                isFavorite = shop.isFavorite,
                isFavoriteLoading = isFavoriteLoading,
                isCheckInLoading = isCheckInLoading,
                isVisited = details.isVisited,
                onToggleFavorite = onToggleFavorite,
                onCheckIn = onCheckIn,
            )
        }

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
                        RatingBlock(
                            rating = shop.rating,
                            reviewCount = shop.reviewCount,
                        )
                    }

                    Spacer(Modifier.height(CpDimens.spacing2))
                    StatusBadges(details = details)

                    shop.priceRange?.let { price ->
                        Spacer(Modifier.height(CpDimens.spacing2))
                        InfoChip("Ценовой диапазон: $price")
                    }

                    details.location?.address?.let { address ->
                        Spacer(Modifier.height(CpDimens.spacing2))
                        LocationRow(address = address)
                    }

                    val lat = details.location?.latitude
                    val lon = details.location?.longitude
                    if (lat != null && lon != null) {
                        Spacer(Modifier.height(CpDimens.spacing2))
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
                    SectionCard(title = "Контакты") {
                        Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
                            contact.phone?.let {
                                ContactRow(
                                    icon = CpIcons.Phone,
                                    text = it,
                                    onClick = { OpenInBrowser.openInBrowser("tel:$it") },
                                )
                            }
                            contact.email?.let {
                                ContactRow(
                                    icon = CpIcons.Email,
                                    text = it,
                                    onClick = { OpenInBrowser.openInBrowser("mailto:$it") },
                                )
                            }
                            contact.website?.let {
                                ContactRow(
                                    icon = CpIcons.Globe,
                                    text = it,
                                    onClick = { OpenInBrowser.openInBrowser(it) },
                                )
                            }
                            contact.instagram?.let {
                                ContactRow(
                                    icon = CpIcons.Favorite,
                                    text = "Instagram: $it",
                                    onClick = { OpenInBrowser.openInBrowser(it) },
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            ReviewsSection(
                reviews = details.reviews,
                reviewCount = shop.reviewCount,
                canCreateReview = details.canCreateReview == true && details.existingReviewId == null,
                hasExistingReview = details.existingReviewId != null,
                onCreateReview = onCreateReview,
                onReviewPhotoClick = onReviewPhotoClick,
            )
        }

        item { Spacer(Modifier.height(CpDimens.spacing6)) }
    }
}

@Composable
private fun ShopHeroImage(
    photos: List<String>,
    title: String,
    details: CoffeeShopDetails,
    isFavorite: Boolean,
    isFavoriteLoading: Boolean,
    isCheckInLoading: Boolean,
    isVisited: Boolean,
    onToggleFavorite: () -> Unit,
    onCheckIn: () -> Unit,
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

        ShopHeroActions(
            isFavorite = isFavorite,
            isFavoriteLoading = isFavoriteLoading,
            isCheckInLoading = isCheckInLoading,
            isVisited = isVisited,
            onToggleFavorite = onToggleFavorite,
            onCheckIn = onCheckIn,
            modifier = Modifier.align(Alignment.TopEnd),
        )

        Box(modifier = Modifier.align(Alignment.BottomStart).padding(CpDimens.spacing3)) {
            StatusBadges(details = details)
        }
    }
}

@Composable
private fun ShopHeroActions(
    isFavorite: Boolean,
    isFavoriteLoading: Boolean,
    isCheckInLoading: Boolean,
    isVisited: Boolean,
    onToggleFavorite: () -> Unit,
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(CpDimens.spacing3),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        HeroIconButton(
            onClick = onToggleFavorite,
            enabled = !isFavoriteLoading,
            isLoading = isFavoriteLoading,
            contentDescription = if (isFavorite) "Убрать из избранного" else "Добавить в избранное",
        ) {
            Icon(
                imageVector = if (isFavorite) CpIcons.FavoriteFilled else CpIcons.Favorite,
                contentDescription = null,
                tint = if (isFavorite) CpColor.Error else Color.White,
            )
        }
        HeroIconButton(
            onClick = onCheckIn,
            enabled = !isCheckInLoading && !isVisited,
            isLoading = isCheckInLoading,
            contentDescription = if (isVisited) "Уже отмечено" else "Чек-ин",
        ) {
            Icon(
                imageVector = CpIcons.AddLocationAlt,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
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
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.45f),
        modifier = Modifier.size(40.dp),
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
        ) {
            if (isLoading) {
                CoffeePeekLoader(
                    size = 18.dp,
                    strokeWidth = 2.dp,
                    color = Color.White,
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
private fun ReviewsSection(
    reviews: List<Review>,
    reviewCount: Int,
    canCreateReview: Boolean,
    hasExistingReview: Boolean,
    onCreateReview: () -> Unit,
    onReviewPhotoClick: (String) -> Unit,
) {
    SectionCard(title = if (reviewCount > 0) "Отзывы ($reviewCount)" else "Отзывы") {
        when {
            canCreateReview -> {
                OutlinedButton(
                    onClick = onCreateReview,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(CpIcons.Review, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(CpDimens.spacing2))
                    Text("Оставить отзыв")
                }
                if (reviews.isNotEmpty()) {
                    Spacer(Modifier.height(CpDimens.spacing3))
                }
            }
            hasExistingReview -> {
                Text(
                    text = "Вы уже оставляли отзыв об этом месте",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (reviews.isNotEmpty()) {
                    Spacer(Modifier.height(CpDimens.spacing3))
                }
            }
        }

        if (reviews.isEmpty()) {
            if (!canCreateReview && !hasExistingReview) {
                Text(
                    text = if (reviewCount > 0) {
                        "Отзывы пока не загружены"
                    } else {
                        "Пока нет отзывов"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3)) {
                reviews.forEach { review ->
                    ReviewCard(review, onReviewPhotoClick)
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
        if (details.canCreateReview == true) ShopBadge("Можно оставить отзыв", CpColor.Success)
        if (details.existingReviewId != null) ShopBadge("Ваш отзыв", MaterialTheme.colorScheme.secondary)
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
private fun ContactRow(
    icon: ImageVector,
    text: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
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
