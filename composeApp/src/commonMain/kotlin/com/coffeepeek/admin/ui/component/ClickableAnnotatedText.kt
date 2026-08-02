package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle

@Composable
fun ClickableAnnotatedText(
    text: AnnotatedString,
    style: TextStyle,
    annotationTag: String,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    BasicText(
        text = text,
        style = style,
        onTextLayout = { layoutResult = it },
        modifier = modifier.pointerInput(text, annotationTag) {
            detectTapGestures { position ->
                val layout = layoutResult ?: return@detectTapGestures
                val offset = layout.getOffsetForPosition(position)
                text.getStringAnnotations(annotationTag, offset, offset)
                    .firstOrNull()
                    ?.item
                    ?.let(onLinkClick)
            }
        },
    )
}
