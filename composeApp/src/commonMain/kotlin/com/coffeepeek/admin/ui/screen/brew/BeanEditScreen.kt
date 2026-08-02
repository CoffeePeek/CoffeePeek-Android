package com.coffeepeek.admin.ui.screen.brew

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.AppButton
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.domain.brew.CoffeeOriginCountries
import com.coffeepeek.domain.model.RoastLevel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BeanEditScreen(beanId: String = "") {
    val vm: BeanEditViewModel = koinViewModel(parameters = { parametersOf(beanId) })
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (beanId.isBlank()) "Новое зерно" else "Зерно") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(CpDimens.spacing4),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = vm::onName,
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.roasterName,
                onValueChange = vm::onRoaster,
                label = { Text("Обжарщик") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Text("Обжарка", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
                RoastLevel.entries.forEach { level ->
                    FilterChip(
                        selected = state.roastLevel == level,
                        onClick = { vm.onRoast(level) },
                        label = { Text(level.labelRu) },
                    )
                }
            }
            Text("Страна происхождения", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            ) {
                state.countries.forEach { country ->
                    val flag = CoffeeOriginCountries.flagEmoji(country.code)
                    FilterChip(
                        selected = state.originCountryCode.equals(country.code, ignoreCase = true),
                        onClick = { vm.onOrigin(country.code) },
                        label = {
                            Text(if (flag.isNotEmpty()) "$flag ${country.nameRu}" else country.nameRu)
                        },
                    )
                }
            }
            OutlinedTextField(
                value = state.notes,
                onValueChange = vm::onNotes,
                label = { Text("Заметки") },
                modifier = Modifier.fillMaxWidth(),
            )
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(CpDimens.spacing2))
            AppButton(
                text = if (state.isSaving) "Сохранение…" else "Сохранить",
                onClick = vm::save,
                enabled = !state.isSaving,
            )
        }
    }
}
