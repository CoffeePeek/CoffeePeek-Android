package com.coffeepeek.admin.ui.screen.shopchange

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.utils.utcIsoToLocalDateTime
import com.coffeepeek.admin.di.platformViewModel
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.component.CpTopBar
import com.coffeepeek.domain.model.ShopChangePayload
import org.koin.core.parameter.parametersOf

@Composable
fun ShopChangeRequestDetailScreen(requestId: String) {
    val vm: ShopChangeRequestDetailViewModel = platformViewModel(
        parameters = { parametersOf(requestId) },
    )
    val state by vm.state.collectAsState()

    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = vm::clearError,
            title = { Text("Ошибка") },
            text = { Text(err) },
            confirmButton = { TextButton(onClick = vm::clearError) { Text("Понятно") } },
        )
    }

    Scaffold(
        topBar = { CpTopBar("Заявка") },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val request = state.request
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CoffeePeekLoader()
            }
            request == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Заявка не найдена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(CpDimens.spacing3))
                    Button(
                        onClick = vm::refresh,
                        modifier = Modifier.height(CpDimens.buttonHeight),
                        shape = RoundedCornerShape(percent = 50),
                    ) { Text("Повторить") }
                }
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = CpDimens.spacing4)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
            ) {
                Text(request.section.title(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(request.status.title(), color = MaterialTheme.colorScheme.primary)
                Text(
                    "Создана: ${utcIsoToLocalDateTime(request.createdAtUtc)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                request.rejectionReason?.takeIf { it.isNotBlank() }?.let { reason ->
                    Text("Причина отклонения: $reason", color = MaterialTheme.colorScheme.error)
                }
                PayloadSummary(request.payload)
                Spacer(Modifier.height(CpDimens.spacing4))
            }
        }
    }
}

@Composable
private fun PayloadSummary(payload: ShopChangePayload) {
    Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing1)) {
        Text("Изменения", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        payload.description?.let { Text("Описание: $it") }
        payload.contacts?.let { contacts ->
            Text("Телефон: ${contacts.phoneNumber ?: "—"}")
            Text("Email: ${contacts.email ?: "—"}")
            Text("Сайт: ${contacts.siteLink ?: "—"}")
            Text("Instagram: ${contacts.instagramLink ?: "—"}")
        }
        payload.photos?.let {
            Text("Сохранённые фото: ${it.retainedPhotoIds.size}, новые: ${it.newPhotos.size}")
        }
        payload.tagIds?.let { Text("Теги: ${it.size}") }
        payload.roasterIds?.let { Text("Обжарщики: ${it.size}") }
        payload.equipmentIds?.let { Text("Оборудование: ${it.size}") }
        payload.brewMethodIds?.let { Text("Методы заваривания: ${it.size}") }
        payload.menu?.let {
            Text("Позиции меню: ${it.items.size}, фото меню: ${it.retainedPhotoIds.size + it.newPhotos.size}")
        }
    }
}
