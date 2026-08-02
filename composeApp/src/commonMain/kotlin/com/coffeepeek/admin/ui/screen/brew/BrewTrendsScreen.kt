package com.coffeepeek.admin.ui.screen.brew

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.icons.CpIcons
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewTrendsScreen(vm: BrewTrendsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val trends = state.trends

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тренды") },
                navigationIcon = {
                    IconButton(onClick = { Navigator.popBack() }) {
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
        if (state.isLoading || trends == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CoffeePeekLoader()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(CpDimens.spacing4),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
                FilterChip(
                    selected = state.periodDays == 7,
                    onClick = { vm.load(7) },
                    label = { Text("7 дней") },
                )
                FilterChip(
                    selected = state.periodDays == 30,
                    onClick = { vm.load(30) },
                    label = { Text("30 дней") },
                )
            }

            Text("${trends.sessionCount} заварок", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Средняя оценка: ${trends.averageScore?.let { String.format("%.1f", it) } ?: "—"}",
                style = MaterialTheme.typography.bodyLarge,
            )
            trends.scoreDeltaVsPrevious?.let { delta ->
                val label = when {
                    delta > 0.1f -> "Лучше прошлого периода (+${String.format("%.1f", delta)})"
                    delta < -0.1f -> "Хуже прошлого периода (${String.format("%.1f", delta)})"
                    else -> "На уровне прошлого периода"
                }
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trends.dominantTasteShift?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(CpDimens.spacing2))
            Text("Методы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (trends.methodCounts.isEmpty()) {
                Text("Нет данных", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                trends.methodCounts.entries
                    .sortedByDescending { it.value }
                    .forEach { (method, count) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(method.labelRu)
                            Text("$count")
                        }
                    }
            }

            Spacer(Modifier.height(CpDimens.spacing2))
            Text("Вкус", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (trends.tasteCounts.isEmpty()) {
                Text("Нет отметок вкуса", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                trends.tasteCounts.entries
                    .sortedByDescending { it.value }
                    .forEach { (tag, count) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tag.labelRu)
                            Text("$count")
                        }
                    }
            }
        }
    }
}
