package com.coffeepeek.admin.ui.screen.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.checkin_error_note_required
import coffeepeek.composeapp.generated.resources.checkin_note_hint_private
import coffeepeek.composeapp.generated.resources.checkin_note_hint_public
import coffeepeek.composeapp.generated.resources.checkin_public_hint
import coffeepeek.composeapp.generated.resources.checkin_public_title
import coffeepeek.composeapp.generated.resources.checkin_submit
import coffeepeek.composeapp.generated.resources.checkin_title
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.component.AppButton
import com.coffeepeek.admin.ui.screen.review.ReviewRatingRow
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInBottomSheet(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        isPublic: Boolean,
        note: String?,
        placeRating: Int,
        serviceRating: Int,
        coffeeRating: Int,
    ) -> Unit,
) {
    var isPublic by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var noteError by remember { mutableStateOf<String?>(null) }
    var placeRating by remember { mutableIntStateOf(5) }
    var serviceRating by remember { mutableIntStateOf(5) }
    var coffeeRating by remember { mutableIntStateOf(5) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val noteRequiredError = stringResource(Res.string.checkin_error_note_required)

    fun onPublicChange(value: Boolean) {
        isPublic = value
        if (!value) noteError = null
    }

    fun onNoteChange(value: String) {
        note = value.take(500)
        if (noteError != null && (!isPublic || note.trim().isNotEmpty())) {
            noteError = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CpDimens.spacing4)
                .padding(bottom = CpDimens.spacing6),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
        ) {
            Text(
                text = stringResource(Res.string.checkin_title),
                style = MaterialTheme.typography.titleLarge,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.checkin_public_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(Res.string.checkin_public_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = isPublic,
                    onCheckedChange = ::onPublicChange,
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = ::onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        stringResource(
                            if (isPublic) Res.string.checkin_note_hint_public
                            else Res.string.checkin_note_hint_private,
                        ),
                    )
                },
                isError = noteError != null,
                supportingText = noteError?.let { error ->
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                },
                minLines = 2,
                maxLines = 4,
            )

            ReviewRatingRow("Атмосфера", placeRating) { placeRating = it }
            ReviewRatingRow("Сервис", serviceRating) { serviceRating = it }
            ReviewRatingRow("Кофе", coffeeRating) { coffeeRating = it }

            AppButton(
                text = stringResource(Res.string.checkin_submit),
                onClick = {
                    val trimmedNote = note.trim()
                    if (isPublic && trimmedNote.isEmpty()) {
                        noteError = noteRequiredError
                        return@AppButton
                    }
                    onSubmit(
                        isPublic,
                        trimmedNote.takeIf { it.isNotEmpty() },
                        placeRating,
                        serviceRating,
                        coffeeRating,
                    )
                },
                enabled = !isLoading,
            )
        }
    }
}
