package com.coffeepeek.admin.ui.screen.profile

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.config.AppConfig
import com.coffeepeek.admin.legal.LegalUrls
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.theme.ThemeMode
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.component.LocalFloatingNavClearance
import com.coffeepeek.admin.utils.CpImage
import com.coffeepeek.admin.utils.COFFEEPEEK_SHARE_TEXT
import com.coffeepeek.admin.utils.OpenInBrowser
import com.coffeepeek.admin.utils.ShareHelper
import com.coffeepeek.domain.model.City
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.profile_version
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(vm: ProfileViewModel = koinInject()) {
    val state by vm.uiState.collectAsState()
    val themeMode by vm.themeMode.collectAsState()
    val cities by vm.cities.collectAsState()
    val selectedCityId by vm.selectedCityId.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = { showLogoutDialog = false; vm.logout() },
            onDismiss = { showLogoutDialog = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { _ ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                CoffeePeekLoader()
            }
            return@Scaffold
        }

        if (state.error != null && !state.hasContent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(CpDimens.spacing4),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error ?: "Ошибка загрузки профиля",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(CpDimens.spacing3))
                    Button(
                        onClick = vm::refreshProfile,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Попробовать снова")
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            state.refreshError?.let { refreshError ->
                Text(
                    text = refreshError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CpDimens.spacing4, vertical = CpDimens.spacing2),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // ── Шапка ─────────────────────────────────────────────────────────
            if (state.isLoggedIn) {
                ProfileHeader(
                    state = state,
                    onEdit = { Navigator.navigate(Navigator.Screen.EditProfile) },
                )
            } else {
                GuestLoginHeader(
                    onLogin = { Navigator.navigate(Navigator.Screen.Auth) },
                )
            }

            Spacer(Modifier.height(CpDimens.spacing4))

            // ── Добавить ──────────────────────────────────────────────────────
            SettingsSection(title = "Добавить") {
                SettingsRow(
                    icon = CpIcons.Add,
                    label = "Добавить кофейню",
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { Navigator.navigate(Navigator.Screen.AddShop) },
                )
                SettingsDivider()
                SettingsRow(
                    icon = CpIcons.CoffeeBean,
                    label = "Добавить обжарщика",
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { Navigator.navigate(Navigator.Screen.AddRoaster) },
                )
            }

            Spacer(Modifier.height(CpDimens.settingsSectionSpacing))

            // ── Моя активность ─────────────────────────────────────────────────
            SettingsSection(title = "Моя активность") {
                SettingsRow(
                    icon = CpIcons.Favorite,
                    label = "Избранные кофейни",
                    enabled = state.isLoggedIn,
                    showArrow = state.isLoggedIn,
                    onClick = { Navigator.navigate(Navigator.Screen.Favorites) },
                )
                SettingsDivider()
                SettingsRow(
                    icon = CpIcons.Review,
                    label = "Мои отзывы",
                    enabled = state.isLoggedIn,
                    showArrow = state.isLoggedIn,
                    onClick = { Navigator.navigate(Navigator.Screen.MyReviews) },
                )
                SettingsDivider()
                SettingsRow(
                    icon = CpIcons.Location,
                    label = "Чекины",
                    enabled = state.isLoggedIn,
                    showArrow = state.isLoggedIn,
                    onClick = { Navigator.navigate(Navigator.Screen.VisitedPlaces) },
                )
            }

            Spacer(Modifier.height(CpDimens.settingsSectionSpacing))

            // ── Настройки ─────────────────────────────────────────────────────
            SettingsSection(title = "Настройки") {
                CityRow(
                    cities = cities,
                    selectedCityId = selectedCityId,
                    onSelect = vm::setCity,
                )
                SettingsDivider()
                ThemeRow(current = themeMode, onSelect = vm::setTheme)
                SettingsDivider()
                SettingsRow(
                    icon = CpIcons.Info,
                    label = stringResource(Res.string.profile_version),
                    trailing = {
                        Text(
                            text = AppConfig.versionName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {},
                    showArrow = false,
                )
            }

            Spacer(Modifier.height(CpDimens.settingsSectionSpacing))

            SettingsSection(title = "О приложении") {
                SettingsRow(
                    icon = CpIcons.Lock,
                    label = "Политика использования",
                    onClick = { OpenInBrowser.openInBrowser(LegalUrls.TERMS) },
                )
                SettingsDivider()
                SettingsRow(
                    icon = CpIcons.Share,
                    label = "Поделиться",
                    onClick = { ShareHelper.shareText(COFFEEPEEK_SHARE_TEXT) },
                )
            }

            Spacer(Modifier.height(CpDimens.settingsSectionSpacing))

            // ── Выход ─────────────────────────────────────────────────────────
            if (state.isLoggedIn) {
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CpDimens.settingsPagePadding)
                    .height(CpDimens.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CpColor.Error.copy(alpha = 0.1f),
                    contentColor   = CpColor.Error,
                ),
                shape = RoundedCornerShape(CpDimens.buttonRadius),
            ) {
                Icon(
                    imageVector = CpIcons.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(CpDimens.spacing2))
                Text(
                    text = "Выйти",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            }

            Spacer(Modifier.height(CpDimens.spacing8 + LocalFloatingNavClearance.current))
        }
    }
}

// ── Шапка профиля ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(state: ProfileUiState, onEdit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                horizontal = CpDimens.settingsPagePadding,
                vertical = CpDimens.spacing4,
            ),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing4),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.size(104.dp)) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(CpColor.Primary, CpColor.GoldWarm))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.initials.ifEmpty { "?" },
                        style = MaterialTheme.typography.headlineMedium,
                        color = CpColor.DarkTextOnPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    if (!state.avatarUrl.isNullOrBlank()) {
                        CpImage(
                            data = state.avatarUrl,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = CpIcons.Edit,
                        contentDescription = "Редактировать профиль",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = state.displayName.ifBlank { "Пользователь" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (state.email.isNotBlank()) {
                    Text(
                        text = state.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(CpDimens.spacing2))
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatBadge(state.reviewCount, "Отзывы", Modifier.weight(1f))
                    StatBadge(state.checkInCount, "Чек-ины", Modifier.weight(1f))
                    StatBadge(state.addedShopsCount, "Кофейни", Modifier.weight(1f))
                }
            }
        }

        if (!state.about.isNullOrBlank()) {
            Text(
                text = state.about,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GuestLoginHeader(onLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                horizontal = CpDimens.settingsPagePadding,
                vertical = CpDimens.spacing4,
            ),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
    ) {
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(CpDimens.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(CpDimens.buttonRadius),
        ) {
            Text(
                text = "Присоединиться к сообществу CoffeePeek",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun StatBadge(count: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Выбор темы через DropdownMenu ─────────────────────────────────────────────

private fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> "Авто"
    ThemeMode.LIGHT  -> "Светлая"
    ThemeMode.DARK   -> "Тёмная"
}

private fun ThemeMode.icon() = when (this) {
    ThemeMode.SYSTEM -> CpIcons.ThemeSystem
    ThemeMode.LIGHT  -> CpIcons.ThemeLight
    ThemeMode.DARK   -> CpIcons.ThemeDark
}

@Composable
private fun CityRow(
    cities: List<City>,
    selectedCityId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCity = cities.firstOrNull { it.id == selectedCityId }
    val menuShape = RoundedCornerShape(CpDimens.selectRadius)

    SettingsRow(
        icon = CpIcons.Location,
        label = "Город",
        trailing = {
            Box {
                Row(
                    modifier = Modifier
                        .height(CpDimens.buttonHeight)
                        .clip(menuShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, menuShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = CpDimens.spacing3),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
                ) {
                    Text(
                        text = selectedCity?.name ?: "Выберите",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp),
                    )
                    Icon(
                        imageVector = CpIcons.ChevronUpDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .widthIn(min = 208.dp)
                        .clip(menuShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, menuShape),
                    shape = menuShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    cities.forEach { city ->
                        val isSelected = city.id == selectedCityId
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Normal,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            },
                            onClick = {
                                onSelect(city.id)
                                expanded = false
                            },
                            trailingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = CpIcons.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        },
        onClick = { if (cities.isNotEmpty()) expanded = true },
        showArrow = false,
    )
}

@Composable
private fun ThemeRow(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val menuMinWidth = 208.dp
    val menuWidth = anchorWidth.coerceAtLeast(menuMinWidth)
    val selectShape = RoundedCornerShape(CpDimens.selectRadius)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = CpDimens.settingsRowPaddingH,
                vertical = CpDimens.settingsRowPaddingV,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(CpDimens.settingsIconContainer)
                .clip(RoundedCornerShape(CpDimens.settingsIconRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = current.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(CpDimens.settingsIconSize),
            )
        }
        Spacer(Modifier.width(CpDimens.spacing3))
        Text(
            text = "Тема",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )

        Box(modifier = Modifier.wrapContentWidth(Alignment.End)) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(CpDimens.buttonHeight)
                    .onGloballyPositioned { coords ->
                        anchorWidth = with(density) { coords.size.width.toDp() }
                    }
                    .clip(selectShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, selectShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { expanded = true }
                    .padding(horizontal = CpDimens.spacing3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
            ) {
                Text(
                    text = current.label(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = CpIcons.ChevronUpDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(x = anchorWidth - menuWidth, y = 0.dp),
                modifier = Modifier
                    .width(menuWidth)
                    .clip(selectShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, selectShape),
                shape = selectShape,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = mode == current
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = mode.label(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                                softWrap = false,
                            )
                        },
                        onClick = {
                            onSelect(mode)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = mode.icon(),
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = CpIcons.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}

// ── Общие компоненты ──────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = CpDimens.settingsPagePadding)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = androidx.compose.ui.unit.TextUnit(0.08f, androidx.compose.ui.unit.TextUnitType.Em),
            modifier = Modifier.padding(bottom = CpDimens.spacing2),
        )
        Card(
            shape = RoundedCornerShape(CpDimens.cardRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    showArrow: Boolean = true,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    iconBg: Color = MaterialTheme.colorScheme.surfaceVariant,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = CpDimens.settingsRowPaddingH,
                vertical = CpDimens.settingsRowPaddingV,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(CpDimens.settingsIconContainer)
                .clip(RoundedCornerShape(CpDimens.settingsIconRadius))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(CpDimens.settingsIconSize),
            )
        }
        Spacer(Modifier.width(CpDimens.spacing3))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            trailing()
        } else if (showArrow) {
            Icon(
                imageVector = CpIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(CpDimens.settingsIconSize),
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = CpDimens.settingsDividerStart),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp,
    )
}

// ── Диалог выхода ─────────────────────────────────────────────────────────────

@Composable
private fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Выйти из аккаунта?", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Text(
                text = "Вы уверены? Потребуется повторный вход.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CpColor.Error,
                    contentColor   = Color.White,
                ),
                shape = RoundedCornerShape(CpDimens.buttonRadius),
            ) {
                Text("Выйти", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Отмена",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        shape = RoundedCornerShape(CpDimens.radius2xl),
    )
}
