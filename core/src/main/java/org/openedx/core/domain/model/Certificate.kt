package org.openedx.core.domain.model

import org.openedx.core.data.model.room.discovery.CertificateDb

data class Certificate(
    val certificateURL: String?
) {
    fun isCertificateEarned() = certificateURL?.isNotEmpty() == true

    fun mapToRoomEntity() = CertificateDb(certificateURL)
}
