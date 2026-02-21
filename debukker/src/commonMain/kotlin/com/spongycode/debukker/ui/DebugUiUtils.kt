package com.spongycode.debukker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun getHighlightedText(text: String, query: String): AnnotatedString {
    if (query.isEmpty() || !text.contains(query, ignoreCase = true)) {
        return AnnotatedString(text)
    }

    val highlightColor = Color(0xFFFFFF00)
    val onHighlightColor = Color.Black

    return buildAnnotatedString {
        var start = 0
        while (start < text.length) {
            val index = text.indexOf(query, start, ignoreCase = true)
            if (index == -1) {
                append(text.substring(start))
                break
            }

            append(text.substring(start, index))
            withStyle(
                SpanStyle(
                    background = highlightColor,
                    color = onHighlightColor,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(index, index + query.length))
            }
            start = index + query.length
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    if (bytes < 1024) return "$bytes B"
    val exp = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    val value = bytes / 1024.0.pow(exp.toDouble())
    return if (value % 1 == 0.0) {
        "${value.toInt()} ${pre}B"
    } else {
        "${(value * 10).toInt() / 10.0} ${pre}B"
    }
}
