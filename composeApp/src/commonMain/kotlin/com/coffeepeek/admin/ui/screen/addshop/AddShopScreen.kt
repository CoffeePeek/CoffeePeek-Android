package com.coffeepeek.admin.ui.screen.addshop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
                    AddShopStep.BASIC    -> 0.33f
                    AddShopStep.CONTACTS -> 0.66f
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

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
                        CircularProgressIndicator(
                            modifier = Modifier.size(CpDimens.loaderButton),
                            color = MaterialTheme.colorScheme.primary,
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
    FormField(label = "Название*") {
        AppOutlinedField(
            value = state.name,
            onValueChange = vm::onNameChange,
            placeholder = "Например: Rocket Coffee",
            errorText = if (state.name.isNotEmpty()) state.nameError else null,
            counter = "${state.name.length}/55",
        )
    }

    Spacer(Modifier.height(CpDimens.spacing4))

    FormField(label = "Город*") {
        CityDropdown(
            cities = state.cities,
            selected = state.selectedCity,
            onSelect = vm::onCitySelect,
            error = if (state.selectedCity == null && state.name.isNotEmpty()) state.cityError else null,
        )
    }

    Spacer(Modifier.height(CpDimens.spacing4))

    FormField(label = "Адрес*") {
        AppOutlinedField(
            value = state.address,
            onValueChange = vm::onAddressChange,
            placeholder = "Минск, пр. Независимости, 15",
            errorText = if (state.address.isNotEmpty() && state.address.isBlank()) state.addressError else null,
        )
    }

    Spacer(Modifier.height(CpDimens.spacing4))

    FormField(label = "Описание") {
        AppOutlinedField(
            value = state.description,
            onValueChange = vm::onDescriptionChange,
            placeholder = "Расскажите о заведении…",
            minLines = 3,
            maxLines = 6,
        )
    }

    Spacer(Modifier.height(CpDimens.spacing4))

    FormField(label = "Ценовой диапазон") {
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

// ── Шаг 2: Контакты ───────────────────────────────────────────────────────────

@Composable
private fun StepContacts(state: AddShopUiState, vm: AddShopViewModel) {
    Text(
        text = "Заполните те поля, которые актуальны для вашего заведения.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = CpDimens.spacing4),
    )

    FormField(label = "Телефон") {
        AppOutlinedField(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            placeholder = "+375 29 000 00 00",
            keyboardType = KeyboardType.Phone,
        )
    }
    Spacer(Modifier.height(CpDimens.spacing3))
    FormField(label = "Email") {
        AppOutlinedField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            placeholder = "info@coffee.by",
            keyboardType = KeyboardType.Email,
        )
    }
    Spacer(Modifier.height(CpDimens.spacing3))
    FormField(label = "Сайт") {
        AppOutlinedField(
            value = state.website,
            onValueChange = vm::onWebsiteChange,
            placeholder = "https://coffee.by",
            keyboardType = KeyboardType.Uri,
        )
    }
    Spacer(Modifier.height(CpDimens.spacing3))
    FormField(label = "Instagram") {
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
                    { Icon(Icons.Outlined.Check, null, modifier = Modifier.size(14.dp)) }
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
private fun FormField(label: String, content: @Composable () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
    )
    content()
}

@Composable
private fun AppOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
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
            modifier = Modifier.fillMaxWidth(),
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
                    Icons.Outlined.ExpandMore,
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
                        { Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
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
