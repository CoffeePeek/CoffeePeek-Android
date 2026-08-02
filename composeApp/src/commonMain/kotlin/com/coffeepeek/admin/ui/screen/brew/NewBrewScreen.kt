package com.coffeepeek.admin.ui.screen.brew

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.component.AppButton
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.domain.model.TasteTag
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewBrewScreen(repeatSessionId: String = "") {
    val vm: NewBrewViewModel = koinViewModel(parameters = { parametersOf(repeatSessionId) })
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stepTitle(state.step)) },
                navigationIcon = {
                    IconButton(onClick = vm::back) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(CpDimens.spacing4),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
            ) {
                when (state.step) {
                    NewBrewStep.METHOD -> MethodStep(state, vm::selectMethod)
                    NewBrewStep.BEAN -> BeanStep(state, vm::selectBean)
                    NewBrewStep.PARAMS -> ParamsStep(state, vm)
                    NewBrewStep.TASTE -> TasteStep(state, vm)
                    NewBrewStep.ADVICE -> AdviceStep(state)
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(CpDimens.spacing3))
            AppButton(
                text = when (state.step) {
                    NewBrewStep.ADVICE -> if (state.isSaving) "Сохранение…" else "Сохранить"
                    else -> "Далее"
                },
                onClick = vm::next,
                enabled = !state.isSaving,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MethodStep(state: NewBrewUiState, onSelect: (com.coffeepeek.domain.model.BrewMethod) -> Unit) {
    Text("Как завариваете?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
        state.methods.forEach { method ->
            FilterChip(
                selected = state.method == method,
                onClick = { onSelect(method) },
                label = { Text(method.labelRu) },
            )
        }
    }
}

@Composable
private fun BeanStep(state: NewBrewUiState, onSelect: (String?) -> Unit) {
    Text("Зерно", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text(
        "Можно пропустить и добавить пакет позже.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    FilterChip(
        selected = state.selectedBeanId == null,
        onClick = { onSelect(null) },
        label = { Text("Без зерна") },
    )
    state.beans.forEach { bean ->
        FilterChip(
            selected = state.selectedBeanId == bean.id,
            onClick = { onSelect(bean.id) },
            label = { Text("${bean.name} · ${bean.originCountryCode}") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ParamsStep(state: NewBrewUiState, vm: NewBrewViewModel) {
    Text("Параметры", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    OutlinedTextField(
        value = state.doseG,
        onValueChange = vm::onDose,
        label = { Text("Доза, г") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    OutlinedTextField(
        value = state.yieldOrWaterG,
        onValueChange = vm::onYield,
        label = { Text(state.method.yieldLabel()) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    OutlinedTextField(
        value = state.temperatureC,
        onValueChange = vm::onTemp,
        label = { Text("Температура, °C (опц.)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    OutlinedTextField(
        value = state.grindNote,
        onValueChange = vm::onGrind,
        label = { Text("Помол") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    OutlinedTextField(
        value = state.notes,
        onValueChange = vm::onNotes,
        label = { Text("Заметка") },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(CpDimens.spacing2))
    Text("Таймер", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
    Text(
        formatDuration(state.elapsedSec),
        style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp),
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        androidx.compose.material3.OutlinedButton(
            onClick = { if (state.timerRunning) vm.pauseTimer() else vm.startTimer() },
            modifier = Modifier.weight(1f),
        ) {
            Text(if (state.timerRunning) "Пауза" else "Старт")
        }
        androidx.compose.material3.OutlinedButton(
            onClick = vm::resetTimer,
            modifier = Modifier.weight(1f),
        ) {
            Text("Сброс")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TasteStep(state: NewBrewUiState, vm: NewBrewViewModel) {
    Text("Вкус", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        TasteTag.entries.forEach { tag ->
            FilterChip(
                selected = tag in state.tasteTags,
                onClick = { vm.toggleTaste(tag) },
                label = { Text(tag.labelRu) },
            )
        }
    }
    Spacer(Modifier.height(CpDimens.spacing2))
    Text("Оценка", style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
        (1..5).forEach { score ->
            FilterChip(
                selected = state.overallScore == score,
                onClick = { vm.setScore(score) },
                label = { Text("$score") },
            )
        }
    }
}

@Composable
private fun AdviceStep(state: NewBrewUiState) {
    Text("Совет", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text(
        state.advicePreview,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(CpDimens.spacing2))
    Text(
        "Совет сохранится в истории и не изменится при обновлении правил.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun stepTitle(step: NewBrewStep): String = when (step) {
    NewBrewStep.METHOD -> "Метод"
    NewBrewStep.BEAN -> "Зерно"
    NewBrewStep.PARAMS -> "Параметры"
    NewBrewStep.TASTE -> "Вкус"
    NewBrewStep.ADVICE -> "Совет"
}
