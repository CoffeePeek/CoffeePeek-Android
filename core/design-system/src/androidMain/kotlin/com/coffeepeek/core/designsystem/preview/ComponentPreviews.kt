package com.coffeepeek.core.designsystem.preview

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.coffeepeek.core.designsystem.component.*
import com.coffeepeek.core.designsystem.icons.CpIcons
import com.coffeepeek.core.designsystem.modifier.GlassIconButton
import com.coffeepeek.core.designsystem.theme.CoffeePeekTheme

/** Uses system preview uiMode for both themes. Brand font is injected at integration. */
@Composable
private fun PreviewTheme(content: @Composable () -> Unit) {
    CoffeePeekTheme(FontFamily.Default) {
        Surface { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        } }
    }
}

@PreviewLightDark @Composable
private fun ButtonsPreview() = PreviewTheme {
    AppButton("Continue", {})
    AppButton("Unavailable", {}, enabled = false)
    GlassIconButton({}, contentDescription = "Close", hazeState = null) {
        Icon(CpIcons.Close, null)
    }
}

@PreviewLightDark @Composable
private fun GroupedRowsPreview() = PreviewTheme {
    GroupSection("Options") {
        CheckmarkRow("Selected option", true, {})
        RowSeparator()
        ActionRow("Action", {})
        SwitchRow("Notifications", true, {})
        StepperRow("Quantity", "2", {}, {}, "Decrease", "Increase")
    }
}

@PreviewLightDark @Composable
private fun FieldsPreview() = PreviewTheme {
    var value by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    AppTextField("Name", value, { value = it }, "Enter name")
    AppTextField("Name", "", {}, "Enter name", errorText = "Required")
    AppTextField("Password", "example", {}, "Password", isPassword = true,
        passwordVisible = visible, onPasswordVisibilityChange = { visible = it },
        passwordToggleDescription = if (visible) "Hide password" else "Show password")
    AppTextField("Disabled", "", {}, "Unavailable", enabled = false)
    CompactOutlinedTextField(value, { value = it }, placeholder = { Text("Compact input") })
}

@PreviewLightDark @Composable
private fun SearchPreview() = PreviewTheme {
    var query by remember { mutableStateOf("Coffee") }
    CpSearchField(query, { query = it }, "Search", "Clear search")
}

@PreviewLightDark @Composable
private fun TopBarPreview() = PreviewTheme {
    CpTopBar("CoffeePeek", "Back", onBack = {})
}

@PreviewLightDark @Composable
private fun SettingsPreview() = PreviewTheme {
    SettingsSection("Settings", description = "Presentation only") {
        SettingsRow(CpIcons.Settings, "Appearance", description = "System theme", onClick = {})
        SettingsDivider()
        SettingsRow(CpIcons.Settings, "Unavailable", onClick = {}, enabled = false)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconBadge(CpIcons.Settings, IconBadgePalette.Cyan, contentDescription = "Cyan badge")
        IconBadge(CpIcons.Settings, IconBadgePalette.Gold, contentDescription = "Gold badge")
    }
}

@PreviewLightDark @Composable
private fun SegmentsPreview() = PreviewTheme {
    var selected by remember { mutableStateOf("First") }
    CapsuleSegmentedControl(listOf("First", "Second"), selected, { it }, { selected = it })
}

@PreviewLightDark @Composable
private fun LoaderPreview() = PreviewTheme { CoffeePeekLoader("Loading") }

@PreviewLightDark @Composable
private fun FabMenuPreview() = PreviewTheme {
    FabMenu(listOf(FabMenuAction(CpIcons.Settings, "Settings", {}),
        FabMenuAction(CpIcons.Close, "Close", {}),
        FabMenuAction(CpIcons.Search, "Search unavailable", {}, enabled = false)))
}

// Modal windows are best inspected in Interactive Preview / Run Preview.
@PreviewLightDark @Composable
private fun SheetPreview() = PreviewTheme {
    var shown by remember { mutableStateOf(true) }
    if (shown) SwipeDismissModalBottomSheet({ shown = false }, "Dismiss sheet") {
        Column(Modifier.padding(24.dp)) {
            Text("Example sheet", style = MaterialTheme.typography.titleLarge)
            Text("Drag the handle or invoke its accessible dismiss action.")
        }
    }
}

@PreviewLightDark @Composable
private fun ErrorDialogPreview() = PreviewTheme {
    var shown by remember { mutableStateOf(true) }
    ErrorDialog(shown, "Try again later", "Something went wrong", "Dismiss", { shown = false })
}

@PreviewLightDark @Composable
private fun LoadingDialogPreview() = PreviewTheme {
    LoadingDialog(true, "Loading", "Please wait")
}
