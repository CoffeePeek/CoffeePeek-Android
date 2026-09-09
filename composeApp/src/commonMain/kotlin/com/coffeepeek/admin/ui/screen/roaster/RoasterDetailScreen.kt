package com.coffeepeek.admin.ui.screen.roaster

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.component.CoffeeShopImage
import com.coffeepeek.admin.ui.component.CoffeeShopPlaceholderImage
import com.coffeepeek.admin.ui.component.FullScreenImageDialog
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.admin.utils.OpenInBrowser
import com.coffeepeek.domain.model.RoasterDetails
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RoasterDetailScreen(roasterId: String) {
    val vm: RoasterDetailViewModel = koinViewModel(parameters = { parametersOf(roasterId) })
    val state by vm.state.collectAsState()
    var previewUrl by remember { mutableStateOf<String?>(null) }

    previewUrl?.let { url ->
        FullScreenImageDialog(imageUrl = url, onDismiss = { previewUrl = null })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CoffeePeekLoader() }

            state.error != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(CpDimens.spacing4),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
                ) {
                    Text(
                        text = state.error ?: "Ошибка загрузки",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = vm::load) { Text("Повторить") }
                    OutlinedButton(onClick = Navigator::popBack) { Text("Назад") }
                }
            }

            state.details != null -> RoasterContent(
                details = state.details!!,
                onBack = Navigator::popBack,
                onPhotoClick = { previewUrl = it },
                onShopClick = { Navigator.navigate(Navigator.Screen.ShopDetail(it)) },
            )
        }
    }
}

@Composable
private fun RoasterContent(
    details: RoasterDetails,
    onBack: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onShopClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = CpDimens.spacing6),
    ) {
        item {
            RoasterHero(
                details = details,
                onBack = onBack,
                onPhotoClick = onPhotoClick,
            )
        }
        item {
            Column(
                modifier = Modifier.padding(CpDimens.spacing4),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing4),
            ) {
                Text(
                    text = details.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )

                details.about?.takeIf { it.isNotBlank() }?.let { about ->
                    RoasterSection(title = "Об обжарщике") {
                        Text(
                            text = about,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                details.location?.let { location ->
                    RoasterSection(title = "Адрес") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                        ) {
                            Icon(
                                CpIcons.Location,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(location.address, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                val instagram = details.contact?.instagramLink
                val website = details.contact?.siteLink
                if (!instagram.isNullOrBlank() || !website.isNullOrBlank()) {
                    RoasterSection(title = "Ссылки") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                        ) {
                            if (!instagram.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = { OpenInBrowser.openInBrowser(normalizeExternalUrl(instagram)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(CpDimens.buttonRadius),
                                ) {
                                    Icon(CpIcons.Instagram, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(CpDimens.spacing1))
                                    Text("Instagram", maxLines = 1)
                                }
                            }
                            if (!website.isNullOrBlank()) {
                                Button(
                                    onClick = { OpenInBrowser.openInBrowser(normalizeExternalUrl(website)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(CpDimens.buttonRadius),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                ) {
                                    Icon(CpIcons.Globe, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(CpDimens.spacing1))
                                    Text("Сайт", maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Где используют",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing2),
            )
        }

        if (details.shops.isEmpty()) {
            item {
                Text(
                    text = "Пока нет кофеен, которые указали этого обжарщика.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing3),
                )
            }
        } else {
            items(details.shops, key = { it.id }) { shop ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing1)
                        .clickable { onShopClick(shop.id) },
                    shape = RoundedCornerShape(CpDimens.cardRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(CpDimens.spacing4),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = CpIcons.Coffee,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = shop.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(horizontal = CpDimens.spacing3),
                        )
                        Icon(
                            imageVector = CpIcons.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoasterHero(
    details: RoasterDetails,
    onBack: () -> Unit,
    onPhotoClick: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (details.photos.isEmpty()) {
            CoffeeShopPlaceholderImage(
                labelSize = 24.sp,
                contentDescription = "Фотографии ${details.name} отсутствуют",
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { details.photos.size })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val photo = details.photos[page]
                CoffeeShopImage(
                    imageUrl = photo.fullUrl,
                    contentDescription = details.name,
                    contentScale = ContentScale.Crop,
                    placeholderLabelSize = 24.sp,
                    modifier = Modifier.fillMaxSize().clickable { onPhotoClick(photo.fullUrl) },
                )
            }
            if (details.photos.size > 1) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(CpDimens.spacing3),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${details.photos.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = CpDimens.spacing3, vertical = CpDimens.spacing1),
                    )
                }
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(CpDimens.spacing2)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape),
        ) {
            Icon(CpIcons.Back, contentDescription = "Назад", tint = Color.White)
        }
    }
}

@Composable
private fun RoasterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}

private fun normalizeExternalUrl(value: String): String {
    val trimmed = value.trim()
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        "https://$trimmed"
    }
}
