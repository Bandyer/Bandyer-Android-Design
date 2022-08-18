package com.kaleyra.collaboration_suite_core_ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

val symbolPattern by lazy {
    Regex("""(https?://[^\s\t\n]+)|(\*[\w]+\*)|(_[\w]+_)|(~[\w]+~)""")
}

enum class SymbolAnnotationType {
    LINK
}
typealias StringAnnotation = AnnotatedString.Range<String>
typealias SymbolAnnotation = Pair<AnnotatedString, StringAnnotation?>

/**
 * Format a message following Markdown-lite syntax
 *
 * @param text contains message to be parsed
 * @return AnnotatedString with annotations used inside the ClickableText wrapper
 */
@Composable
fun messageFormatter(
    text: String,
    primary: Boolean
): AnnotatedString {
    val tokens = symbolPattern.findAll(text)

    return buildAnnotatedString {

        var cursorPosition = 0

        for (token in tokens) {
            append(text.slice(cursorPosition until token.range.first))

            val (annotatedString, stringAnnotation) = getSymbolAnnotation(
                matchResult = token,
                colors = MaterialTheme.colors,
                primary = primary
            )
            append(annotatedString)

            if (stringAnnotation != null) {
                val (item, start, end, tag) = stringAnnotation
                addStringAnnotation(tag = tag, start = start, end = end, annotation = item)
            }

            cursorPosition = token.range.last + 1
        }

        if (!tokens.none()) append(text.slice(cursorPosition..text.lastIndex))
        else append(text)
    }
}

/**
 * Map regex matches found in a message with supported syntax symbols
 *
 * @param matchResult is a regex result matching our syntax symbols
 * @return pair of AnnotatedString with annotation (optional) used inside the ClickableText wrapper
 */
private fun getSymbolAnnotation(
    matchResult: MatchResult,
    colors: Colors,
    primary: Boolean
): SymbolAnnotation {
    return when (matchResult.value.first()) {
// De-comment to enable string formatting using some special symbols (*bold*, _italic_, ~strikethrough~)
//        '*' -> SymbolAnnotation(
//            AnnotatedString(
//                text = matchResult.value.trim('*'),
//                spanStyle = SpanStyle(fontWeight = FontWeight.Bold)
//            ),
//            null
//        )
//        '_' -> SymbolAnnotation(
//            AnnotatedString(
//                text = matchResult.value.trim('_'),
//                spanStyle = SpanStyle(fontStyle = FontStyle.Italic)
//            ),
//            null
//        )
//        '~' -> SymbolAnnotation(
//            AnnotatedString(
//                text = matchResult.value.trim('~'),
//                spanStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
//            ),
//            null
//        )
        'h' -> SymbolAnnotation(
            AnnotatedString(
                text = matchResult.value,
                spanStyle = SpanStyle(
                    color = if (primary) colors.onPrimary else colors.onSecondary,
                    textDecoration = TextDecoration.Underline
                )
            ),
            StringAnnotation(
                item = matchResult.value,
                start = matchResult.range.first,
                end = matchResult.range.last,
                tag = SymbolAnnotationType.LINK.name
            )
        )
        else -> SymbolAnnotation(AnnotatedString(matchResult.value), null)
    }
}