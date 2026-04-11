package org.openedx.core.extension

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openedx.core.config.Config
import org.openedx.foundation.extension.isLinkValid

actual object TextConverter : KoinComponent {

    private val config by inject<Config>()

    actual fun htmlTextToLinkedText(html: String): LinkedText {
        // Simple HTML tag stripping and link extraction for iOS
        // Extract links from <a href="...">text</a> patterns
        val linkRegex = Regex("""<a\s+[^>]*href\s*=\s*"([^"]*)"[^>]*>(.*?)</a>""", RegexOption.IGNORE_CASE)
        val linksMap = mutableMapOf<String, String>()

        for (match in linkRegex.findAll(html)) {
            var href = match.groupValues[1]
            val linkText = match.groupValues[2].replace(Regex("<[^>]*>"), "").trim()

            if (href.isNotEmpty() && href[0] == '/') {
                href = href.substring(1)
            }
            if (!href.startsWith("http")) {
                href = config.getApiHostURL() + href
            }
            if (href.isNotEmpty() && isLinkValid(href)) {
                linksMap[linkText] = href
            }
        }

        // Strip all HTML tags for plain text
        val text = html.replace(Regex("<[^>]*>"), "").trim()

        return LinkedText(text, linksMap.toMap())
    }

    actual fun isLinkValid(link: String): Boolean = link.lowercase().isLinkValid()
}
