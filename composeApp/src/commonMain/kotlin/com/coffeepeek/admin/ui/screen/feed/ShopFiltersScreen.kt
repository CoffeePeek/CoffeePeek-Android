package com.coffeepeek.admin.ui.screen.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.component.PriceBeanSlider
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.admin.ui.model.COFFEE_FOCUS_OPTIONS
import com.coffeepeek.domain.model.CatalogItem

private const val COLLAPSED_COUNT = 3

@Composable
fun ShopFiltersScreen(
    state: FeedUiState,
    onDismiss: () -> Unit,
    onApply: (FeedFiltersUi) -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    var draft by remember(state.filters) { mutableStateOf(state.filters) }

    LaunchedEffect(Unit) { visible = true }

    fun dismissAnimated() {
        visible = false
    }

    Dialog(
        onDismissRequest = { dismissAnimated() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)),
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CpDimens.spacing2, vertical = CpDimens.spacing2),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = { dismissAnimated() }) {
                                Icon(
                                    imageVector = CpIcons.Back,
                                    contentDescription = "Назад",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Text(
                                text = "Фильтры",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                onClick = { draft = FeedFiltersUi() },
                                enabled = draft != FeedFiltersUi(),
                            ) {
                                Text("Сбросить")
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing3),
                            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing4),
                        ) {
                            FilterPriceSection(
                                selected = draft.priceRange,
                                onSelect = { value ->
                                    draft = draft.copy(priceRange = value)
                                },
                            )
                            // Specialty / Кофейня / Кафе — чипы как типы в основном поиске
                            CoffeeFocusChips(
                                selectedId = draft.coffeeFocus,
                                onSelect = { focusId ->
                                    draft = draft.copy(
                                        coffeeFocus = if (draft.coffeeFocus == focusId) null else focusId,
                                    )
                                },
                            )
                            ExpandableCheckboxSection(
                                title = "Обжарщики",
                                items = state.roasters,
                                selectedIds = draft.roasterIds,
                                onToggle = { id ->
                                    draft = draft.copy(roasterIds = draft.roasterIds.toggle(id))
                                },
                            )
                            ExpandableCheckboxSection(
                                title = "Зёрна",
                                items = state.beans,
                                selectedIds = draft.beanIds,
                                onToggle = { id ->
                                    draft = draft.copy(beanIds = draft.beanIds.toggle(id))
                                },
                            )
                            ExpandableCheckboxSection(
                                title = "Оборудование",
                                items = state.equipment,
                                selectedIds = draft.equipmentIds,
                                onToggle = { id ->
                                    draft = draft.copy(equipmentIds = draft.equipmentIds.toggle(id))
                                },
                            )
                            ExpandableCheckboxSection(
                                title = "Метод заваривания",
                                items = state.brewMethods,
                                selectedIds = draft.brewMethodIds,
                                onToggle = { id ->
                                    draft = draft.copy(brewMethodIds = draft.brewMethodIds.toggle(id))
                                },
                            )
                            ExpandableCheckboxSection(
                                title = "Особенности",
                                items = ShopTagGroups.amenityTags(state.shopTags),
                                selectedIds = draft.tagIds,
                                onToggle = { id ->
                                    draft = draft.copy(tagIds = draft.tagIds.toggle(id))
                                },
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Button(
                            onClick = {
                                onApply(draft)
                                dismissAnimated()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(CpDimens.spacing4),
                            shape = RoundedCornerShape(CpDimens.radiusLg),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CpColor.Primary,
                                contentColor = CpColor.DarkTextOnPrimary,
                            ),
                        ) {
                            Text(
                                text = "Применить",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }
            }
        }

        LaunchedEffect(visible) {
            if (!visible) {
                // Allow exit animation to play, then dismiss dialog host.
                kotlinx.coroutines.delay(180)
                onDismiss()
            }
        }
    }
}

@Composable
private fun FilterPriceSection(
    selected: Int?,
    onSelect: (Int?) -> Unit,
) {
    PriceBeanSlider(
        selected = selected,
        onSelect = onSelect,
    )
}

/** Same chip row style as venue types on the main feed search. */
@Composable
private fun CoffeeFocusChips(
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        COFFEE_FOCUS_OPTIONS.forEach { option ->
            FilterChip(
                selected = selectedId == option.id,
                onClick = { onSelect(option.id) },
                label = {
                    Text(option.label, style = MaterialTheme.typography.labelSmall)
                },
            )
        }
    }
}

@Composable
private fun ExpandableCheckboxSection(
    title: String,
    items: List<CatalogItem>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (items.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    val visibleItems = if (expanded || items.size <= COLLAPSED_COUNT) {
        items
    } else {
        items.take(COLLAPSED_COUNT)
    }
    val hiddenCount = (items.size - COLLAPSED_COUNT).coerceAtLeast(0)

    Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing1)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        visibleItems.forEach { item ->
            FilterCheckboxRow(
                label = item.name,
                checked = item.id in selectedIds,
                onClick = { onToggle(item.id) },
            )
        }
        if (items.size > COLLAPSED_COUNT) {
            TextButton(
                onClick = { expanded = !expanded },
            ) {
                Text(
                    text = if (expanded) "Свернуть" else "Показать ещё ($hiddenCount)",
                    style = MaterialTheme.typography.labelLarge,
                    color = CpColor.Primary,
                )
            }
        }
    }
}

@Composable
private fun FilterCheckboxRow(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundedFilterCheckbox(
            checked = checked,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (checked) CpColor.Primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = CpDimens.spacing2),
        )
    }
}

@Composable
private fun RoundedFilterCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(shape)
            .background(if (checked) CpColor.Primary else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (checked) CpColor.Primary else MaterialTheme.colorScheme.outline,
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = CpIcons.Check,
                contentDescription = null,
                tint = CpColor.DarkTextOnPrimary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

private fun Set<String>.toggle(id: String): Set<String> =
    if (contains(id)) this - id else this + id
