package com.coffeepeek.admin.ui.screen.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.checkin_action
import coffeepeek.composeapp.generated.resources.checkin_date_label
import coffeepeek.composeapp.generated.resources.checkin_date_picker_confirm
import coffeepeek.composeapp.generated.resources.checkin_date_picker_dismiss
import coffeepeek.composeapp.generated.resources.checkin_description_label
import coffeepeek.composeapp.generated.resources.checkin_header_label
import coffeepeek.composeapp.generated.resources.checkin_header_placeholder
import coffeepeek.composeapp.generated.resources.checkin_note_label
import coffeepeek.composeapp.generated.resources.checkin_note_placeholder
import coffeepeek.composeapp.generated.resources.checkin_photos_label
import coffeepeek.composeapp.generated.resources.checkin_public_switch_hint
import coffeepeek.composeapp.generated.resources.checkin_public_switch_title
import coffeepeek.composeapp.generated.resources.checkin_rating_atmosphere
import coffeepeek.composeapp.generated.resources.checkin_rating_coffee
import coffeepeek.composeapp.generated.resources.checkin_rating_service
import coffeepeek.composeapp.generated.resources.checkin_sheet_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.component.AppButton
import com.coffeepeek.admin.ui.component.PhotoAttachmentsSection
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.admin.utils.MAX_REVIEW_PHOTOS
import com.coffeepeek.admin.utils.currentEpochMillis
import com.coffeepeek.admin.utils.formatVisitDate
import com.coffeepeek.admin.utils.validatePublicCheckInDescription
import com.coffeepeek.admin.utils.validatePublicCheckInHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInBottomSheet(
    draft: CheckInDraft,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onDraftChange: (CheckInDraft) -> Unit,
    onSubmit: (CheckInDraft) -> Unit,
    placeName: String? = null,
) {
    var headerError by remember { mutableStateOf<String?>(null) }
    var noteError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun onPublicChange(value: Boolean) {
        onDraftChange(draft.copy(isPublic = value))
        if (!value) {
            headerError = null
            noteError = null
        }
    }

    fun onHeaderChange(value: String) {
        val updated = value.take(120)
        onDraftChange(draft.copy(header = updated))
        if (headerError != null && validatePublicCheckInHeader(updated) == null) {
            headerError = null
        }
    }

    fun onNoteChange(value: String) {
        val updated = value.take(2000)
        onDraftChange(draft.copy(note = updated))
        if (noteError != null && (!draft.isPublic || validatePublicCheckInDescription(updated) == null)) {
            noteError = null
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = draft.visitMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= currentEpochMillis()
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDraftChange(draft.copy(visitMillis = it))
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(Res.string.checkin_date_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.checkin_date_picker_dismiss))
                }
            },
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
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
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = CpDimens.spacing4)
                .padding(bottom = CpDimens.spacing6),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing6),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing1)) {
                Text(
                    text = stringResource(Res.string.checkin_sheet_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!placeName.isNullOrBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = CpIcons.Location,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = placeName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── Ratings ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            ) {
                RatingCard(
                    modifier = Modifier.weight(1f),
                    image = Res.drawable.checkin_rating_coffee,
                    label = stringResource(Res.string.checkin_rating_coffee),
                    rating = draft.coffeeRating,
                    onRatingChange = { onDraftChange(draft.copy(coffeeRating = it)) },
                )
                RatingCard(
                    modifier = Modifier.weight(1f),
                    image = Res.drawable.checkin_rating_service,
                    label = stringResource(Res.string.checkin_rating_service),
                    rating = draft.serviceRating,
                    onRatingChange = { onDraftChange(draft.copy(serviceRating = it)) },
                )
                RatingCard(
                    modifier = Modifier.weight(1f),
                    image = Res.drawable.checkin_rating_atmosphere,
                    label = stringResource(Res.string.checkin_rating_atmosphere),
                    rating = draft.placeRating,
                    onRatingChange = { onDraftChange(draft.copy(placeRating = it)) },
                )
            }

            // ── Visit date ────────────────────────────────────────────────────
            LabeledField(label = stringResource(Res.string.checkin_date_label)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CpDimens.radiusMd))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(CpDimens.radiusMd),
                        )
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = CpIcons.Calendar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = formatVisitDate(draft.visitMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = CpIcons.ChevronDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // ── Public toggle ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.checkin_public_switch_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.checkin_public_switch_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(CpDimens.spacing3))
                Switch(
                    checked = draft.isPublic,
                    onCheckedChange = ::onPublicChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedBorderColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }

            // ── Public review fields / private note ───────────────────────────
            if (draft.isPublic) {
                LabeledField(label = stringResource(Res.string.checkin_header_label)) {
                    CheckInTextField(
                        value = draft.header,
                        onValueChange = ::onHeaderChange,
                        placeholder = stringResource(Res.string.checkin_header_placeholder),
                        isError = headerError != null,
                        singleLine = true,
                    )
                    headerError?.let { error ->
                        FieldError(error)
                    }
                }
            }

            LabeledField(
                label = stringResource(
                    if (draft.isPublic) {
                        Res.string.checkin_description_label
                    } else {
                        Res.string.checkin_note_label
                    }
                )
            ) {
                CheckInNoteField(
                    value = draft.note,
                    onValueChange = ::onNoteChange,
                    placeholder = stringResource(Res.string.checkin_note_placeholder),
                    isError = noteError != null,
                )
                noteError?.let { error ->
                    FieldError(error)
                }
            }

            // ── Photos (optional) ─────────────────────────────────────────────
            PhotoAttachmentsSection(
                photos = draft.photos,
                maxPhotos = MAX_REVIEW_PHOTOS,
                onPhotosAdded = { added ->
                    val remaining = MAX_REVIEW_PHOTOS - draft.photos.size
                    if (remaining > 0) {
                        onDraftChange(draft.copy(photos = draft.photos + added.take(remaining)))
                    }
                },
                onRemovePhoto = { index ->
                    onDraftChange(
                        draft.copy(photos = draft.photos.filterIndexed { i, _ -> i != index })
                    )
                },
                title = stringResource(Res.string.checkin_photos_label),
                hint = "Добавьте до $MAX_REVIEW_PHOTOS фото вашего визита.",
            )

            // ── Submit ────────────────────────────────────────────────────────
            AppButton(
                text = stringResource(Res.string.checkin_action),
                onClick = {
                    headerError = if (draft.isPublic) {
                        validatePublicCheckInHeader(draft.header)
                    } else {
                        null
                    }
                    noteError = if (draft.isPublic) {
                        validatePublicCheckInDescription(draft.note)
                    } else {
                        null
                    }
                    if (headerError != null || noteError != null) {
                        return@AppButton
                    }
                    onSubmit(draft)
                },
                enabled = !isLoading,
            )
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing1)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun RatingCard(
    image: DrawableResource,
    label: String,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CpDimens.radiusMd))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(CpDimens.radiusMd),
            )
            .padding(horizontal = CpDimens.spacing2, vertical = CpDimens.spacing3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(CpDimens.radiusSm)),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        CompactStarRow(rating = rating, onRatingChange = onRatingChange)
    }
}

@Composable
private fun CompactStarRow(
    rating: Int,
    onRatingChange: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        (1..5).forEach { star ->
            val icon: ImageVector = if (star <= rating) CpIcons.StarFilled else CpIcons.StarOutline
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (star <= rating) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRatingChange(star) },
            )
        }
    }
}

@Composable
private fun CheckInNoteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
) {
    val borderColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outline
    }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CpDimens.radiusMd))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(CpDimens.radiusMd),
            )
            .heightIn(min = 80.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
            innerTextField()
        },
    )
}

@Composable
private fun CheckInTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    singleLine: Boolean,
) {
    val borderColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outline
    }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CpDimens.radiusMd))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(CpDimens.radiusMd),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
            innerTextField()
        },
    )
}

@Composable
private fun FieldError(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(top = CpDimens.spacing1),
    )
}
