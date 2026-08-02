package com.coffeepeek.admin.ui.screen.brew

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.AppButton
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.domain.model.BrewSessionDetails
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewHomeScreen(vm: BrewHomeViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои заварки") },
                navigationIcon = {
                    IconButton(onClick = { Navigator.popBack() }) {
                        Icon(CpIcons.Back, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { Navigator.navigate(Navigator.Screen.BrewBeans) }) {
                        Icon(CpIcons.Coffee, contentDescription = "Зерно")
                    }
                    IconButton(onClick = { Navigator.navigate(Navigator.Screen.BrewTrends) }) {
                        Icon(CpIcons.Trends, contentDescription = "Тренды")
                    }
                    IconButton(onClick = { Navigator.navigate(Navigator.Screen.BrewOrigins) }) {
                        Icon(CpIcons.Globe, contentDescription = "Страны")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CoffeePeekLoader()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(CpDimens.spacing4),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
        ) {
            item {
                AppButton(
                    text = "Новая заварка",
                    onClick = { Navigator.navigate(Navigator.Screen.NewBrew()) },
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    MiniStat(
                        title = "За 7 дней",
                        value = "${state.weekTrends?.sessionCount ?: 0}",
                    )
                    MiniStat(
                        title = "Ср. оценка",
                        value = state.weekTrends?.averageScore?.let { String.format("%.1f", it) } ?: "—",
                    )
                    MiniStat(
                        title = "Пакеты",
                        value = "${state.beanCount}",
                    )
                }
                state.weekTrends?.dominantTasteShift?.let { shift ->
                    Spacer(Modifier.height(CpDimens.spacing2))
                    Text(
                        text = shift,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Последние",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = { Navigator.navigate(Navigator.Screen.BrewBeans) }) {
                        Text("Зерно")
                    }
                }
            }

            if (state.recent.isEmpty()) {
                item {
                    Text(
                        "Пока нет заварок. Создайте первую — таймер и совет по помолу внутри.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                items(state.recent, key = { it.session.id }) { details ->
                    BrewSessionRow(
                        details = details,
                        onClick = {
                            Navigator.navigate(Navigator.Screen.BrewDetail(details.session.id))
                        },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun MiniStat(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun BrewSessionRow(
    details: BrewSessionDetails,
    onClick: () -> Unit,
) {
    val session = details.session
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = CpDimens.spacing2),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                session.method.labelRu,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                formatBrewDate(session.createdAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            buildString {
                append(details.bean?.name ?: "Без зерна")
                append(" · ")
                append("${session.doseG} г")
                append(" · ")
                append(formatDuration(session.durationSec))
                session.overallScore?.let { append(" · $it/5") }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (session.adviceSnapshot.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                session.adviceSnapshot.lineSequence().first(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}
