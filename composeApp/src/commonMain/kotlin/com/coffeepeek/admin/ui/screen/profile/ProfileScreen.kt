package com.coffeepeek.admin.ui.screen.profile

import com.coffeepeek.admin.ui.component.GuestAuthCard

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.config.AppConfig
import com.coffeepeek.admin.legal.LegalUrls
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.theme.ThemeMode
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.component.LocalFloatingNavClearance
import com.coffeepeek.admin.ui.component.SettingsIconBadge
import com.coffeepeek.admin.ui.component.SettingsIconColors
import com.coffeepeek.admin.ui.component.SettingsIconPalette
import com.coffeepeek.admin.utils.CpImage
import com.coffeepeek.admin.utils.COFFEEPEEK_SHARE_TEXT
import com.coffeepeek.admin.utils.OpenInBrowser
import com.coffeepeek.admin.utils.ShareHelper
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
                        modifier = Modifier.height(CpDimens.buttonHeight),
                        shape = RoundedCornerShape(percent = 50),
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
                    onRegister = { Navigator.navigate(Navigator.Screen.Register) },
                )
            }

            Spacer(Modifier.height(CpDimens.spacing4))

            // ── Добавить ──────────────────────────────────────────────────────
            SettingsSection(title = if (state.isLoggedIn) "Добавить" else "Внести вклад") {
                SettingsRow(
                    icon = CpIcons.Add,
                    label = "Добавить кофейню",
                    description = "Предложить новое место для CoffeePeek",
                    iconColors = SettingsIconPalette.Gold,
                    onClick = { Navigator.navigate(Navigator.Screen.AddShop) },
                )
                SettingsDivider()
                SettingsRow(
                    icon = CpIcons.CoffeeBean,
                    label = "Добавить обжарщика",
                    description = "Помогите сообществу открыть новых обжарщиков",
                    iconColors = SettingsIconPalette.Mint,
                    onClick = { Navigator.navigate(Navigator.Screen.AddRoaster) },
                )
            }

            // ── Моя активность ─────────────────────────────────────────────────
            Spacer(Modifier.height(CpDimens.settingsSectionSpacing))
            SettingsSection(title = "Моя активность") {
                SettingsRow(
                    icon = CpIcons.Favorite,
                    label = "Избранные кофейни",
                    description = "Кофейни, которые вы сохранили",
                    iconColors = SettingsIconPalette.Rose,
                    onClick = { Navigator.navigate(Navigator.Screen.Favorites) },
                )
                SettingsDivider()
                SettingsRow(
                    icon = CpIcons.Review,
                    label = "Мои отзывы",
                    description = "Ваши оценки и отзывы о кофейнях",
                    iconColors = SettingsIconPalette.Lavender,
                    onClick = { Navigator.navigate(Navigator.Screen.MyReviews) },
                )
                SettingsDivider()
                SettingsRow(
                    icon = CpIcons.Location,
                    label = "Чекины",
                    description = "Места, которые вы уже посетили",
                    iconColors = SettingsIconPalette.Sky,
                    onClick = { Navigator.navigate(Navigator.Screen.VisitedPlaces) },
                )
                if (state.isLoggedIn) {
                    SettingsDivider()
                    SettingsRow(
                        icon = CpIcons.NoteEdit,
                        label = "Мои правки кофеен",
                        description = "Заявки, которые вы отправили на модерацию",
                        iconColors = SettingsIconPalette.Gold,
                        onClick = { Navigator.navigate(Navigator.Screen.MyShopChanges) },
                    )
                }
            }

            if (state.canModerate) {
                Spacer(Modifier.height(CpDimens.settingsSectionSpacing))
                SettingsSection(title = "Модерация") {
                    SettingsRow(
                        icon = CpIcons.CheckCircle,
                        label = "Заявки на правки",
                        description = "Просмотр, правка и решение по заявкам пользователей",
                        iconColors = SettingsIconPalette.Violet,
                        onClick = { Navigator.navigate(Navigator.Screen.ModeratorShopChanges) },
                    )
                }
            }

            Spacer(Modifier.height(CpDimens.settingsSectionSpacing))

            // ── Настройки ─────────────────────────────────────────────────────
            SettingsSection(title = "Настройки") {
                SettingsRow(
                    icon = CpIcons.Location,
                    label = "Город",
                    description = "Определяет, какие кофейни показывать в первую очередь",
                    iconColors = SettingsIconPalette.Aqua,
                    trailing = {
                        CityValue(
                            cityName = cities.firstOrNull { it.id == selectedCityId }?.name,
                        )
                    },
                    onClick = { Navigator.navigate(Navigator.Screen.CitySettings) },
                )
                SettingsDivider()
                SettingsRow(
                    icon = themeMode.icon(),
                    label = "Тема",
                    description = "Настройте внешний вид приложения",
                    iconColors = SettingsIconPalette.Violet,
                    trailing = { ThemeValue(themeMode) },
                    onClick = { Navigator.navigate(Navigator.Screen.ThemeSettings) },
                )
            }

            Spacer(Modifier.height(CpDimens.settingsSectionSpacing))

            SettingsSection(
                title = "Другие настройки",
                description = "Управление полезными дополнениями, отзывы в App Store и настройки конфиденциальности",
            ) {
                SettingsRow(
                    icon = CpIcons.Lock,
                    label = "Политика использования",
                    iconColors = SettingsIconPalette.Emerald,
                    onClick = { OpenInBrowser.openInBrowser(LegalUrls.TERMS) },
                )
                SettingsDivider()
                SettingsRow(
                    icon = CpIcons.Share,
                    label = "Поделиться",
                    iconColors = SettingsIconPalette.BrightCyan,
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

            AppVersionFooter()
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
private fun GuestLoginHeader(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
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
            text = "Аккаунт",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        GuestAuthCard(onLogin = onLogin, onRegister = onRegister)
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

// ── Настройки города и темы ───────────────────────────────────────────────────

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
private fun CityValue(cityName: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        Text(
            text = cityName ?: "Выберите",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = CpIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(CpDimens.settingsIconSize),
        )
    }
}

@Composable
private fun ThemeValue(current: ThemeMode) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        Text(
            text = current.label(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Icon(
            imageVector = CpIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ── Общие компоненты ──────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    description: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = CpDimens.settingsPagePadding)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = androidx.compose.ui.unit.TextUnit(0.08f, androidx.compose.ui.unit.TextUnitType.Em),
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = CpDimens.spacing1),
            )
        }
        Spacer(Modifier.height(CpDimens.spacing2))
        Card(
            shape = RoundedCornerShape(CpDimens.cardRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    description: String? = null,
    onClick: () -> Unit,
    showArrow: Boolean = true,
    iconColors: SettingsIconColors = SettingsIconPalette.Cyan,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = CpDimens.settingsRowPaddingH,
                vertical = CpDimens.settingsRowPaddingV,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconBadge(icon = icon, colors = iconColors)
        Spacer(Modifier.width(CpDimens.spacing3))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(CpDimens.spacing2))
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
private fun AppVersionFooter() {
    Text(
        text = "Версия ${AppConfig.versionName}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = CpDimens.settingsPagePadding,
                top = CpDimens.spacing4,
                end = CpDimens.settingsPagePadding,
            ),
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
        thickness = 1.dp,
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
                modifier = Modifier.height(CpDimens.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CpColor.Error,
                    contentColor   = Color.White,
                ),
                shape = RoundedCornerShape(percent = 50),
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
