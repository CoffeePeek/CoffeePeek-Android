package com.coffeepeek.admin.ui.screen.shop

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.maskot_with_book
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeeShopImage
import com.coffeepeek.admin.ui.component.CoffeeShopPlaceholderImage
import com.coffeepeek.admin.utils.CpImage
import com.coffeepeek.admin.utils.currentLocalDayOfWeek
import com.coffeepeek.admin.ui.component.PriceBynRow
import com.coffeepeek.admin.ui.component.PriceBynIcon
import com.coffeepeek.admin.ui.component.priceRangeLevel
import com.coffeepeek.admin.ui.component.FullScreenImageDialog
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.utils.OpenInBrowser
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.CheckIn
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.Review
import com.coffeepeek.domain.model.ReviewRating
import com.coffeepeek.domain.model.ShopContact
import com.coffeepeek.domain.model.ShopMenu
import com.coffeepeek.domain.model.ShopMenuItem
import com.coffeepeek.domain.model.ShopSchedule
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
        state.checkInDraft?.let { draft ->
            CheckInBottomSheet(
                draft = draft,
                isLoading = state.isCheckInLoading,
                onDismiss = vm::dismissCheckInSheet,
                onDraftChange = vm::updateCheckInDraft,
                onSubmit = vm::checkIn,
                placeName = state.details?.shop?.title,
            )
        }
    }

    val details = state.details
    val floatingActionsClearance = 72.dp

    Box(modifier = Modifier.fillMaxSize()) {
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
                details != null -> {
                    ShopDetailContent(
                        details = details,
                        modifier = Modifier.padding(padding),
                        bottomContentPadding = floatingActionsClearance,
                        isFavoriteLoading = state.isFavoriteLoading,
                        onToggleFavorite = vm::toggleFavorite,
                        onShare = vm::shareShop,
                        onOpenOnMap = vm::openOnMap,
                        onCopyPhone = vm::copyPhone,
                        onReportIncorrectData = vm::openReportIncorrectData,
                        onBack = Navigator::popBack,
                        onReviewPhotoClick = { previewImageUrl = it },
                    )
                }
            }
        }

        if (details != null) {
            ShopDetailBottomBar(
                isCheckInLoading = state.isCheckInLoading,
                canOpenRoute = details.location?.latitude != null &&
                    details.location?.longitude != null,
                onRoute = vm::openRoute,
                onReview = vm::openReviewAction,
                onCheckIn = vm::openCheckInSheet,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShopDetailContent(
    details: CoffeeShopDetails,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = CpDimens.spacing4,
    isFavoriteLoading: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onShare: () -> Unit = {},
    onOpenOnMap: () -> Unit = {},
    onCopyPhone: (String) -> Unit = {},
    onReportIncorrectData: () -> Unit = {},
    onBack: () -> Unit = {},
    onReviewPhotoClick: (String) -> Unit = {},
) {
    val shop = details.shop
    val photos = details.photos.filter { it.isNotBlank() }.ifEmpty {
        listOfNotNull(shop.photoUrl?.takeIf { it.isNotBlank() })
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = bottomContentPadding + CpDimens.spacing4),
    ) {
        item {
            ShopHeroImage(
                photos = photos,
                title = shop.title,
                onBack = onBack,
                isFavorite = shop.isFavorite,
                isFavoriteLoading = isFavoriteLoading,
                onToggleFavorite = onToggleFavorite,
                onShare = onShare,
                onPhotoClick = onReviewPhotoClick,
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CpDimens.spacing4)
                    .padding(top = CpDimens.spacing5, bottom = CpDimens.spacing4),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing4),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
                    Text(
                        text = shop.title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            letterSpacing = (-0.75).sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    ShopMetaRow(
                        rating = shop.rating,
                        reviewCount = shop.reviewCount,
                        isNew = details.isNew,
                        isOpen = shop.isOpen,
                        priceRange = shop.priceRange,
                    )
                }
                if (shop.tags.isNotEmpty()) {
                    ShopTagRow(tags = shop.tags)
                }
            }
        }

        details.description?.trim()?.takeIf { it.isNotBlank() }?.let { description ->
            item {
                DescriptionSection(description = description)
            }
        }

        details.menu?.takeIf { menu ->
            groupedPresentItems(menu.items).isNotEmpty() || menu.photos.isNotEmpty()
        }?.let { menu ->
            item {
                MenuSection(
                    menu = menu,
                    onPhotoClick = onReviewPhotoClick,
                )
            }
        }

        if (details.schedules.isNotEmpty()) {
            item {
                CollapsibleScheduleSection(schedules = details.schedules)
            }
        }

        if (
            details.brewMethods.isNotEmpty() ||
            details.coffeeBeans.isNotEmpty() ||
            details.roasters.isNotEmpty() ||
            details.equipment.isNotEmpty()
        ) {
            item {
                CoffeeDetailsSection(
                    brewMethods = details.brewMethods,
                    coffeeBeans = details.coffeeBeans,
                    roasters = details.roasters,
                    equipment = details.equipment,
                    onRoasterClick = {
                        Navigator.navigate(Navigator.Screen.RoasterDetail(it))
                    },
                )
            }
        }

        details.contact?.let { contact ->
            if (contact.hasAny()) {
                item {
                    ContactsSection(
                        contact = contact,
                        onCopyPhone = onCopyPhone,
                    )
                }
            }
        }

        if (details.userCheckIns.isNotEmpty()) {
            item {
                CheckInsSection(
                    checkIns = details.userCheckIns,
                    onPhotoClick = onReviewPhotoClick,
                )
            }
        }

        item {
            ReviewsSection(
                reviews = details.reviews,
                shopTitle = shop.title,
                onReviewPhotoClick = onReviewPhotoClick,
            )
        }

        val address = details.location?.address ?: shop.address
        val lat = details.location?.latitude
        val lon = details.location?.longitude
        if (!address.isNullOrBlank() || (lat != null && lon != null)) {
            item {
                AddressCard(
                    address = address,
                    canOpenMap = lat != null && lon != null,
                    onOpenOnMap = onOpenOnMap,
                )
            }
        }

        item {
            ReportIncorrectDataAction(onClick = onReportIncorrectData)
        }

        item { Spacer(Modifier.height(CpDimens.spacing6)) }
    }
}

