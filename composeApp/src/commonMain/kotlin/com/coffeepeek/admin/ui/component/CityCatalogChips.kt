package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.domain.model.City

@Composable
fun CityCatalogChips(
    cities: List<City>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cities.forEach { city ->
            FilterChip(
                selected = selectedId == city.id,
                onClick = { onSelect(if (selectedId == city.id) null else city.id) },
                label = { Text(city.name, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}
