package org.openedx.core.extension

data class LinkedText(
    val text: String,
    val links: Map<String, String>
)

expect object TextConverter {
    fun htmlTextToLinkedText(html: String): LinkedText
    fun isLinkValid(link: String): Boolean
}
