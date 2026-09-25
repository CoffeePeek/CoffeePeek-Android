package com.coffeepeek.admin.ui.screen.contributions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.di.platformViewModel
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.component.CpTopBar
import com.coffeepeek.admin.ui.component.ReviewDisplayCard
import com.coffeepeek.admin.ui.screen.review.EditReviewBottomSheet
import com.coffeepeek.domain.model.ModerationStatus
import org.koin.core.parameter.parametersOf

@Composable
fun MyContributionsScreen(kind: ContributionKind) {
    val vm: MyContributionsViewModel = platformViewModel(
        key = "contributions-$kind",
        parameters = { parametersOf(kind) },
    )
    val state by vm.state.collectAsState()
    var editingReviewId by remember { mutableStateOf<String?>(null) }
    var selectedName by rememberSaveable { mutableStateOf<String?>(null) }

    editingReviewId?.let { reviewId ->
        EditReviewBottomSheet(
            reviewId = reviewId,
            placeName = null,
            onDismiss = { editingReviewId = null },
            onSaved = {
                editingReviewId = null
                vm.refresh()
            },
        )
    }

    Scaffold(
        topBar = { CpTopBar(kind.title) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val tabs = state.visibleTabs
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CoffeePeekLoader()
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error ?: "Ошибка", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(CpDimens.spacing3))
                    Button(
                        onClick = vm::refresh,
                        modifier = Modifier.height(CpDimens.buttonHeight),
                        shape = RoundedCornerShape(percent = 50),
                    ) { Text("Попробовать снова") }
                }
            }
            tabs.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(kind.emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> Column(Modifier.fillMaxSize().padding(padding)) {
                val selected = tabs.firstOrNull { it.name == selectedName } ?: tabs.first()
                StatusSegmentedControl(
                    tabs = tabs,
                    selected = selected,
                    counts = state.tabs.mapValues { it.value.totalCount },
                    onSelected = { selectedName = it.name },
                )
                key(selected) {
                    ContributionList(
                        tab = state.tabs[selected] ?: ContributionTab(),
                        onLoadMore = { vm.loadMore(selected) },
                        onEditReview = { editingReviewId = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusSegmentedControl(
    tabs: List<ModerationStatus>,
    selected: ModerationStatus,
    counts: Map<ModerationStatus, Int>,
    onSelected: (ModerationStatus) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing2)
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(CpDimens.radiusSm))
            .padding(2.dp)
            .selectableGroup(),
    ) {
        tabs.forEach { status ->
            val isSelected = status == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                    )
                    .then(
                        if (isSelected) Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(6.dp),
                        ) else Modifier,
                    )
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onSelected(status) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${status.tabTitle()} · ${counts[status] ?: 0}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ContributionList(
    tab: ContributionTab,
    onLoadMore: () -> Unit,
    onEditReview: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(tab) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= tab.items.size - 3 && tab.hasMore && !tab.isLoadingMore
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(CpDimens.spacing4),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
    ) {
        items(tab.items, key = { it.id }) { item ->
            if (item.review != null) {
                Column {
                    ReviewDisplayCard(
                        review = item.review,
                        onEditClick = if (item.editable) ({ onEditReview(item.review.id) }) else null,
                    )
                    item.rejectedReason?.let { RejectedReason(it) }
                }
            } else {
                ContributionCard(item)
            }
        }
        if (tab.isLoadingMore) {
            item {
                Box(Modifier.fillMaxWidth().padding(CpDimens.spacing3), contentAlignment = Alignment.Center) {
                    CoffeePeekLoader(size = CpDimens.loaderButton, strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
private fun ContributionCard(item: ContributionItem) {
    val target = item.target
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (target != null) Modifier.clickable { Navigator.navigate(target) } else Modifier),
    ) {
        Column(modifier = Modifier.padding(CpDimens.spacing4)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!item.subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            item.rejectedReason?.let { RejectedReason(it) }
        }
    }
}

@Composable
private fun RejectedReason(reason: String) {
    if (reason.isBlank()) return
    Text(
        text = "Причина отклонения: $reason",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(top = CpDimens.spacing2),
    )
}

private fun ModerationStatus.tabTitle() = when (this) {
    ModerationStatus.Approved -> "Опубликовано"
    ModerationStatus.Pending -> "На модерации"
    ModerationStatus.Rejected -> "Отклонено"
}
