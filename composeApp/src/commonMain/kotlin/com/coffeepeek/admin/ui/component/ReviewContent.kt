package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.checkin_rating_atmosphere
import coffeepeek.composeapp.generated.resources.checkin_rating_coffee
import coffeepeek.composeapp.generated.resources.checkin_rating_service
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.admin.utils.CpImage
import com.coffeepeek.domain.model.Review
import com.coffeepeek.domain.model.ReviewRating
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ReviewFormField(
    label: String,
    error: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(CpDimens.spacing1)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
        error?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun ReviewTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    singleLine: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(CpDimens.radiusMd)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                shape = shape,
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
fun ReviewRatingCards(
    coffeeRating: Int,
    serviceRating: Int,
    placeRating: Int,
    onCoffeeRatingChange: (Int) -> Unit,
    onServiceRatingChange: (Int) -> Unit,
    onPlaceRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Ваши оценки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Нажмите на звёзды, чтобы изменить оценку",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        EditableRatingCard(
            image = Res.drawable.checkin_rating_coffee,
            label = "Кофе",
            rating = coffeeRating,
            onRatingChange = onCoffeeRatingChange,
        )
        EditableRatingCard(
            image = Res.drawable.checkin_rating_service,
            label = "Сервис",
            rating = serviceRating,
            onRatingChange = onServiceRatingChange,
        )
        EditableRatingCard(
            image = Res.drawable.checkin_rating_atmosphere,
            label = "Атмосфера",
            rating = placeRating,
            onRatingChange = onPlaceRatingChange,
        )
    }
}

@Composable
private fun EditableRatingCard(
    image: DrawableResource,
    label: String,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CpDimens.radiusMd))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(CpDimens.radiusMd))
            .padding(horizontal = CpDimens.spacing2, vertical = CpDimens.spacing3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(CpDimens.radiusSm)),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                (1..5).forEach { star ->
                    val icon: ImageVector = if (star <= rating) CpIcons.StarFilled else CpIcons.StarOutline
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onRatingChange(star) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "$label: выбрать $star из 5",
                            tint = if (star <= rating) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewRatingSummary(rating: ReviewRating, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
        RatingValue("Кофе", rating.coffee)
        RatingValue("Сервис", rating.service)
        RatingValue("Атмосфера", rating.place)
    }
}

/** Shared read-only review card used on the shop page and in the user's reviews. */
@Composable
fun ReviewDisplayCard(
    review: Review,
    modifier: Modifier = Modifier,
    onPhotoClick: ((String) -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CpDimens.radius2xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(CpDimens.spacing4),
            verticalArrangement = Arrangement.spacedBy(CpDimens.spacing4),
        ) {
            ReviewHeader(review = review, onEditClick = onEditClick)
            ReviewMetricCards(review.rating)
            ReviewScoreRow(review.rating.average)

            if (review.header.isNotBlank()) {
                Text(
                    text = review.header,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (review.comment.isNotBlank()) ReviewQuote(review.comment)

            ReviewPhotoStrip(
                photoUrls = review.photoUrls,
                onPhotoClick = onPhotoClick,
            )

            // Helpful votes and replies stay hidden until their state and
            // mutations are part of the backend contract.
        }
    }
}

@Composable
private fun ReviewHeader(review: Review, onEditClick: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReviewAvatar(avatarUrl = review.avatarUrl, username = review.username)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = CpDimens.spacing3),
        ) {
            Text(
                text = review.username.ifBlank { "Пользователь" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (review.createdAt.isNotBlank()) {
                Text(
                    text = formatReviewDisplayDate(review.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (onEditClick != null) {
            IconButton(onClick = onEditClick) {
                Text(
                    text = "•••",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReviewAvatar(avatarUrl: String?, username: String) {
    val avatarModifier = Modifier.size(52.dp).clip(CircleShape)
    if (!avatarUrl.isNullOrBlank()) {
        CpImage(
            data = avatarUrl,
            modifier = avatarModifier,
            contentDescription = "Фото пользователя ${username.ifBlank { "Пользователь" }}",
        )
    } else {
        Box(
            modifier = avatarModifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                shape = CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = username.trim().firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ReviewMetricCards(rating: ReviewRating) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        ReviewMetricCard(
            image = Res.drawable.checkin_rating_atmosphere,
            label = "Аура",
            value = rating.place,
            modifier = Modifier.weight(1f),
        )
        ReviewMetricCard(
            image = Res.drawable.checkin_rating_service,
            label = "Сервис",
            value = rating.service,
            modifier = Modifier.weight(1f),
        )
        ReviewMetricCard(
            image = Res.drawable.checkin_rating_coffee,
            label = "Кофе",
            value = rating.coffee,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ReviewMetricCard(
    image: DrawableResource,
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(CpDimens.radiusLg))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f))
            .padding(horizontal = CpDimens.spacing2, vertical = CpDimens.spacing2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(38.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ReviewScoreRow(average: Double) {
    val filledStars = average.toInt().coerceIn(0, 5)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            (1..5).forEach { star ->
                Icon(
                    imageVector = if (star <= filledStars) CpIcons.StarFilled else CpIcons.StarOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = "%.1f".format(average),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = CpDimens.spacing2),
        )
        Box(
            modifier = Modifier.weight(1f).padding(start = CpDimens.spacing2),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text = experienceLabel(average),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(CpDimens.radius2xl))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = CpDimens.spacing3, vertical = CpDimens.spacing2),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ReviewQuote(comment: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "“",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
            modifier = Modifier.width(28.dp),
        )
        Text(
            text = comment,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "”",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
            modifier = Modifier.width(20.dp),
        )
    }
}

private fun experienceLabel(average: Double): String = when {
    average >= 4.0 -> "Отличный опыт"
    average >= 3.0 -> "Хороший опыт"
    average >= 2.0 -> "Смешанные впечатления"
    else -> "Есть вопросы"
}

private fun formatReviewDisplayDate(raw: String): String {
    val datePart = raw.substringBefore('T').ifBlank { raw }
    val parts = datePart.split('-')
    if (parts.size != 3) return datePart
    val month = when (parts[1].toIntOrNull()) {
        1 -> "января"
        2 -> "февраля"
        3 -> "марта"
        4 -> "апреля"
        5 -> "мая"
        6 -> "июня"
        7 -> "июля"
        8 -> "августа"
        9 -> "сентября"
        10 -> "октября"
        11 -> "ноября"
        12 -> "декабря"
        else -> return datePart
    }
    return "${parts[2].trimStart('0').ifBlank { "0" }} $month ${parts[0]} г."
}

@Composable
private fun RatingValue(label: String, value: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(CpDimens.radiusSm))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .padding(horizontal = CpDimens.spacing2, vertical = CpDimens.spacing1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Icon(CpIcons.StarFilled, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
        Text(value.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ReviewPhotoStrip(
    photoUrls: List<String>,
    modifier: Modifier = Modifier,
    onPhotoClick: ((String) -> Unit)? = null,
) {
    if (photoUrls.isEmpty()) return
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
        photoUrls.take(3).forEach { url ->
            CpImage(
                data = url,
                contentDescription = "Фотография отзыва",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(CpDimens.radiusSm))
                    .then(
                        if (onPhotoClick != null) Modifier.clickable { onPhotoClick(url) }
                        else Modifier,
                    ),
            )
        }
        if (photoUrls.size > 3) {
            Text(
                "+${photoUrls.size - 3}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}
