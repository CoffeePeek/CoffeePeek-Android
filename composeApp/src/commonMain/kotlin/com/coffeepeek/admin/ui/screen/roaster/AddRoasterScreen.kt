package com.coffeepeek.admin.ui.screen.roaster

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.component.PhotoAttachmentsSection
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.domain.model.City
import org.koin.compose.viewmodel.koinViewModel

private const val ROASTER_PHOTO_LIMIT = 5

@Composable
fun AddRoasterScreen(vm: AddRoasterViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()

    state.result?.let { result ->
        RoasterSubmittedContent(
            addressValidated = result.isAddressValidated,
            message = result.message,
            onDone = Navigator::popBack,
        )
        return
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = vm::clearError,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Не удалось отправить") },
            text = { Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = vm::clearError) { Text("Понятно") }
            },
            shape = RoundedCornerShape(CpDimens.radius2xl),
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = CpDimens.spacing2, vertical = CpDimens.spacing2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = Navigator::popBack) {
                    Icon(CpIcons.Back, contentDescription = "Назад")
                }
                Text(
                    text = "Добавить обжарщика",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoadingCatalogs) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CoffeePeekLoader() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(CpDimens.spacing4),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing4),
        ) {
            Text(
                text = "Расскажите об обжарщике. После проверки он появится в общем каталоге.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoasterField(
                label = "Название",
                value = state.name,
                onValueChange = vm::onNameChange,
                placeholder = "Например, Coffee Circus",
                error = if (state.name.isNotEmpty()) state.nameError else null,
            )
            RoasterField(
                label = "Об обжарщике",
                value = state.about,
                onValueChange = vm::onAboutChange,
                placeholder = "История, подход к обжарке, особенности",
                singleLine = false,
                minLines = 4,
            )

            Text(
                text = "Адрес (необязательно)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Город и адрес нужно указать вместе.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RoasterCityPicker(
                cities = state.cities,
                selected = state.selectedCity,
                onSelect = vm::onCitySelect,
            )
            RoasterField(
                label = "Адрес",
                value = state.address,
                onValueChange = vm::onAddressChange,
                placeholder = "Улица, дом",
                error = state.locationError,
                leadingIcon = CpIcons.Location,
            )

            RoasterField(
                label = "Instagram",
                value = state.instagram,
                onValueChange = vm::onInstagramChange,
                placeholder = "@username или https://instagram.com/…",
                error = state.instagramError,
                leadingIcon = CpIcons.Instagram,
                keyboardType = KeyboardType.Uri,
            )
            RoasterField(
                label = "Сайт",
                value = state.website,
                onValueChange = vm::onWebsiteChange,
                placeholder = "https://example.com",
                error = state.websiteError,
                leadingIcon = CpIcons.Globe,
                keyboardType = KeyboardType.Uri,
            )

            PhotoAttachmentsSection(
                photos = state.photos,
                maxPhotos = ROASTER_PHOTO_LIMIT,
                onPhotosAdded = vm::addPhotos,
                onRemovePhoto = vm::removePhoto,
                title = "Фотографии обжарщика",
                hint = "До $ROASTER_PHOTO_LIMIT фотографий (необязательно).",
            )

            Button(
                onClick = vm::submit,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(CpDimens.buttonRadius),
            ) {
                if (state.isSubmitting) {
                    CoffeePeekLoader(
                        size = CpDimens.loaderButton,
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Отправить на модерацию", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(CpDimens.spacing4))
        }
    }
}

@Composable
private fun RoasterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String? = null,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing1)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (error != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(icon, contentDescription = null) }
            },
            singleLine = singleLine,
            minLines = minLines,
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(if (singleLine) CpDimens.inputRadius else CpDimens.radiusLg),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = CpDimens.spacing2),
            )
        }
    }
}

@Composable
private fun RoasterCityPicker(
    cities: List<City>,
    selected: City?,
    onSelect: (City?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(280.dp) }
    val density = LocalDensity.current
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .onGloballyPositioned { anchorWidth = with(density) { it.size.width.toDp() } },
            shape = RoundedCornerShape(CpDimens.inputRadius),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            contentPadding = PaddingValues(horizontal = CpDimens.spacing4),
        ) {
            Text(
                text = selected?.name ?: "Не указывать город",
                modifier = Modifier.weight(1f),
                color = if (selected == null) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
            )
            Icon(CpIcons.ChevronDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(anchorWidth),
            shape = RoundedCornerShape(CpDimens.selectRadius),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            DropdownMenuItem(
                text = { Text("Не указывать город") },
                onClick = { onSelect(null); expanded = false },
            )
            cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.name) },
                    onClick = { onSelect(city); expanded = false },
                    trailingIcon = if (city.id == selected?.id) {
                        { Icon(CpIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                )
            }
        }
    }
}

@Composable
private fun RoasterSubmittedContent(
    addressValidated: Boolean,
    message: String,
    onDone: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(CpDimens.spacing6),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing4),
        ) {
            Icon(
                imageVector = CpIcons.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = "Заявка отправлена",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message.ifBlank {
                    if (addressValidated) "Обжарщик появится после проверки модератором."
                    else "Заявка принята. Адрес дополнительно проверит модератор."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onDone) { Text("Готово") }
        }
    }
}
