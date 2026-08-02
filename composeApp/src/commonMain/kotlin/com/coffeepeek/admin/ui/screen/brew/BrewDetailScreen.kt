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
import com.coffeepeek.admin.ui.component.AppButton
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.icons.CpIcons
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewDetailScreen(sessionId: String) {
    val vm: BrewDetailViewModel = koinViewModel(parameters = { parametersOf(sessionId) })
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заварка") },
                navigationIcon = {
                    IconButton(onClick = { Navigator.popBack() }) {
                        Icon(CpIcons.Back, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = vm::delete) {
                        Icon(CpIcons.Delete, contentDescription = "Удалить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CoffeePeekLoader() }
            state.details == null -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(state.error ?: "Не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                val details = state.details!!
                val session = details.session
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(CpDimens.spacing4),
                    verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
                ) {
                    Text(session.method.labelRu, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(formatBrewDate(session.createdAt), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    DetailLine("Зерно", details.bean?.name ?: "—")
                    details.bean?.let {
                        DetailLine("Происхождение", "${it.originCountryCode} · ${it.roastLevel.labelRu}")
                    }
                    DetailLine("Доза", "${session.doseG} г")
                    DetailLine(session.method.yieldLabel(), "${session.yieldOrWaterG} г")
                    DetailLine("Время", formatDuration(session.durationSec))
                    session.temperatureC?.let { DetailLine("Температура", "$it °C") }
                    if (session.grindNote.isNotBlank()) DetailLine("Помол", session.grindNote)
                    if (session.tasteTags.isNotEmpty()) {
                        DetailLine("Вкус", session.tasteTags.joinToString { it.labelRu })
                    }
                    session.overallScore?.let { DetailLine("Оценка", "$it / 5") }
                    if (session.notes.isNotBlank()) DetailLine("Заметка", session.notes)

                    Spacer(Modifier.height(CpDimens.spacing2))
                    Text("Совет", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        session.adviceSnapshot.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Spacer(Modifier.height(CpDimens.spacing3))
                    AppButton(text = "Повторить", onClick = vm::repeat)
                }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
