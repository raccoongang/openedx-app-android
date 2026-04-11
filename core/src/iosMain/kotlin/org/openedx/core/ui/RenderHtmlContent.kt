package org.openedx.core.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
actual fun RenderHtmlContent(html: String) {
    // Simplified HTML rendering for iOS — strips tags
    val text = html.replace(Regex("<[^>]*>"), "").trim()
    Text(text = text)
}
