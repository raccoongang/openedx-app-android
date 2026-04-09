package org.openedx.core.domain.model

data class Certificate(
    val certificateURL: String?
) {
    fun isCertificateEarned() = certificateURL?.isNotEmpty() == true
}