@Composable
private fun ShopHeroImage(
    photos: List<String>,
    title: String,
    onBack: () -> Unit,
    isFavorite: Boolean,
    isFavoriteLoading: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onPhotoClick: (String) -> Unit,
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
                CoffeeShopImage(
                    imageUrl = coverUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    placeholderLabelSize = 24.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onPhotoClick(coverUrl) },
                )
            } else {
                CoffeeShopPlaceholderImage(
                    labelSize = 24.sp,
                    contentDescription = "Фото $title отсутствует",
                )
            }
        } else {
            PhotoGallery(
                photos = photos,
                title = title,
                onPhotoClick = onPhotoClick,
            )
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
                onToggleFavorite = onToggleFavorite,
                onShare = onShare,
            )
        }
    }
}

@Composable
private fun HeaderActionButtons(
    isFavorite: Boolean,
    isFavoriteLoading: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
        verticalAlignment = Alignment.CenterVertically,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShopMetaRow(
    rating: Double?,
    reviewCount: Int,
    isNew: Boolean,
    isOpen: Boolean,
    priceRange: String?,
) {
    val statusGreen = Color(0xFF4ADE80)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(CpDimens.radiusSm))
                .background(CpColor.PrimaryTint10)
                .padding(horizontal = CpDimens.spacing3, vertical = CpDimens.spacing1),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
        ) {
            Icon(
                imageVector = CpIcons.StarFilled,
                contentDescription = null,
                tint = CpColor.Primary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "%.1f".format(rating ?: 0.0),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = CpColor.Primary,
            )
        }
        Text(
            text = reviewCountLabel(reviewCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = TextDecoration.Underline,
        )
        if (isNew) {
            StatusBadgeChip(text = "Новая", textColor = statusGreen)
        }
        StatusBadgeChip(
            text = if (isOpen) "Открыта" else "Закрыта",
            textColor = if (isOpen) statusGreen else CpColor.Error,
            backgroundColor = if (isOpen) {
                CpColor.Success.copy(alpha = 0.2f)
            } else {
                CpColor.Error.copy(alpha = 0.2f)
            },
        )
        priceRangeLevel(priceRange)?.let { level ->
            PriceBynRow(level = level, iconSize = 13.dp)
        }
    }
}

