package org.openedx.profile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.LanguageProficiency

data class LanguageProficiency(
    @SerialName("code")
    val code: String?
) {
    fun mapToDomain(): LanguageProficiency {
        return LanguageProficiency(
            code = code ?: ""
        )
    }
}
