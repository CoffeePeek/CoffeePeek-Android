package com.coffeepeek.admin.ui.screen.checkins

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.domain.model.CheckIn
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitedPlacesScreen(vm: VisitedPlacesViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.checkIns.size - 3 && state.hasMore && !state.isLoadingMore
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) vm.loadMore()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Посещённые места") },
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
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CoffeePeekLoader()
            }
            state.error != null && state.checkIns.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error ?: "Ошибка", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = vm::refresh, modifier = Modifier.padding(top = CpDimens.spacing3)) {
                        Text("Попробовать снова")
                    }
                }
            }
            state.checkIns.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Пока нет чек-инов", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                state = listState,
                contentPadding = PaddingValues(CpDimens.spacing4),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
            ) {
                items(state.checkIns, key = { it.id }) { checkIn ->
                    CheckInCard(
                        checkIn = checkIn,
                        onClick = { Navigator.navigate(Navigator.Screen.ShopDetail(checkIn.shopId)) },
                    )
                }
                if (state.isLoadingMore) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(CpDimens.spacing3), contentAlignment = Alignment.Center) {
                            CoffeePeekLoader(size = CpDimens.loaderButton, strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckInCard(checkIn: CheckIn, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(CpDimens.spacing3)) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(CpIcons.Location, null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = checkIn.shopName.ifBlank { "Кофейня" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = CpDimens.spacing2),
                )
            }
            if (checkIn.note.isNotBlank()) {
                Text(
                    text = checkIn.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = CpDimens.spacing1),
                )
            }
            if (checkIn.createdAt.isNotBlank()) {
                Text(
                    text = checkIn.createdAt.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
