package com.coffeepeek.admin.ui.screen.addshop

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.AppButton
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.layout.ContentScale
import com.coffeepeek.admin.utils.KamelExt
import com.coffeepeek.admin.utils.MAX_SHOP_PHOTOS
import com.coffeepeek.admin.utils.PhotoPickerController
import com.coffeepeek.admin.utils.rememberPhotoPicker
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.City
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShopScreen(vm: AddShopViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()

    // Диалог успеха
    if (state.isSuccess) {
        AlertDialog(
            onDismissRequest = vm::onSuccessDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Заявка отправлена!", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    "Кофейня отправлена на модерацию. После проверки она появится в ленте.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = vm::onSuccessDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(CpDimens.buttonRadius),
                ) { Text("Отлично!") }
            },
            shape = RoundedCornerShape(CpDimens.radius2xl),
        )
    }

    // Диалог ошибки
    state.submitError?.let { err ->
        AlertDialog(
            onDismissRequest = vm::clearSubmitError,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Ошибка", style = MaterialTheme.typography.headlineSmall) },
            text = { Text(err, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = vm::clearSubmitError) { Text("Понятно") }
            },
            shape = RoundedCornerShape(CpDimens.radius2xl),
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = when (state.currentStep) {
                                AddShopStep.BASIC    -> "Основное"
                                AddShopStep.PHOTOS   -> "Фото"
                                AddShopStep.SCHEDULE -> "Расписание"
                                AddShopStep.CONTACTS -> "Контакты"
                                AddShopStep.FEATURES -> "Особенности"
                            },
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (state.currentStep == AddShopStep.BASIC) Navigator.popBack()
                            else vm.prevStep()
                        }) {
                            Icon(CpIcons.Back, contentDescription = "Назад")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                // Прогресс-бар
                val progress = when (state.currentStep) {
                    AddShopStep.BASIC    -> 0.2f
                    AddShopStep.PHOTOS   -> 0.4f
                    AddShopStep.SCHEDULE -> 0.6f
                    AddShopStep.CONTACTS -> 0.8f
                    AddShopStep.FEATURES -> 1.0f
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoadingCatalogs) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CoffeePeekLoader()
            }
            return@Scaffold
        }

        if (state.catalogsError != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.catalogsError ?: "Ошибка", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(CpDimens.spacing3))
                    Button(onClick = { vm.loadCatalogs() }) { Text("Повторить") }
                }
            }
            return@Scaffold
        }

        var isPhotoLoading by remember { mutableStateOf(false) }
        val remainingPhotos = (MAX_SHOP_PHOTOS - state.photos.size).coerceAtLeast(1)
        val photoPicker = rememberPhotoPicker(
            maxSelection = remainingPhotos,
            isLoading = { isPhotoLoading = it },
            onPhotosPicked = vm::addPhotos,
        )

        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (slideInHorizontally { if (forward) it else -it }) togetherWith
                    (slideOutHorizontally { if (forward) -it else it })
            },
            label = "step",
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(CpDimens.spacing4),
            ) {
                when (step) {
                    AddShopStep.BASIC    -> StepBasic(state, vm)
                    AddShopStep.PHOTOS   -> StepPhotos(
                        state = state,
                        vm = vm,
                        photoPicker = photoPicker,
                        isPhotoLoading = isPhotoLoading,
                    )
                    AddShopStep.SCHEDULE -> StepSchedule(state, vm)
                    AddShopStep.CONTACTS -> StepContacts(state, vm)
                    AddShopStep.FEATURES -> StepFeatures(state, vm)
                }

                Spacer(Modifier.height(CpDimens.spacing6))

                AppButton(
                    text = when (step) {
                        AddShopStep.FEATURES -> "Отправить на модерацию"
                        else -> "Продолжить"
                    },
                    onClick = vm::nextStep,
                    enabled = !state.isSubmitting &&
                        (step != AddShopStep.BASIC || state.step1Valid),
                )

                if (state.isSubmitting) {
                    Spacer(Modifier.height(CpDimens.spacing3))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CoffeePeekLoader(
                            size = CpDimens.loaderButton,
                            strokeWidth = 2.dp,
                        )
                    }
                }

                Spacer(Modifier.height(CpDimens.spacing8))
            }
        }
    }
}

// ── Шаг 1: Основное ───────────────────────────────────────────────────────────

