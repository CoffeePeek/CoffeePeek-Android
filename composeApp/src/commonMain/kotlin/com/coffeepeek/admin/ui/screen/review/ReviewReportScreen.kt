package com.coffeepeek.admin.ui.screen.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.component.CpTopBar
import com.coffeepeek.admin.ui.component.ReviewTextInput

@Composable
fun ReviewReportScreen(reviewId: String) {
    var description by rememberSaveable(reviewId) { mutableStateOf("") }
    Scaffold(topBar = { CpTopBar("Пожаловаться на отзыв") }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(CpDimens.spacing4),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
        ) {
            Text("Опишите проблему с отзывом", style = MaterialTheme.typography.titleMedium)
            ReviewTextInput(
                value = description, onValueChange = { description = it },
                placeholder = "Что не так с этим отзывом?", maxLength = 2000,
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Отправка жалоб пока недоступна", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("Отправить") }
        }
    }
}