@Composable
private fun StatusBadgeChip(
    text: String,
    textColor: Color,
    backgroundColor: Color = CpColor.Success.copy(alpha = 0.2f),
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CpDimens.radiusSm))
            .background(backgroundColor)
            .padding(horizontal = CpDimens.spacing2, vertical = CpDimens.spacing1),
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
            ),
            color = textColor,
        )
    }
}

private fun reviewCountLabel(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val word = when {
        mod100 in 11..14 -> "отзывов"
        mod10 == 1 -> "отзыв"
        mod10 in 2..4 -> "отзыва"
        else -> "отзывов"
    }
    return "$count $word"
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
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(999.dp))
                    .padding(horizontal = CpDimens.spacing3, vertical = CpDimens.spacing1),
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
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
    val currentDay = remember { currentLocalDayOfWeek() }
    val orderedSchedules = remember(schedules) {
        schedules.sortedBy { schedule ->
            if (schedule.dayOfWeek == 0) 7 else schedule.dayOfWeek
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4, vertical = 6.dp),
        shape = RoundedCornerShape(CpDimens.radius2xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(CpDimens.spacing4)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(CpDimens.radiusMd))
                        .background(CpColor.PrimaryTint10),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = CpIcons.Time,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(CpDimens.spacing3))
                Text(
                    text = "Часы работы",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (expanded) CpIcons.ChevronUp else CpIcons.ChevronDown,
                    contentDescription = if (expanded) "Скрыть часы работы" else "Показать часы работы",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }

            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 56.dp, top = CpDimens.spacing2),
                    verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                ) {
                    orderedSchedules.forEach { schedule ->
                        ScheduleRow(
                            schedule = schedule,
                            isCurrentDay = schedule.dayOfWeek == currentDay,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactsSection(
    contact: ShopContact,
    onCopyPhone: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing4),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
    ) {
        SectionTitle("Контакты", barColor = CpColor.GoldWarm)
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            contact.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                PhoneContactPill(
                    phone = phone,
                    onCall = { OpenInBrowser.openInBrowser("tel:$phone") },
                    onCopy = { onCopyPhone(phone) },
                )
            }
            contact.instagram?.let {
                formatInstagramLink(it)?.let { instagram ->
                    ContactPill(
                        icon = CpIcons.Instagram,
                        text = instagramLabel(instagram),
                        onClick = { OpenInBrowser.openInBrowser(instagram.targetUrl) },
                    )
                }
            }
            contact.website?.let {
                formatWebsiteLink(it)?.let { website ->
                    ContactPill(
                        icon = CpIcons.Globe,
                        text = website.displayText,
                        onClick = { OpenInBrowser.openInBrowser(website.targetUrl) },
                    )
                }
            }
            contact.email?.takeIf { it.isNotBlank() }?.let { email ->
                ContactPill(
                    icon = CpIcons.Email,
                    text = email,
                    onClick = { OpenInBrowser.openInBrowser("mailto:$email") },
                )
            }
        }
    }
}

@Composable
private fun ReportIncorrectDataAction(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing2),
        shape = RoundedCornerShape(CpDimens.radiusLg),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
        ),
    ) {
        Icon(
            imageVector = CpIcons.Error,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(CpDimens.spacing2))
        Text("Сообщить о неточности")
    }
}

@Composable
private fun PhoneContactPill(
    phone: String,
    onCall: () -> Unit,
    onCopy: () -> Unit,
) {
    Row(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(999.dp))
            .height(42.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .clickable(onClick = onCall)
                .padding(start = CpDimens.spacing3, end = CpDimens.spacing2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
        ) {
            Icon(
                imageVector = CpIcons.Phone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = phone,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
        IconButton(
            onClick = onCopy,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = CpIcons.Copy,
                contentDescription = "Скопировать номер",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun ReviewsSection(
    reviews: List<Review>,
    shopTitle: String,
    onReviewPhotoClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4)
            .padding(top = CpDimens.spacing6, bottom = CpDimens.spacing3),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing6),
    ) {
        SectionTitle("Отзывы", barColor = CpColor.Primary)
        if (reviews.isEmpty()) {
            EmptyMascotState(
                mascot = Res.drawable.maskot_with_book,
                message = "Станьте первым, кто оценит и оставит отзыв о своём посещении $shopTitle",
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
            ) {
                reviews.forEach { review ->
                    ReviewCard(review, onReviewPhotoClick)
                }
            }
        }
    }
}

@Composable
private fun CheckInsSection(
    checkIns: List<CheckIn>,
    onPhotoClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4)
            .padding(top = CpDimens.spacing6, bottom = CpDimens.spacing3),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
    ) {
        SectionTitle("Мои чекины", barColor = CpColor.Primary)
        checkIns.forEach { checkIn ->
            CheckInCard(checkIn = checkIn, onPhotoClick = onPhotoClick)
        }
    }
}

