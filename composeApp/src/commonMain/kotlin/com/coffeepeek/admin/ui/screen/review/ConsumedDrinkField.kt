package com.coffeepeek.admin.ui.screen.review

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.coffeepeek.admin.ui.component.ReviewTextInput
import com.coffeepeek.domain.model.ConsumedDrinkOption
import com.coffeepeek.domain.model.validateConsumedDrink

@Composable
internal fun ConsumedDrinkField(
    drinks: List<ConsumedDrinkOption>,
    slug: String?,
    customName: String?,
    savedName: String?,
    error: String?,
    onRetry: () -> Unit,
    onChange: (String?, String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = drinks.find { it.slug == slug }
    val label = savedName ?: selected?.nameRu?.ifBlank { selected.nameEn } ?: slug ?: "Не выбран"
    Column {
        Text("Напиток (необязательно)", style = MaterialTheme.typography.labelMedium)
        TextButton(onClick = { expanded = true }) { Text(label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Не выбран") }, onClick = { onChange(null, null); expanded = false })
            drinks.forEach { drink ->
                DropdownMenuItem(text = { Text(drink.nameRu.ifBlank { drink.nameEn }) }, onClick = {
                    onChange(drink.slug, if (drink.slug == "other") "" else null)
                    expanded = false
                })
            }
        }
        if (error != null) {
            Text("Не удалось загрузить напитки", color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onRetry) { Text("Повторить") }
        }
        if (slug == "other") {
            ReviewTextInput(value = customName.orEmpty(), onValueChange = { onChange(slug, it.take(100)) },
                placeholder = "Название напитка", singleLine = true, maxLength = 100)
            validateConsumedDrink(slug, customName)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
