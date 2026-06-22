package com.coffeepeek.admin.ui.screen.reviews

import com.coffeepeek.admin.ui.icons.CpIcons
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.domain.model.Review
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(vm: MyReviewsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.reviews.size - 3 && state.hasMore && !state.isLoadingMore
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) vm.loadMore()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои отзывы") },
                navigationIcon = {
                    IconButton(onClick = { Navigator.popBack() }) {
                        Icon(CpIcons.Back, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CoffeePeekLoader()
            }
            state.error != null && state.reviews.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error ?: "Ошибка", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(CpDimens.spacing3))
                    Button(onClick = vm::refresh) { Text("Попробовать снова") }
                }
            }
            state.reviews.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Вы ещё не оставляли отзывов", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                state = listState,
                contentPadding = PaddingValues(CpDimens.spacing4),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
            ) {
                items(state.reviews, key = { it.id }) { review ->
                    ReviewListCard(
                        review = review,
                        onClick = { Navigator.navigate(Navigator.Screen.ReviewEdit(review.id)) },
                    )
                }
                if (state.isLoadingMore) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(CpDimens.spacing3), contentAlignment = Alignment.Center) {
                            CoffeePeekLoader(size = CpDimens.loaderButton, strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewListCard(review: Review, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(CpDimens.spacing3)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = review.header.ifBlank { "Без заголовка" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        CpIcons.StarFilled,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        "%.1f".format(review.rating.average),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 2.dp),
                    )
                }
            }
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = CpDimens.spacing2),
                    maxLines = 4,
                )
            }
            if (review.photoUrls.isNotEmpty()) {
                Spacer(Modifier.height(CpDimens.spacing2))
                Row(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1)) {
                    review.photoUrls.take(3).forEach { url ->
                        KamelImage(
                            resource = asyncPainterResource(url),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(CpDimens.radiusSm)),
                        )
                    }
                    if (review.photoUrls.size > 3) {
                        Text(
                            "+${review.photoUrls.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = CpDimens.spacing2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (review.createdAt.isNotBlank()) {
                    Text(
                        text = review.createdAt.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "Редактировать",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
