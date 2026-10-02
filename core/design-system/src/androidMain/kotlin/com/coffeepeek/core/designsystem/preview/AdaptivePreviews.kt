package com.coffeepeek.core.designsystem.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.coffeepeek.core.designsystem.component.*
import com.coffeepeek.core.designsystem.icons.CpIcons
import com.coffeepeek.core.designsystem.theme.CoffeePeekTheme

@Composable
private fun AdaptiveSample(direction: LayoutDirection) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalLayoutDirection provides direction,
        LocalDensity provides Density(density.density, fontScale = 2f)) {
        CoffeePeekTheme {
            Surface {
                Column(Modifier.width(320.dp).verticalScroll(rememberScrollState()).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${direction.name} · fontScale 2.0 · Manrope")
                    CpTopBar("Длинный заголовок приложения", "Назад", onBack = {})
                    AppButton("Продолжить настройку приложения", {})
                    GroupSection("Параметры") {
                        StepperRow("Количество порций", "12", {}, {}, "Уменьшить", "Увеличить")
                        var checked by remember { mutableStateOf(false) }
                        SwitchRow("Получать уведомления об изменениях", checked, { checked = it })
                        CheckmarkRow("Длинный текст варианта выбора", true, {})
                        ActionRow("Настроить параметры", {})
                    }
                    SettingsRow(CpIcons.Settings, "Настройки оформления приложения",
                        description = "Системная тема", onClick = {})
                    var selection by remember { mutableStateOf("First") }
                    CapsuleSegmentedControl(listOf("First", "Second"), selection, { it }, { selection = it })
                    AppTextField("Название", "Кофе", {}, "Введите название", errorText = "Проверьте значение")
                    CpSearchField("Кофе", {}, "Поиск", "Очистить поиск")
                }
            }
        }
    }
}

@PreviewLightDark @Composable
private fun LargeFontLtrPreview() = AdaptiveSample(LayoutDirection.Ltr)

@PreviewLightDark @Composable
private fun LargeFontRtlPreview() = AdaptiveSample(LayoutDirection.Rtl)