@Composable
private fun StepBasic(state: AddShopUiState, vm: AddShopViewModel) {
    StepLegend(requiredHint = "Поля со * обязательны для заполнения")
    Spacer(Modifier.height(CpDimens.spacing3))

    FormField(label = "Название", required = true) {
        AppOutlinedField(
            value = state.name,
            onValueChange = vm::onNameChange,
            placeholder = "Например: Rocket Coffee",
            errorText = if (state.name.isNotEmpty()) state.nameError else null,
            counter = "${state.name.length}/55",
        )
    }

    Spacer(Modifier.height(CpDimens.spacing4))

    FormField(label = "Город", required = true) {
        when {
            state.cities.size == 1 -> {
                SingleValueField(value = state.cities.first().name)
            }
            else -> CityDropdown(
                cities = state.cities,
                selected = state.selectedCity,
                onSelect = vm::onCitySelect,
                error = if (state.selectedCity == null && state.name.isNotEmpty()) state.cityError else null,
            )
        }
    }

    Spacer(Modifier.height(CpDimens.spacing4))

    FormField(label = "Адрес", required = true) {
        AppOutlinedField(
            value = state.address,
            onValueChange = vm::onAddressChange,
            placeholder = "Минск, пр. Независимости, 15",
            errorText = if (state.address.isNotEmpty() && state.address.isBlank()) state.addressError else null,
        )
    }

    Spacer(Modifier.height(CpDimens.spacing4))

    FormField(label = "Описание", optional = true) {
        AppOutlinedField(
            value = state.description,
            onValueChange = vm::onDescriptionChange,
            placeholder = "Расскажите о заведении…",
            minLines = 3,
            maxLines = 6,
        )
    }

    Spacer(Modifier.height(CpDimens.spacing4))

    FormField(label = "Ценовой диапазон", optional = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
            listOf(1 to "$", 2 to "$$", 3 to "$$$", 4 to "$$$$").forEach { (value, label) ->
                FilterChip(
                    selected = state.priceRange == value,
                    onClick = { vm.onPriceRangeSelect(if (state.priceRange == value) null else value) },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = state.priceRange == value,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(CpDimens.buttonRadius),
                )
            }
        }
    }
}

// ── Шаг 2: Фото ───────────────────────────────────────────────────────────────

