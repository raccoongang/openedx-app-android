package org.openedx.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.RegistrationField
import org.openedx.core.domain.model.RegistrationFieldType

data class RegistrationFields(
    @SerialName("fields")
    val fields: List<Field>?,
) {

    data class Field(
        @SerialName("name")
        val name: String?,
        @SerialName("label")
        val label: String?,
        @SerialName("type")
        val type: String?,
        @SerialName("placeholder")
        val placeholder: String?,
        @SerialName("instructions")
        val instructions: String?,
        @SerialName("exposed")
        val exposed: Boolean?,
        @SerialName("required")
        val required: Boolean?,
        @SerialName("restrictions")
        val restrictions: Restrictions?,
        @SerialName("options")
        val options: List<Option>?
    ) {
        fun mapToDomain(): RegistrationField {
            return RegistrationField(
                name = name ?: "",
                label = label ?: "",
                type = RegistrationFieldType.returnLocalTypeFromServerType(type),
                placeholder = placeholder ?: "",
                instructions = instructions ?: "",
                exposed = exposed ?: false,
                required = required ?: false,
                restrictions = restrictions?.mapToDomain() ?: RegistrationField.Restrictions(),
                options = options?.map { it.mapToDomain() } ?: emptyList()
            )
        }
    }

    data class Restrictions(
        @SerialName("max_length")
        val maxLength: Int?,
        @SerialName("min_length")
        val minLength: Int?
    ) {
        fun mapToDomain(): RegistrationField.Restrictions {
            return RegistrationField.Restrictions(
                maxLength = maxLength ?: 128,
                minLength = minLength ?: 1
            )
        }
    }

    data class Option(
        @SerialName("value")
        val value: String?,
        @SerialName("name")
        val name: String?,
        @SerialName("default")
        val default: String?
    ) {
        fun mapToDomain(): RegistrationField.Option {
            return RegistrationField.Option(
                value = value ?: "",
                name = name ?: "",
                default = default ?: ""
            )
        }
    }
}
