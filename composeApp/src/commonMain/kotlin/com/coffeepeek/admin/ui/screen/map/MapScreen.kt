package com.coffeepeek.admin.ui.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.map.CoffeeMap
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.model.MapShop
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MapScreen(vm: MapViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()

    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = vm::clearError,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Ошибка", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    err,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = vm::clearError) {
                    Text("Понятно", style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = RoundedCornerShape(CpDimens.radius2xl),
        )
    }

    Box(Modifier.fillMaxSize()) {
        CoffeeMap(
            shops = state.shops,
            selectedShopId = state.selectedShop?.id,
            onBoundsChanged = vm::onBoundsChanged,
            onShopClick = vm::onShopSelected,
            modifier = Modifier.fillMaxSize(),
        )

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = CpDimens.spacing4),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
        }

        state.selectedShop?.let { shop ->
            SelectedShopCard(
                shop = shop,
                onOpen = { Navigator.navigate(Navigator.Screen.ShopDetail(shop.id)) },
                onDismiss = vm::clearSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(CpDimens.spacing4),
            )
        }
    }
}

@Composable
private fun SelectedShopCard(
    shop: MapShop,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CpDimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CpDimens.spacing4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalCafe,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(CpDimens.spacing3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shop.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Button(
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CpDimens.spacing4)
                .padding(bottom = CpDimens.spacing4),
            shape = RoundedCornerShape(CpDimens.buttonRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("Подробнее", style = MaterialTheme.typography.labelLarge)
        }
    }
}
