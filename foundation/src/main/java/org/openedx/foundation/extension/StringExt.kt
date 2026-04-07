package org.openedx.foundation.extension

private val EMAIL_REGEX = Regex(
    "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
)

private val URL_REGEX = Regex(
    "(https?|ftp)://[^\\s/$.?#].[^\\s]*",
    RegexOption.IGNORE_CASE,
)

fun String.isEmailValid(): Boolean {
    return EMAIL_REGEX.matches(this)
}

fun String.isLinkValid(): Boolean {
    return URL_REGEX.containsMatchIn(this)
}

fun String.replaceLinkTags(isDarkMode: Boolean = false): String {
    val linkColor = if (isDarkMode) "#3C91E4" else "#0D6EFD"
    return this.replace("<a ", "<a style=\"color:$linkColor;\" ")
}

fun String.replaceSpace(replacement: String = "+"): String {
    return this.replace(" ", replacement)
}

fun String.tagId(): String = "tag_$this"

fun String.takeIfNotEmpty(): String? {
    return if (this.isNotEmpty()) this else null
}

fun String.toImageLink(apiHostUrl: String): String {
    return if (this.startsWith("/")) {
        apiHostUrl + this
    } else {
        this
    }
}