@Composable
private fun OutlinedContentCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CpDimens.radius2xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(CpDimens.spacing4),
            content = content,
        )
    }
}

@Composable
private fun CheckInCard(
    checkIn: CheckIn,
    onPhotoClick: (String) -> Unit,
) {
    OutlinedContentCard {
        val date = checkIn.visitedAt.ifBlank { checkIn.createdAt }
        if (date.isNotBlank()) {
            Text(
                text = formatReviewDate(date),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (checkIn.note.isNotBlank()) {
            Text(
                text = checkIn.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = CpDimens.spacing1),
            )
        }
        if (checkIn.photoUrls.isNotEmpty()) {
            Spacer(Modifier.height(CpDimens.spacing2))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
                items(checkIn.photoUrls) { url ->
                    CoffeeShopImage(
                        imageUrl = url,
                        contentDescription = "Фото посещения",
                        contentScale = ContentScale.Crop,
                        placeholderLabelSize = 12.sp,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(CpDimens.radiusSm))
                            .clickable { onPhotoClick(url) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
    SectionCard(title = "Описание") {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MenuSection(
    menu: ShopMenu,
    onPhotoClick: (String) -> Unit,
) {
    val capturedLabel = menu.capturedAtUtc?.let(::formatMenuDate)
    val updatedLabel = menu.updatedAtUtc?.let(::formatMenuDate)
    val groups = groupedPresentItems(menu.items)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
    ) {
        SectionTitle("Меню")
        OutlinedContentCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
                verticalAlignment = Alignment.Top,
            ) {
                if (groups.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        groups.forEachIndexed { index, (_, drinks) ->
                            if (index > 0) {
                                Spacer(Modifier.height(CpDimens.spacing2))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                                )
                                Spacer(Modifier.height(CpDimens.spacing2))
                            }
                            drinks.forEach { drink -> MenuDrinkRow(drink) }
                        }
                    }
                } else if (menu.photos.isNotEmpty()) {
                    Spacer(Modifier.weight(1f))
                }

                if (menu.photos.isNotEmpty()) {
                    Column(
                        modifier = Modifier.width(104.dp),
                        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                    ) {
                        menu.photos.forEach { photo ->
                            CoffeeShopImage(
                                imageUrl = photo.fullUrl,
                                contentDescription = "Фотография меню",
                                contentScale = ContentScale.Crop,
                                placeholderLabelSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(104.dp)
                                    .clip(RoundedCornerShape(CpDimens.radiusMd))
                                    .clickable { onPhotoClick(photo.fullUrl) },
                            )
                        }
                        MenuFreshness(capturedLabel, updatedLabel)
                    }
                }
            }

            if (menu.photos.isEmpty()) {
                MenuFreshness(
                    capturedLabel = capturedLabel,
                    updatedLabel = updatedLabel,
                    modifier = Modifier.padding(top = CpDimens.spacing3),
                )
            }
        }
    }
}

@Composable
private fun MenuFreshness(
    capturedLabel: String?,
    updatedLabel: String?,
    modifier: Modifier = Modifier,
) {
    if (capturedLabel.isNullOrBlank() && updatedLabel.isNullOrBlank()) return
    Column(modifier = modifier) {
        if (!capturedLabel.isNullOrBlank()) {
            Text(
                text = "Актуально на $capturedLabel",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!updatedLabel.isNullOrBlank() && updatedLabel != capturedLabel) {
            Text(
                text = "Обновлено $updatedLabel",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MenuDrinkRow(item: ShopMenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CpDimens.spacing1),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.nameRu.ifBlank { item.nameEn.ifBlank { item.slug } },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(end = CpDimens.spacing2),
        )
        item.price?.let { price ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = formatMenuAmount(price),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (item.currency.isBlank() || item.currency.equals("BYN", ignoreCase = true)) {
                    PriceBynIcon(
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Белорусский рубль",
                    )
                } else {
                    Text(
                        text = item.currency,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMascotState(
    mascot: DrawableResource,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CpDimens.spacing4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(mascot),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(120.dp),
        )
        Spacer(Modifier.height(CpDimens.spacing3))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AddressCard(
    address: String?,
    canOpenMap: Boolean,
    onOpenOnMap: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing3),
        shape = RoundedCornerShape(CpDimens.radius2xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(CpDimens.spacing4),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
        ) {
            if (!address.isNullOrBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(CpDimens.radiusLg))
                            .background(CpColor.PrimaryTint10),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = CpIcons.Location,
                            contentDescription = null,
                            tint = CpColor.Primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        text = address,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (canOpenMap) {
                OutlinedButton(
                    onClick = onOpenOnMap,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CpDimens.radiusLg),
                ) {
                    Icon(CpIcons.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(CpDimens.spacing2))
                    Text("Открыть на карте")
                }
            }
        }
    }
}

@Composable
private fun ShopDetailBottomBar(
    isCheckInLoading: Boolean,
    canOpenRoute: Boolean,
    onRoute: () -> Unit,
    onReview: () -> Unit,
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = CpDimens.spacing3, vertical = CpDimens.spacing3),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RouteIconButton(
            enabled = canOpenRoute,
            onClick = onRoute,
        )
        BottomBarAction(
            icon = CpIcons.Review,
            label = "Отзыв",
            onClick = onReview,
            modifier = Modifier.weight(1f),
        )
        BottomBarAction(
            icon = CpIcons.Check,
            label = "Чекин",
            enabled = !isCheckInLoading,
            isLoading = isCheckInLoading,
            onClick = onCheckIn,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RouteIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .size(42.dp)
            .shadow(elevation = 8.dp, shape = shape, clip = false)
            .clip(shape)
            .background(
                if (enabled) CpColor.Primary
                else CpColor.Primary.copy(alpha = 0.38f),
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = CpIcons.Navigation,
            contentDescription = "Маршрут",
            tint = CpColor.DarkTextOnPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun BottomBarAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val tint = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = modifier
            .height(42.dp)
            .shadow(elevation = 8.dp, shape = shape, clip = false)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = enabled && !isLoading, onClick = onClick)
            .padding(horizontal = CpDimens.spacing3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (isLoading) {
            CoffeePeekLoader(size = 16.dp, strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(CpDimens.spacing1))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun scheduleSummary(schedule: ShopSchedule): String = when {
    schedule.isClosed -> "Выходной"
    schedule.intervals.isEmpty() -> "—"
    else -> schedule.intervals.joinToString(", ") { interval ->
        "${formatTime(interval.openTime)}–${formatTime(interval.closeTime)}"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGallery(
    photos: List<String>,
    title: String,
    onPhotoClick: (String) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { photos.size })

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
        ) { page ->
            val photoUrl = photos[page]
            CoffeeShopImage(
                imageUrl = photoUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                placeholderLabelSize = 24.sp,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onPhotoClick(photoUrl) },
            )
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color.Black.copy(alpha = 0.48f),
            tonalElevation = 0.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(CpDimens.spacing3),
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / ${photos.size}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                modifier = Modifier.padding(horizontal = CpDimens.spacing3, vertical = CpDimens.spacing1),
            )
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
private fun ScheduleRow(
    schedule: ShopSchedule,
    isCurrentDay: Boolean,
) {
    val contentColor = if (isCurrentDay) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = shortDayOfWeekLabel(schedule.dayOfWeek),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isCurrentDay) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = contentColor,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = scheduleSummary(schedule),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isCurrentDay) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = contentColor,
        )
    }
}

@Composable
private fun CoffeeDetailsSection(
    brewMethods: List<String>,
    coffeeBeans: List<String>,
    roasters: List<CatalogItem>,
    equipment: List<String>,
    onRoasterClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
    ) {
        SectionTitle("Детали кофе")
        RoasterDetailGroup(roasters, onRoasterClick)
        CatalogDetailGroup(CpIcons.Coffee, "Методы заваривания", brewMethods)
        CatalogDetailGroup(CpIcons.CoffeeBean, "Кофейные зёрна", coffeeBeans)
        CatalogDetailGroup(CpIcons.Settings, "Оборудование", equipment)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoasterDetailGroup(
    items: List<CatalogItem>,
    onRoasterClick: (String) -> Unit,
) {
    if (items.isEmpty()) return
    OutlinedContentCard {
        Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            ) {
                Icon(
                    imageVector = CpIcons.CoffeeBean,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Обжарщики",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            ) {
                items.forEach { item ->
                    InfoChip(
                        text = item.name,
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        textColor = MaterialTheme.colorScheme.primary,
                        onClick = { onRoasterClick(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogDetailGroup(
    icon: ImageVector,
    title: String,
    items: List<String>,
) {
    if (items.isEmpty()) return
    OutlinedContentCard {
        Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
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
        shape = RoundedCornerShape(CpDimens.radius2xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(CpDimens.spacing4)) {
            SectionTitle(title)
            Spacer(Modifier.height(CpDimens.spacing2))
            content()
        }
    }
}

@Composable
private fun InfoChip(
    text: String,
    containerColor: Color = CpColor.GoldWarmSoft,
    textColor: Color = CpColor.GoldWarmHover,
    onClick: (() -> Unit)? = null,
) {
    var modifier = Modifier
        .clip(RoundedCornerShape(CpDimens.radiusSm))
        .background(containerColor)
    if (onClick != null) modifier = modifier.clickable(onClick = onClick)
    Box(
        modifier = modifier
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
private fun SectionTitle(
    title: String,
    barColor: Color = CpColor.GoldWarm,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
    ) {
        Box(
            modifier = Modifier
                .size(width = 6.dp, height = 32.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(barColor),
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
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
    OutlinedContentCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReviewAuthorAvatar(
                avatarUrl = review.avatarUrl,
                username = review.username,
            )
            Spacer(Modifier.width(CpDimens.spacing3))
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
                    CoffeeShopImage(
                        imageUrl = url,
                        contentDescription = "Фото отзыва",
                        contentScale = ContentScale.Crop,
                        placeholderLabelSize = 12.sp,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(CpDimens.radiusSm))
                            .clickable { onPhotoClick(url) },
                    )
                }
            }
        }

    }
}

@Composable
private fun ReviewAuthorAvatar(avatarUrl: String?, username: String) {
    val modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)

    if (!avatarUrl.isNullOrBlank()) {
        CpImage(
            data = avatarUrl,
            modifier = modifier,
            contentDescription = "Фото пользователя ${username.ifBlank { "Пользователь" }}",
        )
    } else {
        Box(
            modifier = modifier
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = username.trim().firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
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

private fun shortDayOfWeekLabel(day: Int): String = when (day) {
    0 -> "Вс"
    1 -> "Пн"
    2 -> "Вт"
    3 -> "Ср"
    4 -> "Чт"
    5 -> "Пт"
    6 -> "Сб"
    else -> "—"
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

private fun formatMenuDate(raw: String): String = formatReviewDate(raw)

private fun formatMenuAmount(price: Double): String {
    val cents = kotlin.math.round(price * 100.0).toLong()
    val whole = cents / 100
    val frac = kotlin.math.abs(cents % 100)
    return "$whole,${frac.toString().padStart(2, '0')}"
}

private fun groupedPresentItems(items: List<ShopMenuItem>): List<Pair<String, List<ShopMenuItem>>> {
    val present = items.filter { it.availability.equals("Present", ignoreCase = true) }
    val grouped = present.groupBy { it.category }
    val order = listOf("Espresso", "Filter")
    val known = order.mapNotNull { category ->
        grouped[category]?.takeIf { it.isNotEmpty() }?.let { menuCategoryTitle(category) to it }
    }
    val rest = grouped
        .filterKeys { it !in order }
        .map { (category, drinks) -> menuCategoryTitle(category) to drinks }
    return known + rest
}

private fun menuCategoryTitle(category: String): String = when (category) {
    "Espresso" -> "Эспрессо"
    "Filter" -> "Фильтр"
    else -> category
}