@Composable
private fun StepPhotos(
    state: AddShopUiState,
    vm: AddShopViewModel,
    photoPicker: PhotoPickerController,
    isPhotoLoading: Boolean,
) {
    StepLegend(optional = true)
    Text(
        text = "Добавьте фотографии кофейни (до $MAX_SHOP_PHOTOS). Можно выбрать несколько сразу.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = CpDimens.spacing3),
    )
    Text(
        text = "Добавлено: ${state.photos.size}/$MAX_SHOP_PHOTOS",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = CpDimens.spacing3),
    )

    if (state.photos.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            modifier = Modifier.padding(bottom = CpDimens.spacing3),
        ) {
            state.photos.forEachIndexed { index, photo ->
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(CpDimens.radiusMd)),
                ) {
                    KamelExt.FlowerImage(
                        data = photo.bytes,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    IconButton(
                        onClick = { vm.removePhoto(index) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50)),
                    ) {
                        Icon(
                            CpIcons.Close,
                            contentDescription = "Удалить",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }

    if (state.photos.size < MAX_SHOP_PHOTOS) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
        ) {
            OutlinedButton(
                onClick = photoPicker.pickFromGallery,
                enabled = !isPhotoLoading,
                modifier = Modifier.weight(1f),
            ) {
                Icon(CpIcons.Gallery, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(CpDimens.spacing1))
                Text("Галерея", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = photoPicker.takePhoto,
                enabled = !isPhotoLoading,
                modifier = Modifier.weight(1f),
            ) {
                Icon(CpIcons.Camera, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(CpDimens.spacing1))
                Text("Камера", style = MaterialTheme.typography.labelMedium)
            }
        }
        if (isPhotoLoading) {
            Spacer(Modifier.height(CpDimens.spacing2))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CoffeePeekLoader(
                    size = CpDimens.loaderButton,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(CpDimens.spacing2))
                Text("Обработка фото…", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ── Шаг 3: Расписание ─────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepSchedule(state: AddShopUiState, vm: AddShopViewModel) {
    StepLegend(optional = true)
    Text(
        text = "По умолчанию кофейня открыта каждый день. Укажите общее время и отметьте только закрытые дни.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = CpDimens.spacing4),
    )

    if (!state.usePerDaySchedule) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = CpDimens.spacing3),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(CpDimens.spacing3)) {
                Text("Время работы", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(CpDimens.spacing2))
                TimeAdjuster(
                    label = "Открытие",
                    value = state.unifiedOpenTime,
                    onDecrease = { vm.adjustUnifiedOpen(-30) },
                    onIncrease = { vm.adjustUnifiedOpen(30) },
                )
                Spacer(Modifier.height(CpDimens.spacing2))
                TimeAdjuster(
                    label = "Закрытие",
                    value = state.unifiedCloseTime,
                    onDecrease = { vm.adjustUnifiedClose(-30) },
                    onIncrease = { vm.adjustUnifiedClose(30) },
                )
                Spacer(Modifier.height(CpDimens.spacing3))
                Text(
                    "Быстрый выбор",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(CpDimens.spacing1))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
                    listOf("08:00" to "20:00", "09:00" to "21:00", "10:00" to "22:00").forEach { (open, close) ->
                        val selected = state.unifiedOpenTime == open && state.unifiedCloseTime == close
                        FilterChip(
                            selected = selected,
                            onClick = { vm.applySchedulePreset(open, close) },
                            label = { Text("$open – $close", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = CpDimens.spacing3),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(CpDimens.spacing3)) {
                Text("Закрытые дни", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Нажмите на день, когда кофейня не работает",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(CpDimens.spacing2))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
                    state.schedules.forEach { day ->
                        val isClosed = day.dayOfWeek in state.closedDays
                        FilterChip(
                            selected = isClosed,
                            onClick = { vm.toggleClosedDay(day.dayOfWeek) },
                            label = { Text(day.shortLabel, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        )
                    }
                }
                val openCount = 7 - state.closedDays.size
                Spacer(Modifier.height(CpDimens.spacing2))
                Text(
                    "Открыто $openCount из 7 дней",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Разное время по дням", style = MaterialTheme.typography.titleSmall)
            Text(
                "Если в будни и выходные разные часы",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = state.usePerDaySchedule,
            onCheckedChange = vm::setUsePerDaySchedule,
        )
    }

    AnimatedVisibility(visible = state.usePerDaySchedule) {
        Column(modifier = Modifier.padding(top = CpDimens.spacing3)) {
            state.schedules.forEach { day ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = CpDimens.spacing2),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(Modifier.padding(CpDimens.spacing3)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(day.label, style = MaterialTheme.typography.titleSmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (day.isClosed) "Закрыто" else "Открыто",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.width(CpDimens.spacing1))
                                Switch(
                                    checked = !day.isClosed,
                                    onCheckedChange = { open ->
                                        vm.updateSchedule(day.dayOfWeek) { it.copy(isClosed = !open) }
                                    },
                                )
                            }
                        }
                        if (!day.isClosed) {
                            Spacer(Modifier.height(CpDimens.spacing2))
                            TimeAdjuster(
                                label = "Открытие",
                                value = day.openTime,
                                onDecrease = { vm.adjustDayOpenTime(day.dayOfWeek, -30) },
                                onIncrease = { vm.adjustDayOpenTime(day.dayOfWeek, 30) },
                            )
                            Spacer(Modifier.height(CpDimens.spacing2))
                            TimeAdjuster(
                                label = "Закрытие",
                                value = day.closeTime,
                                onDecrease = { vm.adjustDayCloseTime(day.dayOfWeek, -30) },
                                onIncrease = { vm.adjustDayCloseTime(day.dayOfWeek, 30) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeAdjuster(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(CpDimens.radiusMd))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            IconButton(onClick = onDecrease, modifier = Modifier.size(40.dp)) {
                Icon(CpIcons.ChevronLeft, "Раньше", modifier = Modifier.size(20.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = CpDimens.spacing2),
            )
            IconButton(onClick = onIncrease, modifier = Modifier.size(40.dp)) {
                Icon(CpIcons.ChevronRight, "Позже", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun StepLegend(
    requiredHint: String? = null,
    optional: Boolean = false,
) {
    when {
        requiredHint != null -> Text(
            text = requiredHint,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        optional -> Text(
            text = "Необязательно — можно пропустить",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = CpDimens.spacing1),
        )
    }
}

// ── Шаг 4: Контакты ───────────────────────────────────────────────────────────

@Composable
private fun StepContacts(state: AddShopUiState, vm: AddShopViewModel) {
    StepLegend(optional = true)
    Text(
        text = "Заполните те поля, которые актуальны для вашего заведения.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = CpDimens.spacing4),
    )

    FormField(label = "Телефон", optional = true) {
        AppOutlinedField(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            placeholder = "+375 29 000 00 00",
            keyboardType = KeyboardType.Phone,
        )
    }
    Spacer(Modifier.height(CpDimens.spacing3))
    FormField(label = "Email", optional = true) {
        AppOutlinedField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            placeholder = "info@coffee.by",
            keyboardType = KeyboardType.Email,
        )
    }
    Spacer(Modifier.height(CpDimens.spacing3))
    FormField(label = "Сайт", optional = true) {
        AppOutlinedField(
            value = state.website,
            onValueChange = vm::onWebsiteChange,
            placeholder = "https://coffee.by",
            keyboardType = KeyboardType.Uri,
        )
    }
    Spacer(Modifier.height(CpDimens.spacing3))
    FormField(label = "Instagram", optional = true) {
        AppOutlinedField(
            value = state.instagram,
            onValueChange = vm::onInstagramChange,
            placeholder = "@coffee_minsk",
        )
    }
}

// ── Шаг 3: Особенности ────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepFeatures(state: AddShopUiState, vm: AddShopViewModel) {
    StepLegend(optional = true)
    Text(
        text = "Выберите характеристики, которые описывают ваше заведение.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = CpDimens.spacing4),
    )

    if (state.brewMethods.isNotEmpty()) {
        CatalogGroup("Методы приготовления", state.brewMethods, state.selectedBrewMethodIds, vm::toggleBrewMethod)
        Spacer(Modifier.height(CpDimens.spacing4))
    }
    if (state.beans.isNotEmpty()) {
        CatalogGroup("Зерно", state.beans, state.selectedBeanIds, vm::toggleBean)
        Spacer(Modifier.height(CpDimens.spacing4))
    }
    if (state.roasters.isNotEmpty()) {
        CatalogGroup("Обжарщики", state.roasters, state.selectedRoasterIds, vm::toggleRoaster)
        Spacer(Modifier.height(CpDimens.spacing4))
    }
    if (state.equipment.isNotEmpty()) {
        CatalogGroup("Оборудование", state.equipment, state.selectedEquipmentIds, vm::toggleEquipment)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatalogGroup(
    title: String,
    items: List<CatalogItem>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = CpDimens.spacing2),
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
        items.forEach { item ->
            val isSelected = item.id in selected
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(item.id) },
                label = { Text(item.name, style = MaterialTheme.typography.labelMedium) },
                leadingIcon = if (isSelected) {
                    { Icon(CpIcons.Check, null, modifier = Modifier.size(14.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(CpDimens.buttonRadius),
            )
        }
    }
}

// ── Переиспользуемые компоненты ───────────────────────────────────────────────

@Composable
private fun FormField(
    label: String,
    required: Boolean = false,
    optional: Boolean = false,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        Text(
            text = if (required) "$label *" else label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (optional) {
            Text(
                text = "необязательно",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
    content()
}

@Composable
private fun AppOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    counter: String? = null,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CpDimens.radiusMd),
            placeholder = {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            isError = errorText != null,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor     = MaterialTheme.colorScheme.error,
                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (errorText != null) {
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else Spacer(Modifier.width(0.dp))
            if (counter != null) {
                Text(
                    text = counter,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SingleValueField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = RoundedCornerShape(CpDimens.radiusMd),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
        enabled = false,
    )
}

@Composable
private fun CityDropdown(
    cities: List<City>,
    selected: City?,
    onSelect: (City) -> Unit,
    error: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            placeholder = {
                Text(
                    "Выберите город",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            trailingIcon = {
                Icon(
                    CpIcons.ChevronDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            isError = error != null,
            shape = RoundedCornerShape(CpDimens.radiusMd),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor     = MaterialTheme.colorScheme.error,
                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledBorderColor  = MaterialTheme.colorScheme.outline,
                disabledTextColor    = MaterialTheme.colorScheme.onSurface,
            ),
            enabled = false,
        )
        // Invisible clickable overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        ) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = city.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (city.id == selected?.id)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        onSelect(city)
                        expanded = false
                    },
                    leadingIcon = if (city.id == selected?.id) {
                        { Icon(CpIcons.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                    } else null,
                )
            }
        }
    }
    if (error != null) {
        Text(
            text = error,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp),
        )
    }
}
