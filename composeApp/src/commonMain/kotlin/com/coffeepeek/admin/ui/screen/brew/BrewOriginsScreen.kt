package com.coffeepeek.admin.ui.screen.brew

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.coffeepeek.domain.brew.CoffeeOriginCountries
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewOriginsScreen(vm: BrewOriginsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val selected = state.selectedCountry

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selected == null) "Карта стран"
                        else CoffeeOriginCountries.nameRu(selected),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selected != null) vm.clearSelection() else Navigator.popBack()
                        },
                    ) {
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
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CoffeePeekLoader()
            }
            selected != null -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(CpDimens.spacing4),
            ) {
                if (state.sessions.isEmpty()) {
                    item {
                        Text("Нет заварок из этой страны", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(state.sessions, key = { it.session.id }) { details ->
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
            state.origins.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "Добавьте зерно со страной происхождения — здесь появится ваша карта",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(CpDimens.spacing4),
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(CpDimens.spacing4),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            ) {
                item {
                    Text(
                        "Страны вашего зерна по числу заварок",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(state.origins, key = { it.countryCode }) { origin ->
                    val flag = CoffeeOriginCountries.flagEmoji(origin.countryCode)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.selectCountry(origin.countryCode) }
                            .padding(vertical = CpDimens.spacing2),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                if (flag.isNotEmpty()) "$flag ${origin.countryNameRu}" else origin.countryNameRu,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                "${origin.beanCount} пак. · ${origin.sessionCount} заварок",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(CpIcons.ChevronRight, contentDescription = null)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}
