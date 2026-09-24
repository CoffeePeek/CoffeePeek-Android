package com.coffeepeek.admin.ui.screen.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.ui.component.PriceBeanSlider
import com.coffeepeek.admin.ui.component.priceLevelHint
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.domain.model.CatalogItem
import kotlinx.coroutines.launch

private const val COLLAPSED_COUNT = 5
private val GroupShape = RoundedCornerShape(12.dp)
private val RowMinHeight = 44.dp
private val RowInset = 16.dp

/** iOS-style filter sheet: slides up, swipe down / back discards, «Готово» applies. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopFiltersScreen(
    state: FeedUiState,
    onDismiss: () -> Unit,
    onApply: (FeedFiltersUi) -> Unit,
) {
    var draft by remember(state.filters) { mutableStateOf(state.filters) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { SheetGrabber() },
    ) {
        SheetNavBar(
            resetEnabled = draft.activeFilterCount > 0,
            onReset = { draft = draft.clearSelections() },
            onDone = {
                onApply(draft)
                hideThen(onDismiss)
            },
        )

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = RowInset)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            GroupSection(
                title = "Цена",
                trailing = priceLevelHint(draft.priceRange) ?: "Любая",
            ) {
                PriceBeanSlider(
                    selected = draft.priceRange,
                    onSelect = { draft = draft.copy(priceRange = it) },
                    showTitle = false,
                    showHint = false,
                    modifier = Modifier.padding(horizontal = RowInset, vertical = 8.dp),
                )
            }
            CheckmarkSection("Обжарщики", state.roasters, draft.roasterIds) {
                draft = draft.copy(roasterIds = draft.roasterIds.toggle(it))
            }
            CheckmarkSection("Зёрна", state.beans, draft.beanIds) {
                draft = draft.copy(beanIds = draft.beanIds.toggle(it))
            }
            CheckmarkSection("Оборудование", state.equipment, draft.equipmentIds) {
                draft = draft.copy(equipmentIds = draft.equipmentIds.toggle(it))
            }
            CheckmarkSection("Метод заваривания", state.brewMethods, draft.brewMethodIds) {
                draft = draft.copy(brewMethodIds = draft.brewMethodIds.toggle(it))
            }
            CheckmarkSection("Особенности", ShopTagGroups.amenityTags(state.shopTags), draft.tagIds) {
                draft = draft.copy(tagIds = draft.tagIds.toggle(it))
            }
        }
    }
}

@Composable
private fun SheetGrabber() {
    Box(
        modifier = Modifier
            .padding(top = 6.dp, bottom = 4.dp)
            .size(width = 36.dp, height = 5.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)),
    )
}

@Composable
private fun SheetNavBar(
    resetEnabled: Boolean,
    onReset: () -> Unit,
    onDone: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 4.dp),
    ) {
        TextButton(
            onClick = onReset,
            enabled = resetEnabled,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Text("Сбросить", fontSize = 17.sp, color = if (resetEnabled) CpColor.Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
        Text(
            text = "Фильтры",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center),
        )
        TextButton(
            onClick = onDone,
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Text("Готово", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = CpColor.Primary)
        }
    }
}

/** Inset grouped list section: footnote header above a rounded card. */
@Composable
private fun GroupSection(
    title: String,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RowInset)
                .padding(bottom = 6.dp),
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) {
                Text(text = trailing, fontSize = 13.sp, color = CpColor.Primary)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(GroupShape)
                .background(MaterialTheme.colorScheme.surface),
            content = content,
        )
    }
}

@Composable
private fun CheckmarkSection(
    title: String,
    items: List<CatalogItem>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (items.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    val collapsible = items.size > COLLAPSED_COUNT
    // Keep selected items visible even when collapsed.
    val visibleItems = if (expanded || !collapsible) items
    else (items.take(COLLAPSED_COUNT) + items.filter { it.id in selectedIds }).distinctBy { it.id }

    GroupSection(
        title = title,
        trailing = selectedIds.count { id -> items.any { it.id == id } }.takeIf { it > 0 }?.let { "Выбрано: $it" },
    ) {
        visibleItems.forEachIndexed { index, item ->
            if (index > 0) RowSeparator()
            CheckmarkRow(
                label = item.name,
                checked = item.id in selectedIds,
                onToggle = { onToggle(item.id) },
            )
        }
        if (collapsible) {
            RowSeparator()
            Text(
                text = if (expanded) "Свернуть" else "Показать все (${items.size})",
                fontSize = 17.sp,
                color = CpColor.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .heightIn(min = RowMinHeight)
                    .padding(horizontal = RowInset, vertical = 11.dp),
            )
        }
    }
}

@Composable
private fun CheckmarkRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
            .heightIn(min = RowMinHeight)
            .padding(horizontal = RowInset, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (checked) {
            Icon(
                imageVector = CpIcons.Check,
                contentDescription = null,
                tint = CpColor.Primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun RowSeparator() {
    HorizontalDivider(
        modifier = Modifier.padding(start = RowInset),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    )
}

private fun Set<String>.toggle(id: String): Set<String> =
    if (contains(id)) this - id else this + id
