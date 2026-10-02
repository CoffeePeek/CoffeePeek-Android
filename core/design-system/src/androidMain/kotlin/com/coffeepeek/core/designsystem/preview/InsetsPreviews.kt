package com.coffeepeek.core.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.coffeepeek.core.designsystem.component.AppButton
import com.coffeepeek.core.designsystem.theme.CoffeePeekTheme

// Synthetic asymmetrical insets make fixtures deterministic in the IDE.
// These are recipes, not another public layout abstraction.
@Composable
private fun InsetsSamples(direction: LayoutDirection) {
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        CoffeePeekTheme {
            Surface {
                val insets = WindowInsets(left = 12.dp, top = 20.dp, right = 32.dp, bottom = 24.dp)
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${direction.name}: physical left 12 / right 32 dp")
                    Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer)
                        .windowInsetsPadding(insets.only(WindowInsetsSides.Horizontal))) {
                        // Nesting the same insets must not add a second gap.
                        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                            .windowInsetsPadding(insets.only(WindowInsetsSides.Horizontal))) {
                            Text("Content: insets applied once")
                            AppButton("Continue", {})
                        }
                    }
                    Text("Logical start / end spacers")
                    Row(Modifier.fillMaxWidth().height(48.dp)) {
                        Spacer(Modifier.fillMaxHeight().background(MaterialTheme.colorScheme.primary)
                            .windowInsetsStartWidth(insets))
                        Text("Start → content → End", Modifier.weight(1f))
                        Spacer(Modifier.fillMaxHeight().background(MaterialTheme.colorScheme.tertiary)
                            .windowInsetsEndWidth(insets))
                    }
                    Text("Bottom bars + synthetic keyboard: max, not sum")
                    val keyboard = WindowInsets(bottom = 64.dp)
                    Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer)
                        .windowInsetsPadding(insets.union(keyboard).only(WindowInsetsSides.Bottom))) {
                        Text("Content above keyboard", Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface).padding(8.dp))
                    }
                }
            }
        }
    }
}

@PreviewLightDark @Composable
private fun InsetsLtrPreview() = InsetsSamples(LayoutDirection.Ltr)

@PreviewLightDark @Composable
private fun InsetsRtlPreview() = InsetsSamples(LayoutDirection.Rtl)
