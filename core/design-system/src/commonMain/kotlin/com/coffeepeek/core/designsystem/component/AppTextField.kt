package com.coffeepeek.core.designsystem.component

import com.coffeepeek.core.designsystem.icons.CpIcons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.coffeepeek.core.designsystem.theme.CpDimens

@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (Boolean) -> Unit = {},
    passwordToggleDescription: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    errorText: String? = null,
    modifier: Modifier = Modifier,
) {
    require(!isPassword || !passwordToggleDescription.isNullOrBlank()) {
        "Password fields require a localized visibility action description"
    }
    val isError = errorText != null

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
        )

        CompactOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CpDimens.inputMinHeight)
                .semantics {
                    contentDescription = label
                    errorText?.let { error(it) }
                },
            shape = RoundedCornerShape(CpDimens.inputRadius),
            contentPadding = CpDimens.singleLineFieldContentPadding,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor  = MaterialTheme.colorScheme.surfaceVariant,
                errorContainerColor     = MaterialTheme.colorScheme.surface,
                focusedBorderColor      = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                errorBorderColor        = MaterialTheme.colorScheme.error,
                cursorColor             = MaterialTheme.colorScheme.primary,
                errorCursorColor        = MaterialTheme.colorScheme.error,
            ),
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else null,
            trailingIcon = {
                when {
                    isPassword -> {
                        IconButton(
                            onClick = { onPasswordVisibilityChange(!passwordVisible) },
                            enabled = enabled && !readOnly,
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) CpIcons.Visibility
                                              else CpIcons.VisibilityOff,
                                contentDescription = passwordToggleDescription,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    isError -> {
                        Icon(
                            imageVector = CpIcons.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = if (isPassword) keyboardOptions.copy(keyboardType = KeyboardType.Password)
                else keyboardOptions,
            keyboardActions = keyboardActions,
        )

        AnimatedVisibility(visible = isError) {
            Text(
                text = errorText ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = CpDimens.inputPadding),
            )
        }
    }
}
