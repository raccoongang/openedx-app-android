package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.room.discovery.CertificateDb
import org.openedx.core.domain.model.Certificate

@Serializable
data class Certificate(
    @SerialName("url")
    val certificateURL: String?
) {
    fun mapToDomain(): Certificate {
        return Certificate(
            certificateURL = certificateURL
        )
    }

    fun mapToRoomEntity() = CertificateDb(certificateURL)
}
